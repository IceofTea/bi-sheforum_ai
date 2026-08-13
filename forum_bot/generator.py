import asyncio
import aiohttp
import requests
from config import (
    LLM_BACKEND, LLM_MODEL, LLM_API_KEY, LLM_BASE_URL,
    LLM_OLLAMA_MODEL, OLLAMA_BASE_URL, FORUM_API_URL,
    MAX_CONTEXT_LENGTH, TOP_K, SIMILARITY_THRESHOLD,
    RECOMMEND_TOP_K, RECOMMEND_THRESHOLD, FORUM_FRONTEND_URL
)


# ═══════════════════════════════════════════════════════
# LLM 生成器（双后端：本地 Ollama / 云端 OpenAI 兼容 API）
# ═══════════════════════════════════════════════════════

SYSTEM_PROMPT_BASE = """你是一个校园论坛的智能问答助手。你的回答必须基于提供的参考资料。

回答要求：
1. 只基于给定的上下文回答，不要编造信息
2. 如果无法从参考资料中找到答案，请明确说明"根据当前检索内容，无法给出准确答复"
3. 回答要简洁明了，语言要友好
4. 如果有相关链接，可以在回答中提及"""

PERSONA_INSTRUCTION = """
【用户画像（可参考以提供个性化回答，但不得编造事实）】
用户昵称：{nickname}
用户等级：Lv.{level}（经验:{exp}分）
兴趣方向：{interests}
近期行为：{behaviors}
"""


