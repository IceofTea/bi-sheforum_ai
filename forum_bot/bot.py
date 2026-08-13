import time
import logging
import requests
from config import (
    FORUM_API_URL, BOT_USERNAME, BOT_NICKNAME, TOP_K, API_HOST, API_PORT
)
from embedding import EmbeddingEngine
from intent import IntentJudger
from generator import LLMGenerator, ForumAPI, RAGSystem
from cache import RedisCache
from agent import CampusAgent, Memory
from analytics import AnalyticsAgent
from hot_topic import HotTopicAgent
from moderation import ModeratorAgent

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("bot.log", encoding="utf-8")
    ]
)
logger = logging.getLogger("ForumBot")


class ForumBot:
    """论坛智能问答机器人 — 完整 RAG 流程"""

    def __init__(self):
        logger.info("=" * 50)
        logger.info("论坛智能问答机器人 初始化中...")
        logger.info("=" * 50)

        self.embedding = EmbeddingEngine()
        self.intent = IntentJudger()
        self.llm = LLMGenerator()
        self.forum_api = ForumAPI()
        self.rag = RAGSystem(self.embedding, self.llm, self.forum_api)
        self.cache = RedisCache()
        self.memory = Memory()
        self.agent = CampusAgent(rag=self.rag, intent=self.intent, cache=self.cache,
                                 llm=self.llm, memory=self.memory)
        self.analytics = AnalyticsAgent(llm=self.llm)
        self.hot_topic = HotTopicAgent()
        self.moderator = ModeratorAgent(llm=self.llm)

        self.bot_user = None
        self.bot_user_id = None
        self._ensure_bot_user()

        self.replied_posts = set()
        self._load_replied_posts()

        logger.info("=" * 50)
        logger.info("机器人初始化完成")
        logger.info(f" Bot用户: {self.bot_user.get('nickname', BOT_NICKNAME)} (ID: {self.bot_user_id})")
        logger.info(f" 已回复: {len(self.replied_posts)} 条")
        logger.info(f" 缓存: {'Redis' if self.cache.is_redis_connected else '内存'}")
        logger.info("=" * 50)

    # ── 用户管理 ──────────────────────────────

    def _ensure_bot_user(self):
        bot = self.forum_api.get_user_by_username(BOT_USERNAME)
        if not bot or not bot.get("id"):
            bot = self.forum_api.register_user(BOT_USERNAME, "bot_password_123", BOT_NICKNAME)
        if not bot or not bot.get("id"):
            bot = {"id": 1, "username": "admin", "nickname": "管理员"}
        self.bot_user = bot
        self.bot_user_id = bot.get("id")
        logger.info(f"Bot用户: {self.bot_user.get('nickname', BOT_NICKNAME)}, ID: {self.bot_user_id}")

    # ── 已回复管理 ────────────────────────────

    def _load_replied_posts(self):
        try:
            resp = requests.get(
                f"{FORUM_API_URL}/api/bot/replied",
                params={"userId": self.bot_user_id},
                timeout=10
            )
            if resp.status_code == 200:
                data = resp.json()
                posts = data.get("data", []) if isinstance(data, dict) else data
                self.replied_posts = {p.get("threadInfoId") for p in posts}
        except Exception:
            pass
        logger.info(f"已回复帖子数: {len(self.replied_posts)}")

    def _mark_replied(self, thread_id: int):
        self.replied_posts.add(thread_id)
        try:
            requests.post(
                f"{FORUM_API_URL}/api/bot/replied",
                json={"userId": self.bot_user_id, "threadInfoId": thread_id},
                timeout=10
            )
        except Exception:
            pass

    # ── 索引 ──────────────────────────────────

    def index_all_threads(self, force: bool = False):
        if force:
            self.embedding.reset()
            self.replied_posts.clear()

        threads = self.forum_api.get_threads(limit=500)
        logger.info(f"开始索引 {len(threads)} 条帖子...")

        for i, thread in enumerate(threads):
            self.rag.index_thread(thread)
            # 同时索引该帖子的评论（每个楼层为一个chunk）
            tid = thread.get("id")
            if tid:
                try:
                    self.rag.index_thread_comments(tid)
                except Exception as e:
                    logger.warning(f"索引帖子 {tid} 评论失败: {e}")
            if (i + 1) % 50 == 0:
                logger.info(f"已索引 {i+1}/{len(threads)} 条")

        stats = self.embedding.stats
        logger.info(f"索引完成")
        logger.info(f"  - 帖子内容向量: {stats['posts']} 条")
        logger.info(f"  - 帖子标题向量: {stats['titles']} 条")
        logger.info(f"  - 嵌入模型: {stats['backend']}/{stats['model']}")

    # ── 单帖子处理 ───────────────────────────

    def process_thread(self, thread: dict) -> bool:
        """处理单个帖子的完整 RAG 流程"""
        thread_id = thread.get("id")
        if not thread_id:
            return False
        if thread_id in self.replied_posts:
            return False

        query = thread.get("name", "").strip()
        if not query:
            return False

        logger.info(f"处理帖子 [{thread_id}]: {query[:80]}")

        # ── 第一步：检查缓存 ──
        cache_key = f"qa:{query}"
        cached = self.cache.get(cache_key)
        if cached:
            logger.info(f"  -> 命中缓存，直接回复")
            answer = cached
            success = self.forum_api.post_comment(thread_id, self.bot_user_id, answer)
            if success:
                self._mark_replied(thread_id)
                logger.info(f"  ✓ 缓存回复成功!")
                return True

        # ── 第二步：意图判断 ──
        intent = self.intent.judge(query)
        if not intent.get("is_question", False):
            logger.info(f"  -> 非提问帖 (原因: {intent.get('reason', '')})，跳过")
            self._mark_replied(thread_id)
            return False

        # ── 第三步：RAG 问答 ──
        result = self.rag.answer_question(query)

        if not result["answer"]:
            logger.info(f"  -> 无法生成回答，跳过")
            return False

        answer = result["answer"]

        # ── 缓存回答 ──
        if result["source"] in ("llm", "exact_match"):
            self.cache.set(cache_key, answer)

        # ── 第四步：发表评论 ──
        success = self.forum_api.post_comment(thread_id, self.bot_user_id, answer)
        if success:
            self._mark_replied(thread_id)
            source_tag = {"exact_match": "精确匹配", "llm": "LLM生成", "no_context": "无上下文"}.get(result["source"], result["source"])
            logger.info(f"  ✓ 回复成功! (来源: {source_tag})")
            if result.get("recommendations"):
                logger.info(f"  📌 推荐了 {len(result['recommendations'])} 个相关问题")
            return True

        logger.warning(f"  ✗ 回复失败 (API返回错误)")
        return False

    # ── 扫描回复 ─────────────────────────────

    def scan_and_reply(self, limit: int = 50):
        threads = self.forum_api.get_threads(limit=limit)
        new_posts = [t for t in threads if t.get("id") not in self.replied_posts]
        logger.info(f"发现 {len(new_posts)} 条新帖子待处理")

        replied = 0
        for thread in new_posts:
            if self.process_thread(thread):
                replied += 1
                time.sleep(1)  # 防止请求过快

        logger.info(f"本次回复 {replied} 条")
        return replied


