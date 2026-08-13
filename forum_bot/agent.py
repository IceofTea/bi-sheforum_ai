"""校园智脑 — 多智能体协作引擎

架构（Multi-Agent Collaboration）：
    CampusAgent (Coordinator 总控)
    ├── PlannerAgent   查询理解：意图识别 / 子查询分解 / 查询改写
    ├── RetrieverAgent 双通道检索：向量语义 + BM25关键词，RRF融合 + 迭代收敛
    ├── CriticAgent    自查-修复闭环：生成后审查，依据不足自动补检重写
    ├── PersonaProvider 个性化：用户画像（资料/收藏/发帖兴趣）
    └── Memory          记忆：会话上下文 + 用户反馈偏好

创新点：
1. 多智能体协作编排（规划/检索/审查/记忆 分角色协同）
2. 双通道检索 + RRF 融合（语义互补，提升召回）
3. 自查-修复闭环（Agent 自我评估答案质量并迭代修复）
4. 全本地部署（Ollama embedding + LLM），零成本、隐私、离线可用
5. 完整推理轨迹输出（可解释性，供评委演示）
"""
import time
import json
import logging

from config import (
    AGENT_MAX_ROUNDS, AGENT_SCORE_THRESHOLD, AGENT_QUERY_REWRITE,
    AGENT_EXPLAIN, AGENT_SELF_CRITIQUE, AGENT_DUAL_RETRIEVAL,
    PERSONA_ENABLED, PERSONA_MAX_ITEMS, FORUM_API_URL,
    TOP_K, MEMORY_ENABLED, MEMORY_MAX_TURNS
)

logger = logging.getLogger("CampusAgent")

try:
    from emotion import predict as emotion_predict, sentiment_prompt as emotion_sentiment_prompt
    _EMOTION_AVAILABLE = True
except Exception as _e:
    logger.warning(f"[Agent] 情绪预判模块加载失败: {_e}")
    emotion_predict = None
    _EMOTION_AVAILABLE = False

try:
    from clarify import ClarifyGuide
    _CLARIFY_AVAILABLE = True
except Exception as _e:
    logger.warning(f"[Agent] 主动引导模块加载失败: {_e}")
    ClarifyGuide = None
    _CLARIFY_AVAILABLE = False

PLANNER_SYSTEM_PROMPT = """你是一个校园论坛智能体的"查询理解与规划器"。
给定用户的原始问题，你要：
1. 识别其中的核心意图（如：考研咨询、失物招领、选课建议、二手交易、报修……）
2. 如果有多个子主题，拆解为最多 3 个独立子查询
3. 对模糊表述做改写/扩展，使其更适合语义检索

只输出 JSON Schema：
{"intent": "一句话意图", "sub_queries": ["子查询1", "子查询2"], "keywords": ["关键词1", "关键词2"]}
"""

CRITIC_SYSTEM_PROMPT = """你是一个"答案质量审查员"。给定用户问题、检索到的资料片段和智能体生成的答案，
判断该答案是否足够回答用户问题。只输出一个 JSON：
{"satisfied": true/false, "missing": "如果不满意的原因/缺少的信息"}"""

CLARIFY_SYSTEM_PROMPT = """你是一个"需求澄清助手"。当用户的问题过于模糊、缺少必要约束条件而无法直接回答时，
你应输出一个追问，引导用户补充关键信息。追问要具体、友好、指向明确，一句话即可。
例如：用户问"食堂哪家好吃"→"请问您偏好食堂还是校外商铺？口味偏清淡还是重口？预算大概多少？"
如果问题无需澄清，则输出"无需澄清"四个字。"""


class _TraceSink:
    """推理轨迹收集器：避免空列表假值短路问题"""

    def __init__(self, enabled=True, on_add=None):
        self.enabled = enabled
        self.items = []
        self._on_add = on_add

    def add(self, msg: str):
        if not self.enabled:
            return
        self.items.append(msg)
        if self._on_add:
            try:
                self._on_add(msg)
            except Exception:
                pass

    def entries(self):
        return list(self.items) if self.enabled else []


