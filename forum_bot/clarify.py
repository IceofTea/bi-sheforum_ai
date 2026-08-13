"""主动式澄清与引导模块（Proactive Clarify & Guidance）

设计理念：把问答客服从「被动回答」升级为「主动引导」。
  传统 RAG：用户问什么 → 答什么（答不上就拒答）
  本模块：   用户提问 → 主动分析信息缺口 → 引导补充 / 给出方向选项 / 主动延伸话题

核心能力：
  1. 主动识别：结合情绪预判(emotion)与意图(intent)，检测问题是否模糊、信息是否不足
  2. 引导式澄清：不只问一句，而是给出「方向 + 选项」，让用户能快速点选/补充
  3. 回答后引导：在回答末尾主动追加延伸话题与下一步建议，形成对话闭环
  4. 紧急行动引导：高紧急问题自动附加「行动指引 + 联系方式」板块
  5. 场景无关：纯输入文本 → 结构化引导建议，可被任意问答/客服系统复用（模块化拼接）

模块化设计（本模块只依赖 emotion/intent 的输出结构，不依赖任何具体业务）：
  - ClarifyGuide(emotion_predictor, intent_judger) 构造，替换即可换场景
  - 全部规则零成本；可配 CLARIFY_USE_LLM=true 启用 LLM 生成更自然的追问
"""
import json
import logging

logger = logging.getLogger("ClarifyGuide")

from scene_config import (
    SCENE_NAME, MISSING_SIGNALS, FOLLOWUP_HINTS, URGENT_ACTION,
    CLARIFY_TEMPLATE, CLARIFY_QUESTIONS, CLARIFY_OPTIONS,
    CLARIFY_DEFAULT_QUESTION, CLARIFY_DEFAULT_OPTIONS, CLARIFY_LLM_PROMPT,
)

try:
    from config import CLARIFY_USE_LLM, OLLAMA_BASE_URL, INTENT_MODEL
except ImportError:
    CLARIFY_USE_LLM = False
    OLLAMA_BASE_URL = "http://localhost:11434"
    INTENT_MODEL = "deepseek-r1:1.5b"

import requests

# 注：以下场景资源已统一移至 scene_config.py（场景配置中心）。
# 换场景（如电商/政务/医疗）只需修改 scene_config.py 一处即可复用本引擎。


class ClarifyGuide:
    """主动式澄清与引导器：识别信息缺口 → 生成引导追问 → 回答后延伸引导"""

    def __init__(self, emotion_predictor=None, intent_judger=None):
        self.emotion = emotion_predictor
        self.intent = intent_judger
        self._ollama = self._check_ollama()

    def _check_ollama(self):
        try:
            resp = requests.get(f"{OLLAMA_BASE_URL}/api/tags", timeout=5)
            return any(INTENT_MODEL in m.get("name", "") for m in resp.json().get("models", []))
        except Exception:
            return False

    # ── 主动识别信息缺口 ──────────────────────────

    def need_clarify(self, query: str, emotion_pred: dict = None) -> dict:
        """主动判断：用户问题是否需要引导补充信息
        返回: {"need": bool, "reason": str, "intent": str}
        """
        query = (query or "").strip()
        if not query:
            return {"need": True, "reason": "空输入", "intent": "咨询"}

        intent_cat = (emotion_pred or {}).get("intent_category", "咨询")
        signals = MISSING_SIGNALS.get(intent_cat, [])

        # 规则1：包含信息缺口关键词 → 需要引导
        for sig in signals:
            if sig in query:
                return {"need": True, "reason": f"缺少关键信息（{sig}）", "intent": intent_cat}

        # 规则2：过短且无语义对象 → 需要引导
        if len(query) < 6 and not any(w in query for w in ["怎么", "如何", "什么", "多少"]):
            return {"need": True, "reason": "问题过于简略", "intent": intent_cat}

        # 规则3：报修/失物类无位置信息 → 默认需要引导（此类问题位置是关键）
        if intent_cat in ("报修", "失物") and not any(k in query for k in signals):
            return {"need": True, "reason": f"{intent_cat}类问题缺少位置信息", "intent": intent_cat}

        return {"need": False, "reason": "", "intent": intent_cat}

    # ── 生成引导追问（选项式） ────────────────────

    def clarify(self, query: str, emotion_pred: dict = None) -> dict:
        """生成主动引导追问
        返回: {"text": str, "question": str, "options": [str], "source": "rule"|"llm"}
        """
        need = self.need_clarify(query, emotion_pred)
        intent_cat = need.get("intent", "咨询")
        default_options = CLARIFY_OPTIONS.get(intent_cat, CLARIFY_DEFAULT_OPTIONS)

        # 尝试 LLM 生成更自然的引导（可选，默认规则零成本）
        if CLARIFY_USE_LLM and self._ollama:
            llm_res = self._llm_clarify(query)
            if llm_res:
                return {**llm_res, "source": "llm"}

        question = CLARIFY_QUESTIONS.get(intent_cat, CLARIFY_DEFAULT_QUESTION)

        return {
            "text": CLARIFY_TEMPLATE.format(question=question, options="\n".join(f"· {o}" for o in default_options)),
            "question": question,
            "options": default_options,
            "source": "rule",
        }

    # ── 回答后主动引导（延伸话题 + 下一步建议） ────

    def followup(self, query: str, emotion_pred: dict = None, answer: str = "") -> str:
        """生成回答末尾的主动引导文本（追问式延伸，让对话延续）
        结合紧急度附加行动指引板块
        """
        intent_cat = (emotion_pred or {}).get("intent_category", "咨询")
        urgency = (emotion_pred or {}).get("urgency", "低")

        parts = []
        # 紧急度行动指引
        if urgency in ("高", "中") and intent_cat in URGENT_ACTION:
            parts.append(f"⚡ 行动指引：{URGENT_ACTION[intent_cat]}")

        # 延伸话题引导
        hints = FOLLOWUP_HINTS.get(intent_cat, ["相关问题"])
        if hints:
            parts.append("💬 您可以继续问我：\n" + "\n".join(f"· {h}" for h in hints[:3]))

        return "\n\n".join(parts) if parts else ""

    # ── LLM 兜底 ─────────────────────────────────

    def _llm_clarify(self, query: str) -> dict:
        try:
            resp = requests.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={"model": INTENT_MODEL, "prompt": f"用户问题：{query}\n\n{CLARIFY_LLM_PROMPT}",
                      "stream": False, "options": {"temperature": 0.2, "num_predict": 200}},
                timeout=30,
            )
            if resp.status_code == 200:
                raw = resp.json().get("response", "")
                start, end = raw.find("{"), raw.rfind("}") + 1
                if start >= 0 and end > start:
                    data = json.loads(raw[start:end])
                    q = data.get("question", "")
                    opts = [o for o in data.get("options", []) if o][:4]
                    if q:
                        return {
                            "text": CLARIFY_TEMPLATE.format(question=q, options="\n".join(f"· {o}" for o in opts or CLARIFY_DEFAULT_OPTIONS)),
                            "question": q,
                            "options": opts,
                        }
        except Exception as e:
            logger.warning(f"[Clarify] LLM 引导生成失败: {e}")
        return {}