# ═══════════════════════════════════════════════════════
# 运行模式
# ═══════════════════════════════════════════════════════

def run_bot_service():
    """持续监控模式：索引 + 每60秒扫描"""
    bot = ForumBot()
    logger.info("开始索引历史帖子...")
    bot.index_all_threads(force=False)
    logger.info("开始监控新帖子 (每60秒)...")
    while True:
        try:
            bot.scan_and_reply(limit=20)
        except Exception as e:
            logger.error(f"处理出错: {e}", exc_info=True)
        time.sleep(60)


def _warmup_cache(bot):
    """启动预热：异步填充高频问题的智能体缓存，让首批演示请求秒回"""
    import threading
    warm_qs = [
        "图书馆几点关门", "校园卡丢了怎么补办", "宿舍空调不制冷怎么报修",
        "考研英语怎么复习", "一食堂哪个窗口好吃", "二手自行车在哪买",
    ]
    def _run():
        for q in warm_qs:
            try:
                bot.agent.answer(q, user_id=1, session_id="warmup")
            except Exception:
                pass
        try:
            n = bot.cache.size if hasattr(bot.cache, "size") else None
            logger.info(f"[预热] 缓存预热完成，已缓存 {n or '?'} 条")
        except Exception:
            pass
    threading.Thread(target=_run, daemon=True).start()