# ═══════════════════════════════════════════════════════
# PlannerAgent：查询理解与规划
# ═══════════════════════════════════════════════════════
class PlannerAgent:
    """规划智能体：理解查询、拆解子查询、提取关键词"""

    def __init__(self, llm, on_llm=None):
        self.llm = llm
        self.on_llm = on_llm or (lambda: None)

    def plan(self, query: str) -> dict:
        if not AGENT_QUERY_REWRITE:
            return {"intent": "通用问答", "sub_queries": [query], "keywords": query.split()}
        try:
            raw = self.llm.generate(
                f"用户问题：{query}\n\n请输出规划结果 JSON：",
                max_tokens=200, system_prompt=PLANNER_SYSTEM_PROMPT,
            )
            self.on_llm()
            start, end = raw.find("{"), raw.rfind("}") + 1
            if start >= 0 and end > start:
                data = json.loads(raw[start:end])
                return {
                    "intent": data.get("intent", "通用问答"),
                    "sub_queries": [q for q in data.get("sub_queries", []) if q][:3],
                    "keywords": [k for k in data.get("keywords", []) if k],
                }
        except Exception as e:
            logger.warning(f"[Planner] 规划失败，回退原始查询: {e}")
        return {"intent": "通用问答", "sub_queries": [query], "keywords": query.split()}


# ═══════════════════════════════════════════════════════
# RetrieverAgent：双通道检索（向量 + BM25，RRF 融合）
# ═══════════════════════════════════════════════════════
class BM25Retriever:
    """轻量 BM25 关键词检索通道（基于 TF-IDF，与向量通道互补）"""

    def __init__(self, rag):
        self.rag = rag
        self._built = False
        self._docs = []
        self._vectorizer = None
        self._matrix = None

    def _ensure_built(self):
        if self._built:
            return
        try:
            from sklearn.feature_extraction.text import TfidfVectorizer
            if self.rag.embedding.collection is None:
                return
            res = self.rag.embedding.collection.get(include=["documents", "metadatas"])
            ids = res.get("ids") or []
            docs = res.get("documents") or []
            self._docs = [(cid, docs[i] if i < len(docs) else "") for i, cid in enumerate(ids)]
            self._docs = [(cid, c) for cid, c in self._docs if c]
            if not self._docs:
                return
            self._vectorizer = TfidfVectorizer(ngram_range=(1, 2), max_features=8000)
            self._matrix = self._vectorizer.fit_transform([d[1] for d in self._docs])
            self._built = True
        except Exception as e:
            logger.warning(f"[BM25] 索引构建失败: {e}")

    def search(self, query: str, top_k: int = 5) -> list:
        self._ensure_built()
        if not self._built:
            return []
        try:
            from sklearn.metrics.pairwise import cosine_similarity
            import numpy as np
            qvec = self._vectorizer.transform([query])
            sims = cosine_similarity(qvec, self._matrix)[0]
            order = np.argsort(sims)[::-1][:top_k]
            results = []
            for idx in order:
                if sims[idx] <= 0:
                    continue
                cid, content = self._docs[int(idx)]
                results.append({
                    "id": cid, "content": content, "similarity": float(sims[idx]),
                    "metadata": {"type": "bm25"}, "channel": "bm25",
                })
            return results
        except Exception as e:
            logger.warning(f"[BM25] 检索失败: {e}")
            return []


def rrf_fusion(vector_docs, bm25_docs, k=60, top_n=None):
    """Reciprocal Rank Fusion：融合两通道结果"""
    scores = {}
    order = {}
    for channel, docs in (("vector", vector_docs), ("bm25", bm25_docs)):
        for rank, d in enumerate(docs):
            cid = d["id"]
            scores[cid] = scores.get(cid, 0.0) + 1.0 / (k + rank + 1)
            order.setdefault(cid, {})[channel] = d
    merged = []
    for cid, s in sorted(scores.items(), key=lambda kv: kv[1], reverse=True):
        d = dict(order[cid].get("vector") or order[cid].get("bm25"))
        d["id"] = cid
        d["fusion_score"] = round(s, 4)
        merged.append(d)
        if top_n and len(merged) >= top_n:
            break
    return merged