class LLMGenerator:
    """大模型生成器，支持本地 Ollama 与云端 API 双后端

    - 默认使用本地 Ollama（零成本、隐私、离线可用），适合参赛演示
    - 配置 LLM_BACKEND=api 可切换回 OpenAI 兼容云端接口
    """

    def __init__(self, model: str = None, api_key: str = None, base_url: str = None,
                 backend: str = None, ollama_model: str = None):
        self.backend = backend or LLM_BACKEND
        self.model = model or LLM_MODEL
        self.api_key = api_key or LLM_API_KEY
        self.base_url = base_url or LLM_BASE_URL
        self.ollama_model = ollama_model or LLM_OLLAMA_MODEL
        self.ollama_base_url = OLLAMA_BASE_URL

    @property
    def available(self) -> bool:
        """当前后端是否可用"""
        return self.backend == "ollama" or bool(self.api_key)

    def _chat(self, system_prompt: str, prompt: str, max_tokens: int = 500, temperature: float = 0.7) -> str:
        """统一的 LLM Chat 调用（自动选择后端）"""
        if self.backend == "ollama":
            return self._chat_ollama(system_prompt, prompt, max_tokens=max_tokens, temperature=temperature)
        return self._chat_api(system_prompt, prompt, max_tokens=max_tokens, temperature=temperature)

    def _chat_ollama(self, system_prompt: str, prompt: str, max_tokens: int = 500, temperature: float = 0.7) -> str:
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})
        payload = {
            "model": self.ollama_model,
            "messages": messages,
            "stream": False,
            "options": {"num_predict": max_tokens, "temperature": temperature}
        }
        try:
            resp = requests.post(f"{self.ollama_base_url}/api/chat", json=payload, timeout=90)
            if resp.status_code == 200:
                return resp.json().get("message", {}).get("content", "").strip()
            return f"生成失败: {resp.text}"
        except Exception as e:
            return f"生成出错: {str(e)}"

    def _chat_api(self, system_prompt: str, prompt: str, max_tokens: int = 500, temperature: float = 0.7) -> str:
        if not self.api_key:
            return "您好！我是校园论坛智能助手。我正在学习论坛中的帖子内容，以便更好地帮助您。如果您有校园相关的问题，欢迎随时提问！"
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": prompt}
            ],
            "max_tokens": max_tokens,
            "temperature": temperature
        }
        try:
            response = requests.post(
                f"{self.base_url}/chat/completions",
                headers=headers, json=payload, timeout=30
            )
            if response.status_code == 200:
                return response.json()["choices"][0]["message"]["content"]
            return f"生成失败: {response.text}"
        except Exception as e:
            return f"生成出错: {str(e)}"

    def generate(self, prompt: str, max_tokens: int = 500, system_prompt: str = None) -> str:
        return self._chat(system_prompt or SYSTEM_PROMPT_BASE, prompt, max_tokens=max_tokens)

    def generate_personalized(self, prompt: str, persona: dict = None, max_tokens: int = 500,
                              emotion_instruction: str = None) -> str:
        """基于用户画像的情感化应答生成（可注入情绪预判风格指令）"""
        # 情绪预判风格指令（零/低成本，让回答更贴合用户语气）
        emotion_blk = ""
        if emotion_instruction:
            emotion_blk = "\n[用户情绪感知] " + emotion_instruction

        if not persona:
            return self.generate(prompt + emotion_blk, max_tokens=max_tokens)

        interests = "、".join(persona.get("interests", [])[:5]) or "暂未标注"
        behaviors = "；".join(persona.get("behaviors", [])[:5]) or "近期活跃度一般"
        system_prompt = (
            SYSTEM_PROMPT_BASE
            + PERSONA_INSTRUCTION.format(
                nickname=persona.get("nickname", "同学"),
                level=persona.get("level", 1),
                exp=persona.get("experience", 0),
                interests=interests,
                behaviors=behaviors,
            )
            + "\n请结合用户画像，在保持准确的回答的同时适当给出贴合其兴趣/层次的建议。"
        )
        if emotion_instruction:
            system_prompt += "\n" + emotion_instruction
        return self._chat(system_prompt, prompt, max_tokens=max_tokens)

    def score_relevance(self, query: str, doc_content: str) -> float:
        """相关性评估：让 LLM 评分（0~1），用于智能体决定是否继续检索"""
        system_prompt = (
            "你是一个检索相关性评估器。根据用户查询与文档内容的相关程度打分。\n"
            "只输出一个0到1之间的小数（如0.85），不要输出其他任何文字。"
        )
        prompt = f"用户查询：{query}\n\n文档内容（截断）：{doc_content[:800]}\n\n相关性分数："
        try:
            raw = self._chat(system_prompt, prompt, max_tokens=10, temperature=0.0)
            score = float(raw.strip())
            return max(0.0, min(1.0, score))
        except Exception:
            return 0.5

    async def generate_async(self, prompt: str, max_tokens: int = 500) -> str:
        """异步生成：用线程池包装同步调用，兼容双后端"""
        import asyncio as _asyncio
        return await _asyncio.to_thread(self.generate, prompt, max_tokens)

    def generate_with_context(self, query: str, context_docs: list) -> str:
        if not context_docs:
            return self.generate(query)

        # 上下文裁剪：限制总长度，避免超长输入
        context_text = self._build_context(context_docs)

        prompt = f"""基于以下参考资料回答用户问题：

用户问题：{query}

参考资料：
{context_text}

请根据以上参考资料给出回答："""

        return self.generate(prompt)

    def _build_context(self, context_docs: list) -> str:
        parts = []
        total = 0
        for i, doc in enumerate(context_docs):
            content = doc.get("content", "")
            if total + len(content) > MAX_CONTEXT_LENGTH:
                remaining = MAX_CONTEXT_LENGTH - total
                if remaining > 50:
                    parts.append(f"[参考{i+1}] {content[:remaining]}")
                break
            parts.append(f"[参考{i+1}] {content}")
            total += len(content)
        return "\n\n".join(parts)