def run_api_server():
    """Flask API 服务模式（含前端展示面板）"""
    import os
    from flask import Flask, request, jsonify, send_from_directory
    from flask_cors import CORS

    web_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "web")
    app = Flask(__name__)
    CORS(app)

    # 前端展示面板（供演示）：http://localhost:5000/
    if os.path.isdir(web_dir):
        @app.route("/")
        def web_index():
            return send_from_directory(web_dir, "index.html")

        @app.route("/<path:p>")
        def web_files(p):
            return send_from_directory(web_dir, p)

    bot = ForumBot()

    @app.route("/api/bot/status", methods=["GET"])
    def status():
        return jsonify({
            "status": "ok",
            "replied_count": len(bot.replied_posts),
            "bot_user": bot.bot_user,
            "cache_redis": bot.cache.is_redis_connected,
            "embedding_stats": bot.embedding.stats
        })

    @app.route("/api/bot/index", methods=["POST"])
    def index():
        force = request.json.get("force", False) if request.json else False
        bot.index_all_threads(force=force)
        return jsonify({"status": "ok", "message": "索引完成", "stats": bot.embedding.stats})

    @app.route("/api/bot/reply", methods=["POST"])
    def reply():
        thread_id = request.json.get("threadId")
        if not thread_id:
            return jsonify({"error": "缺少threadId"}), 400
        thread = bot.forum_api.get_thread(thread_id)
        if not thread or not thread.get("id"):
            return jsonify({"error": "帖子不存在"}), 404
        success = bot.process_thread(thread)
        if success:
            return jsonify({"status": "ok", "message": "回复成功"})
        return jsonify({"error": "回复失败或已回复"}), 400

    @app.route("/api/bot/search", methods=["GET"])
    def search():
        query = request.args.get("q", "")
        if not query:
            return jsonify({"error": "缺少查询"}), 400
        results = bot.embedding.search(query, top_k=5)
        return jsonify({"status": "ok", "results": results})

    @app.route("/api/bot/ask", methods=["POST"])
    def ask():
        query = request.json.get("query", "")
        if not query:
            return jsonify({"error": "缺少问题"}), 400
        result = bot.rag.answer_question(query)
        return jsonify({
            "status": "ok",
            "answer": result["answer"],
            "source": result["source"],
            "similarity": result["similarity"],
            "recommendations": len(result.get("recommendations", []))
        })

    @app.route("/api/bot/ask/agent/stream", methods=["POST"])
    def ask_agent_stream():
        """智能体入口（SSE 流式）：实时推送推理轨迹，最后推送最终结果"""
        from flask import Response
        import queue, json as _json, threading as _threading
        payload = request.json or {}
        query = payload.get("query", "")
        user_id = payload.get("userId")
        session_id = payload.get("sessionId")
        if not query:
            return jsonify({"error": "缺少问题"}), 400

        q = queue.Queue()

        def worker():
            def stage(msg):
                q.put({"type": "stage", "text": msg})
            try:
                result = bot.agent.answer(query, user_id=user_id, session_id=session_id, on_stage=stage)
                q.put({"type": "done", "result": result})
            except Exception as e:
                q.put({"type": "error", "message": str(e)})

        _threading.Thread(target=worker, daemon=True).start()

        def generate():
            yield "retry: 2000\n\n"
            while True:
                try:
                    item = q.get(timeout=120)
                except queue.Empty:
                    yield "event: error\ndata: timeout\n\n"
                    break
                if item["type"] == "stage":
                    yield f"event: stage\ndata: {_json.dumps(item['text'], ensure_ascii=False)}\n\n"
                elif item["type"] == "done":
                    yield f"event: done\ndata: {_json.dumps({'status': 'ok', **item['result']}, ensure_ascii=False)}\n\n"
                    break
                elif item["type"] == "error":
                    yield f"event: error\ndata: {_json.dumps(item['message'], ensure_ascii=False)}\n\n"
                    break

        return Response(generate(), mimetype="text/event-stream")

    @app.route("/api/bot/ask/agent", methods=["POST"])
    def ask_agent():
        """智能体入口：多智能体协作 + 个性化 + 会话记忆"""
        payload = request.json or {}
        query = payload.get("query", "")
        user_id = payload.get("userId")
        session_id = payload.get("sessionId")
        use_critique = payload.get("useCritique")
        if not query:
            return jsonify({"error": "缺少问题"}), 400
        result = bot.agent.answer(query, user_id=user_id, session_id=session_id, use_critique=use_critique)
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/ask/analytics", methods=["POST"])
    def ask_analytics():
        """数据分析入口（含热点话题）"""
        payload = request.json or {}
        question = payload.get("query", "请分析论坛整体数据情况")
        result = bot.analytics.analyze(question)
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/hot", methods=["GET"])
    def hot():
        """热点话题榜单（前端展示）"""
        try:
            result = bot.hot_topic.detect()
            return jsonify({"status": "ok", **result})
        except Exception as e:
            return jsonify({"status": "error", "message": str(e)}), 500

    @app.route("/api/bot/feedback", methods=["POST"])
    def feedback():
        """用户反馈（点赞/踩），驱动个性化"""
        payload = request.json or {}
        user_id = payload.get("userId")
        query = payload.get("query", "")
        liked = payload.get("liked", True)
        if user_id is None or not query:
            return jsonify({"error": "参数不完整"}), 400
        bot.agent.feedback(user_id, query, bool(liked))
        return jsonify({"status": "ok", "message": "已记录反馈"})

    @app.route("/api/bot/emotion", methods=["POST"])
    def emotion():
        """情绪与意图预判（零成本规则预判）"""
        payload = request.json or {}
        text = payload.get("text", "")
        if not text:
            return jsonify({"error": "缺少文本"}), 400
        from emotion import predict as emo_predict
        result = emo_predict(text, with_llm=bool(payload.get("withLlm", False)))
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/moderation", methods=["POST"])
    def moderation():
        """内容安全审核（单条文本）"""
        payload = request.json or {}
        text = payload.get("text", "")
        if not text:
            return jsonify({"error": "缺少文本"}), 400
        result = bot.moderator.check(text, with_llm=True)
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/moderation/scan", methods=["GET"])
    def moderation_scan():
        """内容安全巡检（扫描论坛帖子）"""
        result = bot.moderator.scan_posts(limit=int(request.args.get("limit", 10)))
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/trace", methods=["POST"])
    def ask_trace():
        """智能体 + 推理轨迹（供可解释性演示）"""
        payload = request.json or {}
        query = payload.get("query", "")
        if not query:
            return jsonify({"error": "缺少问题"}), 400
        result = bot.agent.answer(query, user_id=payload.get("userId"),
                                  session_id=payload.get("sessionId"))
        return jsonify({"status": "ok", **result})

    @app.route("/api/bot/ask/async", methods=["POST"])
    def ask_async():
        query = request.json.get("query", "")
        if not query:
            return jsonify({"error": "缺少问题"}), 400
        from generator import AsyncRAGSystem
        async_rag = AsyncRAGSystem(bot.rag)
        result = async_rag.answer_question_async(query)
        return jsonify({
            "status": "ok",
            "answer": result["answer"],
            "source": result["source"],
            "similarity": result["similarity"],
            "recommendations": len(result.get("recommendations", []))
        })

    @app.route("/api/bot/clear_cache", methods=["POST"])
    def clear_cache():
        bot.cache.clear()
        return jsonify({"status": "ok", "message": "缓存已清空"})

    @app.route("/api/bot/stats", methods=["GET"])
    def stats():
        return jsonify({
            "embedding": bot.embedding.stats,
            "replied": len(bot.replied_posts),
            "cache": "redis" if bot.cache.is_redis_connected else "memory"
        })

    logger.info(f"API 服务启动在 {API_HOST}:{API_PORT}")
    _warmup_cache(bot)
    app.run(host=API_HOST, port=API_PORT, debug=False)


if __name__ == "__main__":
    import sys
    if len(sys.argv) < 2:
        print("Usage: python bot.py [api|scan|index]")
        print("  api   - 启动API服务 (推荐：持续监控)")
        print("  scan  - 执行一次扫描回复")
        print("  index - 重建向量索引")
        sys.exit(1)

    mode = sys.argv[1]
    if mode == "api":
        run_api_server()
    elif mode == "scan":
        bot = ForumBot()
        bot.scan_and_reply(limit=50)
    elif mode == "index":
        bot = ForumBot()
        bot.index_all_threads(force=True)
    else:
        print(f"未知模式: {mode}")
