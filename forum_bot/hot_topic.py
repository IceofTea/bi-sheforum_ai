"""校园智脑 — 热点话题识别智能体

创新点：从论坛帖子流中自动发现热点话题
- 基于板块热度 + 发帖频率 + 关键词聚类
- 输出结构化热点榜单，供数据分析与前端展示
"""
import re
import logging
from collections import Counter

from config import FORUM_API_URL, ANALYTICS_TOP_K, HOT_TOPIC_MIN_COUNT

logger = logging.getLogger("HotTopicAgent")

# 校园常见话题词典（用于轻量关键词聚类）
TOPIC_KEYWORDS = {
    "考研": ["考研", "上岸", "初试", "复试", "考研英语", "数学", "政治"],
    "四六级": ["四六级", "CET", "英语四级", "英语六级", "考级"],
    "选课": ["选课", "通识课", "选修", "抢课", "课程"],
    "食堂": ["食堂", "餐厅", "窗口", "好吃", "美食", "外卖"],
    "宿舍": ["宿舍", "寝室", "空调", "报修", "维修", "水电"],
    "图书馆": ["图书馆", "自习", "闭馆", "占座", "座位"],
    "失物招领": ["失物", "丢了", "捡到", "寻物", "招领", "钱包", "学生卡"],
    "校园卡": ["校园卡", "一卡通", "补办", "充值"],
    "二手": ["二手", "出售", "转让", "闲置", "出", "卖"],
    "求职": ["实习", "校招", "offer", "简历", "求职", "就业"],
    "竞赛": ["竞赛", "比赛", "大创", "建模", "挑战杯"],
    "社团": ["社团", "招新", "活动", "志愿者"],
}


class HotTopicAgent:
    """热点话题识别"""

    def __init__(self, api_base: str = None):
        import requests
        self.api_base = (api_base or FORUM_API_URL).rstrip("/")
        self.requests = requests

    def _get(self, path: str, **params):
        try:
            resp = self.requests.get(f"{self.api_base}{path}", params=params, timeout=10)
            if resp.status_code == 200:
                data = resp.json()
                return data.get("data", data) if isinstance(data, dict) else data
        except Exception as e:
            logger.warning(f"[HotTopic] 请求 {path} 失败: {e}")
        return None

    def detect(self, top_k: int = None) -> dict:
        """识别当前热点话题，返回结构化的榜单"""
        top_k = top_k or ANALYTICS_TOP_K
        threads = self._get("/api/threads/all") or []
        if isinstance(threads, dict):
            threads = threads.get("data", []) or threads.get("list", []) or []
        threads = [t for t in threads if isinstance(t, dict)]

        # 1. 话题关键词聚类
        topic_counter = Counter()
        topic_threads = {}
        for t in threads:
            title = t.get("name") or ""
            content = (t.get("introduction") or "")[:60]
            text = title + " " + content
            matched = None
            for topic, words in TOPIC_KEYWORDS.items():
                if any(w in text for w in words):
                    matched = topic
                    break
            if matched:
                topic_counter[matched] += 1
                topic_threads.setdefault(matched, []).append(t)

        topics = []
        for topic, count in topic_counter.most_common(top_k):
            if count < HOT_TOPIC_MIN_COUNT:
                continue
            samples = [t.get("name") for t in topic_threads.get(topic, [])[:3]]
            topics.append({
                "topic": topic,
                "count": count,
                "samples": samples,
                "heat": round(count / max(len(threads), 1) * 100, 1),
            })

        # 2. 板块热度（附带）
        sort_counter = Counter()
        for t in threads:
            sort_obj = t.get("threadsSort")
            if isinstance(sort_obj, dict):
                sid = sort_obj.get("name") or sort_obj.get("id")
            else:
                sid = t.get("threadsSortId") or t.get("sortId")
            if sid is not None:
                sort_counter[sid] += 1

        total_users = None
        try:
            users = self._get("/api/users/page", pageSize=1, pageIndex=1) or {}
            if isinstance(users, dict):
                total_users = users.get("total")
                if total_users is None and isinstance(users.get("data"), dict):
                    total_users = users["data"].get("total")
        except Exception:
            pass

        return {
            "total_threads": len(threads),
            "total_users": total_users,
            "hot_topics": topics,
            "hot_sorts": sort_counter.most_common(top_k),
            "fetched": __import__("time").strftime("%Y-%m-%d %H:%M"),
        }