# ═══════════════════════════════════════════════════════
# 论坛 API 通信
# ═══════════════════════════════════════════════════════
class ForumAPI:
    def __init__(self, base_url: str = None):
        self.base_url = base_url or FORUM_API_URL

    def get_threads(self, limit: int = 100) -> list:
        try:
            resp = requests.get(f"{self.base_url}/api/threads/all", timeout=10)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", []) if isinstance(data, dict) else data
            return []
        except Exception as e:
            print(f"[ForumAPI] 获取帖子失败: {e}")
            return []

    def get_thread(self, thread_id: int) -> dict:
        try:
            resp = requests.get(
                f"{self.base_url}/api/threads/one",
                params={"id": thread_id}, timeout=10
            )
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", {}) if isinstance(data, dict) else data
            return {}
        except Exception as e:
            print(f"[ForumAPI] 获取帖子详情失败: {e}")
            return {}

    def get_comments(self, thread_id: int) -> list:
        try:
            resp = requests.get(
                f"{self.base_url}/api/comments",
                params={"threadInfoId": thread_id}, timeout=10
            )
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", []) if isinstance(data, dict) else data
            return []
        except Exception as e:
            print(f"[ForumAPI] 获取评论失败: {e}")
            return []

    def post_comment(self, thread_id: int, user_id: int, comment: str) -> bool:
        try:
            resp = requests.post(
                f"{self.base_url}/api/comments",
                json={
                    "threadInfoId": thread_id,
                    "userInfoId": user_id,
                    "comment": comment
                },
                timeout=10
            )
            return resp.status_code == 200
        except Exception as e:
            print(f"[ForumAPI] 发表评论失败: {e}")
            return False

    def get_user_by_username(self, username: str) -> dict:
        try:
            resp = requests.get(
                f"{self.base_url}/api/users/byusername",
                params={"username": username}, timeout=10
            )
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", {}) if isinstance(data, dict) else data
            return {}
        except Exception as e:
            print(f"[ForumAPI] 获取用户失败: {e}")
            return {}

    def register_user(self, username: str, password: str, nickname: str) -> dict:
        try:
            resp = requests.post(
                f"{self.base_url}/api/users/register",
                json={
                    "username": username,
                    "password": password,
                    "nickname": nickname
                },
                timeout=10
            )
            if resp.status_code in [200, 201]:
                data = resp.json()
                return data.get("data", {})
        except Exception as e:
            print(f"[ForumAPI] 注册用户失败: {e}")
        return {}


# ═══════════════════════════════════════════════════════
# RAG 系统
# ═══════════════════════════════════════════════════════
class RAGSystem:
    def __init__(self, embedding_engine=None, llm_generator=None, forum_api=None):
        from embedding import EmbeddingEngine
        self.embedding = embedding_engine or EmbeddingEngine()
        self.llm = llm_generator or LLMGenerator()
        self.forum_api = forum_api or ForumAPI()

    # ── 索引 ──────────────────────────────────

    def index_thread(self, thread: dict):
        """索引一条帖子（内容 + 标题分别存储）"""
        if not thread:
            return
        post_id = thread.get("id")
        title = thread.get("name", "") or ""
        content = thread.get("introduction", "") or ""
        writer = thread.get("writer", "") or ""
        url = f"{FORUM_FRONTEND_URL}/thread/read?id={post_id}"

        full_content = f"{title}\n{content}" if title else content
        self.embedding.add_post(
            post_id=post_id,
            content=full_content,
            title=title,
            writer=writer,
            url=url
        )

    def index_thread_comments(self, thread_id: int):
        """索引某个帖子下的所有评论（每个楼层作为一个chunk）"""
        comments = self.forum_api.get_comments(thread_id)
        for c in comments:
            cid = c.get("id")
            content = c.get("comment", "") or ""
            uid = c.get("userInfoId", 0)
            self.embedding.add_comment(
                comment_id=cid,
                thread_id=thread_id,
                content=content,
                user_id=uid
            )

    # ── 检索 ──────────────────────────────────

    def search_similar(self, query: str, top_k: int = None) -> list:
        """语义检索相关帖子内容"""
        return self.embedding.search(query, top_k=top_k or TOP_K)

    def get_recommendations(self, query: str) -> list:
        """获取关联问题推荐"""
        return self.embedding.get_related_questions(
            query,
            top_k=RECOMMEND_TOP_K,
            threshold=RECOMMEND_THRESHOLD
        )

    # ── 回答生成 ─────────────────────────────

    def answer_question(self, query: str) -> dict:
        """完整 RAG 问答流程
        返回: {"answer": str, "source": str, "similarity": float, "recommendations": list}
        """
        result = {
            "answer": "",
            "source": "none",
            "similarity": 0.0,
            "recommendations": []
        }

        if not query:
            return result

        # ── 步骤1: 高相似度直接返回 ──
        similar_posts = self.embedding.get_similar_posts(query, threshold=SIMILARITY_THRESHOLD)
        if similar_posts:
            best = similar_posts[0]
            url = best.get("metadata", {}).get("url", "")
            link_text = f"\n\n原文链接：{url}" if url else ""
            result["answer"] = (
                f"为您找到相似问题解答：\n\n"
                f"{best['content'][:500]}"
                f"{link_text}\n\n"
                f"（此回答来自历史相似帖子，相似度: {best['similarity']:.2f}）"
            )
            result["source"] = "exact_match"
            result["similarity"] = best["similarity"]
            return result

        # ── 步骤2: 检索 Top-K 上下文 ──
        context_docs = self.embedding.search(query, top_k=TOP_K)
        if not context_docs:
            result["answer"] = "抱歉，当前论坛内容库中未找到与您问题相关的信息，请尝试换个问法。"
            result["source"] = "no_context"
            return result

        result["similarity"] = context_docs[0]["similarity"]

        # ── 步骤3: LLM 生成回答 ──
        answer = self.llm.generate_with_context(query, context_docs)
        result["answer"] = answer
        result["source"] = "llm"

        # ── 步骤4: 关联问题推荐 ──
        recommendations = self.get_recommendations(query)
        if recommendations:
            rec_text = "\n\n📌 相关问题推荐："
            for idx, rec in enumerate(recommendations[:RECOMMEND_TOP_K], 1):
                meta = rec.get("metadata", {})
                url = meta.get("url", "")
                title = rec.get("content", "")
                if url:
                    rec_text += f"\n{idx}. {title}\n   链接：{url}"
                else:
                    rec_text += f"\n{idx}. {title}"
            result["answer"] += rec_text
            result["recommendations"] = recommendations

        return result