class RetrieverAgent:
    """检索智能体：向量 + BM25 双通道，支持迭代改写"""

    def __init__(self, rag, llm):
        self.rag = rag
        self.llm = llm
        self.bm25 = BM25Retriever(rag)

    def retrieve(self, query: str, top_k: int = None) -> list:
        import threading as _threading
        top_k = top_k or TOP_K
        result_holder = {}

        def _vector():
            try:
                vd = self.rag.embedding.search(query, top_k=top_k)
                for d in vd:
                    d["channel"] = "vector"
                result_holder["vector"] = vd
            except Exception as e:
                logger.warning(f"[Retriever] 向量检索失败: {e}")
                result_holder["vector"] = []

        def _bm25():
            try:
                bd = self.bm25.search(query, top_k=top_k)
                result_holder["bm25"] = bd
            except Exception as e:
                logger.warning(f"[Retriever] BM25 检索失败: {e}")
                result_holder["bm25"] = []

        threads = [_threading.Thread(target=_vector)]
        if AGENT_DUAL_RETRIEVAL:
            threads.append(_threading.Thread(target=_bm25))
        for t in threads:
            t.start()
        for t in threads:
            t.join(timeout=30)

        vector_docs = result_holder.get("vector", [])
        if AGENT_DUAL_RETRIEVAL:
            bm25_docs = result_holder.get("bm25", [])
            return rrf_fusion(vector_docs, bm25_docs, top_n=top_k)
        return vector_docs

    def get_similar(self, query: str, threshold: float = 0.9) -> list:
        return self.rag.embedding.get_similar_posts(query, threshold=threshold)


# ═══════════════════════════════════════════════════════
# CriticAgent：自查-修复闭环
# ═══════════════════════════════════════════════════════
class CriticAgent:
    """审查智能体：评估答案质量，决定是否补充检索重写"""

    def __init__(self, llm, on_llm=None):
        self.llm = llm
        self.on_llm = on_llm or (lambda: None)

    def review(self, query: str, answer: str, context: str) -> dict:
        try:
            prompt = (
                f"用户问题：{query}\n\n检索资料片段：{context[:800]}\n\n"
                f"智能体答案：{answer[:800]}\n\n请输出审查 JSON："
            )
            raw = self.llm.generate(prompt, max_tokens=100, system_prompt=CRITIC_SYSTEM_PROMPT)
            self.on_llm()
            start, end = raw.find("{"), raw.rfind("}") + 1
            if start >= 0 and end > start:
                data = json.loads(raw[start:end])
                return {
                    "satisfied": bool(data.get("satisfied", True)),
                    "missing": data.get("missing", ""),
                }
        except Exception as e:
            logger.warning(f"[Critic] 审查失败，默认通过: {e}")
        return {"satisfied": True, "missing": ""}


# ═══════════════════════════════════════════════════════
# Memory：会话记忆 + 用户反馈偏好
# ═══════════════════════════════════════════════════════
class Memory:
    """记忆模块：多轮会话上下文 + 用户反馈偏好"""

    def __init__(self):
        self.sessions = {}
        self.feedback = {}

    def add_turn(self, user_id, role, content, max_turns=None):
        if not MEMORY_ENABLED or user_id is None:
            return
        max_turns = max_turns or MEMORY_MAX_TURNS
        self.sessions.setdefault(user_id, []).append((role, content))
        self.sessions[user_id] = self.sessions[user_id][-max_turns:]

    def history(self, user_id) -> list:
        return list(self.sessions.get(user_id, []))

    def record_feedback(self, user_id, query, liked):
        if user_id is None:
            return
        fb = self.feedback.setdefault(user_id, {"like": set(), "dislike": set()})
        key = "like" if liked else "dislike"
        fb[key].add(query)

    def preference(self, user_id) -> dict:
        fb = self.feedback.get(user_id, {})
        return {
            "liked": list(fb.get("like", set()))[-3:],
            "disliked": list(fb.get("dislike", set()))[-3:],
        }


