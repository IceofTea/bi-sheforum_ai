"""校园智脑 — 数据分析 Agent

响应"数据分析"类自然语言问题：
1. 从论坛 API 聚合统计数据（帖子量、用户量、板块热度、收藏/评论排行）
2. LLM 生成结构化分析报告
3. 支持趋势洞察与排行榜输出

用法示例：
  from analytics import AnalyticsAgent
  agent = AnalyticsAgent()
  report = agent.analyze("最近什么话题最热")
  → {"answer": "...", "data": {...}, "source": "analytics_llm"}
"""
import time
import logging

from config import FORUM_API_URL, ANALYTICS_TOP_K, HOT_TOPIC_MIN_COUNT
from hot_topic import HotTopicAgent

logger = logging.getLogger("AnalyticsAgent")

ANALYTICS_SYSTEM_PROMPT = """你是"校园智脑"的数据分析助手。基于给定的论坛统计数据，输出一段精炼的中文分析报告。

要求：
1. 先给出核心结论（1-2 句）
2. 再给出数据支撑（引用具体数字）
3. 最后给出 1 条可执行的建议
4. 语气专业、面向校园用户
"""


class AnalyticsAgent:
    """论坛数据分析智能体"""

    def __init__(self, api_base: str = None, llm=None):
        import requests
        from generator import LLMGenerator
        self.api_base = (api_base or FORUM_API_URL).rstrip("/")
        self.requests = requests
        self.llm = llm or LLMGenerator()
        self.hot_topic = HotTopicAgent(self.api_base)

    # ── 数据采集 ──────────────────────────────

    def _get(self, path: str, **params):
        try:
            resp = self.requests.get(f"{self.api_base}{path}", params=params, timeout=10)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", data) if isinstance(data, dict) else data
        except Exception as e:
            logger.warning(f"[Analytics] 请求 {path} 失败: {e}")
        return None

    def collect_stats(self) -> dict:
        """聚合论坛基础统计数据 + 热点话题"""
        stats = {"fetched": time.strftime("%Y-%m-%d %H:%M")}

        threads = self._get("/api/threads/all") or []
        if isinstance(threads, dict):
            threads = threads.get("data", []) or threads.get("list", []) or []
        threads = list(threads) if isinstance(threads, (list, tuple)) else []

        users = self._get("/api/users/page", pageSize=1, pageIndex=1)
        stats["total_threads"] = len(threads)
        stats["total_users"] = (users.get("total") if isinstance(users, dict) else None) or "未知"

        # 板块热度统计
        from collections import Counter
        sort_counter = Counter()
        for t in threads:
            sort_obj = t.get("threadsSort")
            if isinstance(sort_obj, dict):
                sid = sort_obj.get("name") or sort_obj.get("id")
            else:
                sid = t.get("threadsSortId") or t.get("sortId")
            if sid is not None:
                sort_counter[sid] += 1
        stats["hot_sorts"] = sort_counter.most_common(ANALYTICS_TOP_K)

        # 发帖用户排行
        writer_counter = Counter()
        for t in threads:
            w = t.get("writer") or "匿名"
            if w:
                writer_counter[w] += 1
        stats["top_writers"] = writer_counter.most_common(ANALYTICS_TOP_K)

        # 最近 20 条帖子（用于趋势）
        stats["recent_threads"] = threads[:20]

        # 热点话题识别（创新点）
        try:
            stats["hot_topics"] = self.hot_topic.detect(top_k=ANALYTICS_TOP_K)
        except Exception as e:
            logger.warning(f"[Analytics] 热点识别失败: {e}")
            stats["hot_topics"] = {"hot_topics": []}
        return stats

    # ── 分析生成 ──────────────────────────────

    def analyze(self, question: str = "") -> dict:
        """入口：采集数据 + LLM 生成报告，返回结构化结果"""
        start_t = time.perf_counter()
        try:
            data = self.collect_stats()
        except Exception as e:
            logger.error(f"[Analytics] 数据采集失败: {e}")
            return {"answer": f"数据采集失败：{e}", "data": {}, "source": "error"}

        # 组装数据文本
        def fmt_sorts(items):
            return "\n".join(f"  板块#{k}: {v} 帖" for k, v in items) or "  （无）"

        def fmt_writers(items):
            return "\n".join(f"  {w}: {c} 帖" for w, c in items) or "  （无）"

        data_text = f"""论坛统计数据（采集于 {data['fetched']}）：
- 帖子总数：{data['total_threads']}
- 用户总数：{data['total_users']}
- 发帖量 Top 板块：
{fmt_sorts(data['hot_sorts'])}
- 发帖活跃用户 Top：
{fmt_writers(data['top_writers'])}
- 最近部分帖子标题：
{self._fmt_titles(data['recent_threads'])}
"""

        user_question = f"用户问题：{question}\n\n" if question else ""
        prompt = f"{user_question}请基于以下数据输出分析报告：\n\n{data_text}"
        try:
            answer = self.llm.generate(prompt, max_tokens=500, system_prompt=ANALYTICS_SYSTEM_PROMPT)
        except Exception as e:
            answer = f"（LLM 分析失败：{e}）"
            logger.error(f"[Analytics] LLM 失败: {e}")

        return {
            "answer": answer,
            "data": {
                "total_threads": data["total_threads"],
                "total_users": data["total_users"],
                "hot_sorts": data["hot_sorts"],
                "top_writers": data["top_writers"],
                "hot_topics": data.get("hot_topics", {}).get("hot_topics", []),
            },
            "source": "analytics_llm",
            "time_ms": round((time.perf_counter() - start_t) * 1000, 1),
        }

    def _fmt_titles(self, threads: list) -> str:
        if not threads:
            return "  （无）"
        return "\n".join("  " + (t.get("name") or "未命名")[:40] for t in threads[:8])