# ═══════════════════════════════════════════════════════
# 异步 RAG 支持
# ═══════════════════════════════════════════════════════
class AsyncRAGSystem:
    """异步版的 RAG 系统，利用 asyncio 实现并发调用"""

    def __init__(self, rag: RAGSystem):
        self.rag = rag
        self.loop = asyncio.new_event_loop()

    def answer_question_async(self, query: str) -> dict:
        """异步并发执行检索和生成的入口"""
        return self.loop.run_until_complete(self._answer_pipeline(query))

    async def _answer_pipeline(self, query: str) -> dict:
        result = {
            "answer": "",
            "source": "none",
            "similarity": 0.0,
            "recommendations": []
        }

        # 并行执行：高相似度检查 + 常规检索
        similar_task = asyncio.to_thread(self.rag.embedding.get_similar_posts, query, SIMILARITY_THRESHOLD)
        search_task = asyncio.to_thread(self.rag.embedding.search, query, TOP_K)

        similar_posts, context_docs = await asyncio.gather(similar_task, search_task)

        if similar_posts:
            best = similar_posts[0]
            url = best.get("metadata", {}).get("url", "")
            link_text = f"\n\n原文链接：{url}" if url else ""
            result["answer"] = (
                f"为您找到相似问题解答：\n\n{best['content'][:500]}"
                f"{link_text}\n\n（此回答来自历史相似帖子，相似度: {best['similarity']:.2f}）"
            )
            result["source"] = "exact_match"
            result["similarity"] = best["similarity"]
            return result

        if not context_docs:
            result["answer"] = "抱歉，未找到相关信息。"
            result["source"] = "no_context"
            return result

        result["similarity"] = context_docs[0]["similarity"]

        context_text = self.rag.llm._build_context(context_docs)
        prompt = f"基于以下参考资料回答用户问题：\n\n用户问题：{query}\n\n参考资料：\n{context_text}\n\n请根据以上参考资料给出回答："
        answer = await self.rag.llm.generate_async(prompt)
        result["answer"] = answer
        result["source"] = "llm"

        rec_task = asyncio.to_thread(self.rag.embedding.get_related_questions, query, RECOMMEND_TOP_K, RECOMMEND_THRESHOLD)
        recommendations = await rec_task
        if recommendations:
            rec_text = "\n\n📌 相关问题推荐："
            for idx, rec in enumerate(recommendations[:RECOMMEND_TOP_K], 1):
                url = rec.get("metadata", {}).get("url", "")
                title = rec.get("content", "")
                if url:
                    rec_text += f"\n{idx}. {title}\n   链接：{url}"
                else:
                    rec_text += f"\n{idx}. {title}"
            result["answer"] += rec_text
            result["recommendations"] = recommendations

        return result