# ═══════════════════════════════════════════════════════
# CampusAgent：Coordinator 总控
# ═══════════════════════════════════════════════════════
class CampusAgent:
    """校园智脑 — 多智能体协作总控"""

    def __init__(self, rag=None, intent=None, cache=None, persona=None, llm=None, memory=None):
        from generator import LLMGenerator, RAGSystem
        from intent import IntentJudger
        from cache import RedisCache

        self.rag = rag or RAGSystem()
        self.intent = intent or IntentJudger()
        self.cache = cache or RedisCache()
        self.llm = llm or (self.rag.llm if self.rag.llm else LLMGenerator())

        def _bump():
            self.stats["llm_calls"] += 1

        # 子智能体（共享成本统计）
        self.planner = PlannerAgent(self.llm, on_llm=_bump)
        self.retriever = RetrieverAgent(self.rag, self.llm)
        self.critic = CriticAgent(self.llm, on_llm=_bump)
        self.persona = persona or PersonaProvider()
        self.memory = memory or Memory()
        self.clarify_guide = ClarifyGuide() if _CLARIFY_AVAILABLE else None

        self.stats = {
            "llm_calls": 0, "total_time_ms": 0.0,
            "retrieval_rounds": 0, "cache_hits": 0, "proactive_guides": 0,
        }

    def _reset_stats(self):
        for k in self.stats:
            self.stats[k] = 0

    def _count_llm(self):
        self.stats["llm_calls"] += 1

    # ── 主流程 ──────────────────────────────────

    def answer(self, query: str, user_id: int = None, session_id: str = None,
               as_comment: bool = True, use_critique=None, on_stage: callable = None) -> dict:
        start_t = time.perf_counter()
        self._reset_stats()
        trace = _TraceSink(AGENT_EXPLAIN, on_add=on_stage)

        query = (query or "").strip()
        if not query:
            return self._finish(start_t, "请提出你的问题～", "none", 0.0, [], trace)

        # 记忆注入
        if MEMORY_ENABLED and user_id:
            history = self.memory.history(user_id)
            if history:
                trace.add(f"[记忆] 已载入 {len(history)} 条会话历史")

        # ── 0. 情绪与意图预判（Zero-cost prelude） ──
        emotion_pred = {}
        if emotion_predict is not None:
            emotion_pred = emotion_predict(query)
            trace.add(
                f"[0] 情绪预判: {emotion_pred.get('emotion')}"
                f"({emotion_pred.get('sentiment')}) · 意图类别={emotion_pred.get('intent_category')}"
                f" · 紧急度={emotion_pred.get('urgency')}"
            )

        # ── 1. 缓存命中 ──
        mem_key = session_id or f"u{user_id}"
        cache_key = f"agent:{mem_key}:{query}"
        cached = self.cache.get(cache_key)
        if cached:
            self.stats["cache_hits"] += 1
            trace.add("[1] 缓存命中，直接返回历史答案")
            return self._finish(start_t, cached, "cache", 1.0, [], trace, emotion_pred=emotion_pred)

        # ── 2. 意图判断 + 查询规划 ──
        intent = self.intent.judge(query)
        trace.add(f"[2] 意图识别: {'提问' if intent.get('is_question') else '非提问'} ({intent.get('reason')})")
        plan = self.planner.plan(query)
        query_pool = list(dict.fromkeys([query] + plan.get("sub_queries", [])))
        trace.add(f"[3] 查询规划: 意图={plan['intent']}, 子查询={query_pool}")

        # ── 3. 迭代检索 + 相关性评估 ──
        best_docs, best_score = self._iterate_retrieval(query, query_pool, trace)

        # ── 4. 个性化应答生成 ──
        persona = None
        if PERSONA_ENABLED and user_id:
            persona = self.persona.get_profile(user_id)
            trace.add(f"[4] 个性化: 已加载用户 #{user_id} 画像")

        if not best_docs:            # 无检索结果：主动引导澄清，避免僵化拒绝
            guide = None
            if self.clarify_guide is not None:
                guide = self.clarify_guide.clarify(query, emotion_pred)
            if guide:
                self.stats["proactive_guides"] += 1
                trace.add(f"[4] 主动引导: 检测到信息缺口 → 引导用户补充（{guide['source']}）")
                return self._finish(start_t, guide["text"], "clarify", 0.0, [], trace, persona,
                                    clarify=guide["text"], emotion_pred=emotion_pred,
                                    guide_options=guide.get("options", []))
            clarify = self._try_clarify(query)
            if clarify:
                self._count_llm()
                trace.add(f"[4] 无上下文，主动追问澄清: {clarify}")
                return self._finish(start_t, clarify, "clarify", 0.0, [], trace, persona, clarify=clarify, emotion_pred=emotion_pred)
            answer = "抱歉，当前论坛内容库中未找到与您问题相关的信息，请尝试换个问法或补充细节。"
            source, similarity = "no_context", 0.0
        else:
            context_text = self.llm._build_context(best_docs)
            prompt = (
                f"基于以下参考资料回答用户问题：\n\n用户问题：{query}\n\n"
                f"参考资料：\n{context_text}\n\n请根据以上参考资料给出回答："
            )
            sentiment_note = emotion_sentiment_prompt(emotion_pred) if emotion_pred else ""
            answer = self.llm.generate_personalized(prompt, persona=persona, max_tokens=600,
                                                    emotion_instruction=sentiment_note)
            self._count_llm()
            source = "llm_personal" if persona else "llm"
            similarity = best_score

            # ── 5. 自查-修复闭环 ──
            if use_critique is not False and AGENT_SELF_CRITIQUE:
                review = self.critic.review(query, answer, context_text)
                trace.add(f"[5] 自查审查: {'通过' if review['satisfied'] else '不通过 - ' + review['missing']}")
                if not review["satisfied"] and review.get("missing"):
                    rewritten = self._rewrite_for_missing(query, review.get("missing", ""))
                    self._count_llm()
                    if rewritten and rewritten != query:
                        trace.add(f"[5] 修复：补充查询「{rewritten}」")
                        extra = self.retriever.retrieve(rewritten, top_k=2)
                        known = {x["id"] for x in best_docs}
                        extra = [d for d in extra if d["id"] not in known]
                        if extra:
                            best_docs = (best_docs + extra)[:TOP_K]
                            context2 = self.llm._build_context(best_docs)
                            prompt2 = (
                                f"基于补充后的参考资料重新回答用户问题：\n\n用户问题：{query}\n\n"
                                f"参考资料：\n{context2}\n\n请给出更完整准确的回答："
                            )
                            answer = self.llm.generate_personalized(prompt2, persona=persona, max_tokens=600,
                                                                    emotion_instruction=sentiment_note)
                            self._count_llm()
                            source = "llm_selfrepair"
                            trace.add("[5] 已用补充资料重新生成答案")

            # 关联问题推荐
            recs = self.rag.get_recommendations(query)
            if recs:
                rec_text = "\n\n📌 相关问题推荐："
                for idx, rec in enumerate(recs[:3], 1):
                    url = rec.get("metadata", {}).get("url", "")
                    title = rec.get("content", "")
                    rec_text += f"\n{idx}. {title}\n   链接：{url}" if url else f"\n{idx}. {title}"
                answer += rec_text

            # 主动式回答后引导：延伸话题 + 紧急行动指引
            if self.clarify_guide is not None:
                follow = self.clarify_guide.followup(query, emotion_pred, answer)
                if follow:
                    self.stats["proactive_guides"] += 1
                    trace.add(f"[5.5] 主动引导: 回答后延伸话题+行动指引")
                    answer += "\n\n" + follow

        # ── 6. 记忆写入 + 缓存 ──
        if MEMORY_ENABLED and user_id:
            self.memory.add_turn(user_id, "user", query)
            self.memory.add_turn(user_id, "assistant", answer[:200])
        self._cache_answer(cache_key, answer)

        if as_comment and user_id:
            trace.add(f"[6] 行动：作为用户 #{user_id} 执行")
        return self._finish(start_t, answer, source, similarity, best_docs, trace, persona, emotion_pred=emotion_pred)

    def _iterate_retrieval(self, query, query_pool, trace):
        """迭代检索：双通道召回 + LLM 相关性评估 + 查询改写收敛"""
        best_docs, best_score = [], 0.0
        for round_i in range(AGENT_MAX_ROUNDS):
            self.stats["retrieval_rounds"] += 1
            # 高分直接命中（节约 token）
            hit = None
            for q in query_pool:
                similar = self.retriever.get_similar(q, threshold=0.9)
                if similar:
                    hit = similar[0]
                    break
            if hit:
                url = hit.get("metadata", {}).get("url", "")
                answer = (
                    f"为您找到相似问题解答：\n\n{hit['content'][:500]}"
                    f"{('原文链接：' + url) if url else ''}"
                    f"\n\n（此回答来自历史相似帖子，相似度: {hit['similarity']:.2f}）"
                )
                trace.add(f"[R{round_i}] 高分命中(>{0.9})，直接返回")
                return [hit], hit["similarity"]

            # 双通道检索
            merged = []
            for q in query_pool:
                merged.extend(self.retriever.retrieve(q, top_k=TOP_K))
            seen, merged2 = set(), []
            for d in merged:
                if d["id"] not in seen:
                    seen.add(d["id"])
                    merged2.append(d)
            merged = merged2
            merged.sort(key=lambda d: d.get("fusion_score", d.get("similarity", 0)), reverse=True)

            if not merged:
                trace.add(f"[R{round_i}] 无检索结果，停止")
                break

            top = merged[0]
            top["_relevance"] = self.llm.score_relevance(query, top.get("content", ""))
            self._count_llm()
            trace.add(f"[R{round_i}] 召回 {len(merged)} 条，LLM评估相关性={top['_relevance']:.2f}")

            if top["_relevance"] >= AGENT_SCORE_THRESHOLD or round_i == AGENT_MAX_ROUNDS - 1:
                best_docs = merged[:TOP_K]
                best_score = top["_relevance"]
                trace.add(f"[R{round_i}] 收敛（达标/达轮数上限）")
                break

            if AGENT_QUERY_REWRITE:
                rewritten = self._rewrite_by_feedback(query, top)
                self._count_llm()
                if rewritten and rewritten not in query_pool:
                    query_pool.insert(0, rewritten)
                    trace.add(f"[R{round_i}] 查询改写: {rewritten}")
                else:
                    best_docs, best_score = merged[:TOP_K], top["_relevance"]
                    break
        return best_docs, best_score

    def _rewrite_by_feedback(self, query, top_doc):
        feedback = (top_doc.get("content") or "")[:200].replace("\n", " ")
        prompt = (
            f"原始问题：{query}\n当前召回内容概览：{feedback}\n"
            f"请改写成一个更聚焦的检索查询（仅输出一句查询，不要其他文字）："
        )
        try:
            return self.llm.generate(prompt, max_tokens=50, system_prompt="").strip().strip('"')
        except Exception:
            return ""

    def _rewrite_for_missing(self, query, missing):
        prompt = (
            f"用户问题：{query}\n当前答案缺少信息：{missing}\n"
            f"请生成一个补充检索查询，用于找到缺失的信息（仅输出一句查询，不要其他文字）："
        )
        try:
            return self.llm.generate(prompt, max_tokens=50, system_prompt="").strip().strip('"')
        except Exception:
            return ""

    def _try_clarify(self, query: str) -> str:
        """当检索无结果时，判断问题是否模糊并生成澄清追问"""
        try:
            raw = self.llm.generate(
                f"用户问题：{query}", max_tokens=80, system_prompt=CLARIFY_SYSTEM_PROMPT,
            )
            self._count_llm()
            txt = (raw or "").strip().strip('"')
            if txt and txt != "无需澄清" and len(txt) < 120:
                return txt
        except Exception as e:
            logger.warning(f"[Clarify] 澄清生成失败: {e}")
        return ""

    def feedback(self, user_id, query, liked):
        self.memory.record_feedback(user_id, query, liked)

    def _cache_answer(self, key, answer):
        try:
            self.cache.set(key, answer)
        except Exception:
            pass

    def _finish(self, start_t, answer, source, similarity, docs, trace, persona=None, clarify=None,
                emotion_pred=None, guide_options=None) -> dict:
        elapsed_ms = (time.perf_counter() - start_t) * 1000
        self.stats["total_time_ms"] = elapsed_ms
        # 证据溯源：从检索文档中提取可引用的来源（标题/摘要/相似度/链接）
        evidence = []
        seen_ev = set()
        for d in (docs or [])[:5]:
            meta = d.get("metadata", {}) or {}
            title = meta.get("title") or meta.get("name") or (d.get("content") or "")[:40]
            if not title or title in seen_ev:
                continue
            seen_ev.add(title)
            evidence.append({
                "title": title,
                "snippet": (d.get("content") or "")[:120],
                "similarity": round(d.get("fusion_score", d.get("similarity", 0)), 3),
                "url": meta.get("url", ""),
            })
        result = {
            "answer": answer,
            "source": source,
            "similarity": round(similarity, 4),
            "recommendations": len(docs),
            "trace": trace.entries(),
            "persona_applied": persona is not None,
            "evidence": evidence,
            "emotion": emotion_pred or {},
            "stats": dict(self.stats),
            "cost_estimate": {
                "llm_calls": self.stats["llm_calls"],
                "approx_tokens": self.stats["llm_calls"] * 300,
                "time_ms": round(elapsed_ms, 1),
            },
        }
        if clarify:
            result["clarify"] = clarify
        if guide_options:
            result["guide_options"] = guide_options
        return result


