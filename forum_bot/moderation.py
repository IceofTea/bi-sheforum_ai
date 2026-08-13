"""校园智脑 — 内容安全审核智能体（ModeratorAgent）

职责：对论坛帖子/评论文本进行多级内容安全检测，输出风险等级与处理建议。
增强智能体的"行动面"——不仅能问答，还能守护社区内容安全。

检测维度：
  1. 广告营销（联系方式、二维码、外链）
  2. 辱骂攻击（人身攻击、歧视性用语）
  3. 色情低俗
  4. 诈骗风险（转账、汇款、代办）
  5. 隐私泄露（身份证号、手机号、住址）

实现：规则引擎（正则+词典）一级判定 → LLM 二级复核置信度，
两通道综合给出 final 结论，兼顾准确率与召回率。
"""
import re
import logging

from config import FORUM_API_URL

logger = logging.getLogger("ModeratorAgent")

# ═══════════════════════════════════════════
# 规则引擎（第一级：快速、确定、可解释）
# ═══════════════════════════════════════════
PHONE_RE = re.compile(r"1[3-9]\d{9}")
QQ_RE = re.compile(r"[QＱ]{1,2}[:：\s]*\d{5,12}")
WECHAT_RE = re.compile(r"(微信|vx|VX|wechat)[:：\s]*[a-zA-Z0-9_-]{5,20}")
IDCARD_RE = re.compile(r"\d{17}[\dXx]")
URL_RE = re.compile(r"(https?://|www\.)\S+")
BANK_RE = re.compile(r"(转账|汇款|打钱|刷单|垫付|兼职日结|稳赚|高回报|包过|代写代考)")

RULE_ADVERT = ["加微信", "加vx", "加VX", "联系我", "私聊我", "点击链接", "扫码", "优惠", "特价", "促销",
               "出售", "低价", "代理", "返利", "客服", "客服微信", "店铺", "下单"]
RULE_INSULT = ["傻逼", "煞笔", "智障", "脑残", "废物", "垃圾人", "狗东西", "去死", "贱人",
               "白痴", "滚蛋", "尼玛", "你妈", "操你"]
RULE_PORN = ["约炮", "援交", "色情", "裸聊", "一夜情", "成人用品", "迷药", "催情"]
RULE_FRAUD = ["银行卡", "密码", "验证码", "转账", "汇款", "刷单", "代考", "包过", "内部渠道", "稳赚不赔"]
RULE_PRIVACY = ["身份证", "家庭住址", "门牌号", "学号", "工号"]


def _rule_scan(text: str) -> dict:
    """返回规则命中明细 {category: [命中词/样例]}"""
    hits = {}
    t = text or ""

    if PHONE_RE.search(t) or QQ_RE.search(t) or WECHAT_RE.search(t) or URL_RE.search(t):
        hits.setdefault("广告营销/联系方式", []).append("检测到手机号/QQ/微信/外链")
    for w in RULE_ADVERT:
        if w in t:
            hits.setdefault("广告营销", []).append(w)
    for w in RULE_INSULT:
        if w in t:
            hits.setdefault("辱骂攻击", []).append(w)
    for w in RULE_PORN:
        if w in t:
            hits.setdefault("色情低俗", []).append(w)
    for w in RULE_FRAUD:
        if w in t:
            hits.setdefault("诈骗风险", []).append(w)
    if IDCARD_RE.search(t):
        hits.setdefault("隐私泄露", []).append("检测到身份证号")
    for w in RULE_PRIVACY:
        if w in t:
            hits.setdefault("隐私泄露", []).append(w)

    return hits


RISK_LEVEL = {0: "正常", 1: "低风险", 2: "高风险"}


class ModeratorAgent:
    """内容安全审核智能体"""

    def __init__(self, llm=None, api_base=None):
        self.llm = llm
        self.api_base = (api_base or FORUM_API_URL).rstrip("/")
        self._cache = {}

    def check(self, text: str, with_llm: bool = True) -> dict:
        """审核一段文本，返回结构化结论"""
        text = (text or "").strip()
        if not text:
            return {"ok": True, "level": 0, "level_name": "正常", "categories": [], "details": [], "advice": "空内容", "source": "rule"}

        cache_key = text[:80]
        if cache_key in self._cache:
            return self._cache[cache_key]

        hits = _rule_scan(text)
        if not hits:
            result = {"ok": True, "level": 0, "level_name": "正常", "categories": [],
                      "details": ["未命中任何敏感规则"], "advice": "内容安全，无需处理", "source": "rule"}
            self._cache[cache_key] = result
            return result

        # 规则已命中：再交给 LLM 复核置信度与处理建议（可解释）
        verdict = None
        if with_llm and self.llm:
            try:
                cats = "、".join(hits.keys())
                prompt = (
                    f"以下帖子被规则引擎标记为潜在违规（类别：{cats}）：\n"
                    f"内容：{text[:300]}\n\n"
                    f"请输出审核结论 JSON：\n"
                    f'{{"confirm": true/false, "risk": "低"或"高", "advice": "一句话处理建议"}}'
                )
                raw = self.llm.generate(prompt, max_tokens=120,
                                        system_prompt="你是校园论坛内容安全审核员，判断是否确属违规并给出处理建议。")
                import json as _json
                s, e = raw.find("{"), raw.rfind("}") + 1
                if s >= 0 and e > s:
                    verdict = _json.loads(raw[s:e])
            except Exception as ex:
                logger.warning(f"[Moderator] LLM 复核失败: {ex}")

        level = 2 if (verdict and verdict.get("confirm") is False) else 1
        if verdict and verdict.get("risk") == "高":
            level = 2
        # 严重违规类别（辱骂/诈骗/隐私/色情）直接判高风险，无需 LLM 确认
        SEVERE = {"辱骂攻击", "诈骗风险", "隐私泄露", "色情低俗"}
        if SEVERE & set(hits.keys()):
            level = 2
        advice = (verdict or {}).get("advice") or "建议人工复核后删除或提醒修改"
        details = []
        for cat, words in hits.items():
            details.append(f"{cat}（命中：{'、'.join(words[:3])}）")

        result = {
            "ok": level == 0,
            "level": level,
            "level_name": RISK_LEVEL[level],
            "categories": list(hits.keys()),
            "details": details,
            "advice": advice,
            "source": "rule+llm" if verdict else "rule",
            "llm_confirm": verdict.get("confirm") if verdict else None,
        }
        self._cache[cache_key] = result
        return result

    def scan_posts(self, threads: list = None, limit: int = 10) -> dict:
        """扫描一批帖子，返回整体健康度报告"""
        import requests
        if threads is None:
            try:
                resp = requests.get(f"{self.api_base}/api/threads/all", timeout=8)
                data = resp.json()
                threads = data.get("data", []) if isinstance(data, dict) else data
            except Exception as e:
                logger.warning(f"[Moderator] 拉取帖子失败: {e}")
                threads = []
        threads = [t for t in (threads or []) if isinstance(t, dict)][:limit]

        flagged = []
        for t in threads:
            title = t.get("name") or ""
            content = t.get("introduction") or ""
            combined = title + " " + content
            res = self.check(combined, with_llm=False)  # 批量扫描用规则，快
            if not res["ok"]:
                flagged.append({"id": t.get("id"), "title": title[:40], **res})

        total = len(threads)
        return {
            "scanned": total,
            "flagged": len(flagged),
            "health": round((total - len(flagged)) / max(total, 1) * 100, 1),
            "flagged_items": flagged,
        }