# ═══════════════════════════════════════════════════════
# PersonaProvider：个性化画像
# ═══════════════════════════════════════════════════════
class PersonaProvider:
    """用户画像提供者：从论坛 API 聚合用户资料、收藏、发帖主题"""

    def __init__(self, api_base: str = None, memory: Memory = None):
        import requests
        self.api_base = (api_base or FORUM_API_URL).rstrip("/")
        self.requests = requests
        self._cache = {}
        self.memory = memory or Memory()

    def _get(self, path: str, **params):
        try:
            resp = self.requests.get(f"{self.api_base}{path}", params=params, timeout=8)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", data) if isinstance(data, dict) else data
        except Exception as e:
            logger.warning(f"[Persona] 请求 {path} 失败: {e}")
        return None

    def get_profile(self, user_id: int) -> dict:
        if user_id in self._cache:
            return self._cache[user_id]
        if not user_id:
            return {}

        base = self._get("/api/users/one", id=user_id) or {}
        profile = {
            "nickname": base.get("nickname", ""),
            "level": base.get("level", 1),
            "experience": base.get("experience", 0),
            "interests": [],
            "behaviors": [],
        }

        collects = self._get("/api/usercollect/byuser", userId=user_id) or []
        interests = []
        for c in collects[:PERSONA_MAX_ITEMS]:
            tid = c.get("threadInfoId") or c.get("id")
            if tid:
                thread = self._get("/api/threads/one", id=tid)
                if thread and thread.get("name"):
                    interests.append(thread["name"][:30])
        profile["interests"] = interests

        threads = self._get("/api/threads/byuser", writerId=user_id) or []
        if isinstance(threads, dict):
            threads = threads.get("data", []) or threads.get("list", []) or []
        thread_topics = [(t.get("name") or "")[:30] for t in threads[:PERSONA_MAX_ITEMS] if t.get("name")]
        profile["behaviors"] = (
            [f"近期发帖: {t}" for t in thread_topics]
            if thread_topics else [f"收藏了 {len(collects) or 0} 条内容"]
        )

        pref = self.memory.preference(user_id)
        if pref.get("liked"):
            profile["behaviors"].append("点赞过: " + "、".join(pref["liked"]))
        if pref.get("disliked"):
            profile["behaviors"].append("不感兴趣: " + "、".join(pref["disliked"]))

        self._cache[user_id] = profile
        return profile