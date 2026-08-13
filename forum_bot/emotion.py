"""情绪与意图预判模块（Sentiment & Intent Prelude）

在进入完整多智能体编排之前，先对用户输入做一次"零成本预判"：
  1. 情感极性（积极 / 中性 / 消极）与情绪标签（急切 / 愤怒 / 焦虑 / 开心 …）
  2. 意图类别（咨询 / 求助 / 报修 / 投诉 / 交易 / 失物 / 分享 / 闲聊 …）
  3. 紧急程度（低 / 中 / 高）——用于触发快速通道或人工介入

设计目标：
  - 默认纯规则（情感/意图词典 + 正则），零 LLM 调用、毫秒级、可离线；
  - 可选 LLM 兜底（配置 EMOTION_USE_LLM=true 时对规则置信度不足的输入做增强），
    保证演示时"低研发成本"与"高准确率"可兼顾。

创新点价值：
  - 系统能"读懂语气"：对负面情绪（投诉/焦虑）的回答自动更温和、追加安抚与行动指引；
  - 对高紧急度问题（报修/丢失）优先给出数字联系方式与线下指引；
  - 全链路可解释：预判结论写入推理轨迹，前后端可视化。
"""
import re
import json

from scene_config import (
    POS_WORDS, NEG_WORDS, EMOTION_DICT, INTENT_DICT, URGENT_HIGH, URGENT_MID,
    CLARIFY_QUESTIONS, CLARIFY_OPTIONS,
)

try:
    from config import OLLAMA_BASE_URL, INTENT_MODEL, EMOTION_USE_LLM, LLM_API_KEY, LLM_BASE_URL, LLM_MODEL
except ImportError:
    OLLAMA_BASE_URL = "http://localhost:11434"
    INTENT_MODEL = "deepseek-r1:1.5b"
    EMOTION_USE_LLM = False
    LLM_API_KEY = ""
    LLM_BASE_URL = "https://api.longcat.chat/openai/v1"
    LLM_MODEL = "LongCat-Flash-Chat"

import requests

# ═══════════════════════════════════════════════════════
# 场景资源：全部由 scene_config.py 注入（换场景只改那里）
# ═══════════════════════════════════════════════════════


class EmotionPredictor:
    """情绪与意图预判器：规则优先 + 可选 LLM 兜底"""

    def __init__(self):
        self._ollama_available = self._check_ollama()
        if EMOTION_USE_LLM and not self._ollama_available:
            print("[Emotion] LLM 增强已开启但 Ollama 不可用，将仅使用规则")

    def _check_ollama(self):
        try:
            resp = requests.get(f"{OLLAMA_BASE_URL}/api/tags", timeout=5)
            models = [m["name"] for m in resp.json().get("models", [])]
            return any(INTENT_MODEL in m for m in models)
        except Exception:
            return False

    def predict(self, text: str, with_llm: bool = False) -> dict:
        """输入用户文本，输出情感 + 意图 + 紧急度预判"""
        text = (text or "").strip()
        if not text:
            return {"emotion": "平静", "sentiment": "neutral", "confidence": 0.0,
                    "intent_category": "闲聊", "urgency": "低", "source": "rule"}

        result = self._rule_predict(text)

        # 可选 LLM 兜底：当规则置信度低且用户显式要求（演示增强）时
        if with_llm and result["confidence"] < 0.5 and self._ollama_available:
            llm_result = self._llm_enhance(text)
            if llm_result:
                return {**result, **llm_result, "source": "rule+llm"}

        return result

    def _rule_predict(self, text):
        pos = sum(1 for w in POS_WORDS if w in text)
        neg = sum(1 for w in NEG_WORDS if w in text)

        if neg > pos:
            sentiment = "negative"
            emotion = self._match_emotion(text, ("愤怒", "焦虑", "急切", "求助"))
        elif pos > neg:
            sentiment = "positive"
            emotion = self._match_emotion(text, ("满意", "开心", "好奇"))
        else:
            sentiment = "neutral"
            emotion = self._match_emotion(text, ("求助", "好奇", "急切", "平静"))

        intent_category = self._match_intent(text)

        # 紧急度
        urgency = "低"
        if any(w in text for w in URGENT_HIGH):
            urgency = "高"
        elif any(w in text for w in URGENT_MID):
            urgency = "中"

        confidence = min(0.95, 0.4 + (pos + neg) * 0.15 + (0.2 if urgency != "低" else 0))

        return {
            "emotion": emotion,
            "sentiment": sentiment,
            "confidence": round(confidence, 2),
            "intent_category": intent_category,
            "urgency": urgency,
            "source": "rule",
        }

    def _match_emotion(self, text, prefer_order):
        for name in prefer_order:
            for kw in dict(EMOTION_DICT).get(name, []):
                if kw and kw in text:
                    return name
        # 全部没命中则取第一候选补位
        for name, kws in EMOTION_DICT:
            if name == prefer_order[0]:
                return name
        return "平静"

    def _match_intent(self, text):
        best, best_len = "咨询", 0
        for name, kws in INTENT_DICT:
            for kw in kws:
                if kw and kw in text and len(kw) > best_len:
                    best, best_len = name, len(kw)
        return best

    def _llm_enhance(self, text):
        prompt = (
            "你是校园助手的情绪预判器。给定一段用户文本，输出 JSON：\n"
            '{"emotion":"积极情绪名","sentiment":"positive/negative/neutral","intent_category":"类别","urgency":"低/中/高"}\n'
            f"文本：{text}"
        )
        try:
            resp = requests.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={"model": INTENT_MODEL, "prompt": prompt,
                      "stream": False, "options": {"temperature": 0.1, "num_predict": 128}},
                timeout=30,
            )
            if resp.status_code == 200:
                raw = resp.json().get("response", "")
                start, end = raw.find("{"), raw.rfind("}") + 1
                if start >= 0 and end > start:
                    data = json.loads(raw[start:end])
                    return {
                        "emotion": data.get("emotion", "平静"),
                        "sentiment": data.get("sentiment", "neutral"),
                        "intent_category": data.get("intent_category", "咨询"),
                        "urgency": data.get("urgency", "低"),
                        "confidence": 0.7,
                    }
        except Exception as e:
            print(f"[Emotion] LLM 增强失败: {e}")
        return None


predictor = EmotionPredictor()


def predict(text: str, with_llm: bool = False) -> dict:
    """便捷入口"""
    return predictor.predict(text, with_llm=with_llm)


# 情感提示模板：供回答个性化注入安抚语气
def sentiment_prompt(pred: dict) -> str:
    """根据预判的情感/紧急度生成回答风格指令（供 generator 注入）"""
    prompts = []
    if pred.get("sentiment") == "negative":
        prompts.append("用户可能有些负面情绪，回答请先表达理解与安抚，语气温和，再给出具体可行的帮助。")
    elif pred.get("sentiment") == "positive":
        prompts.append("用户情绪积极，可用轻松友好的语气回应，并适当给予肯定。")
    if pred.get("urgency") == "高":
        prompts.append("该问题紧急，请优先提供最快可执行的行动方案、相关联系方式或线下窗口指引。")
    elif pred.get("urgency") == "中":
        prompts.append("该问题有一定紧迫性，请给出明确、可立即操作的步骤。")
    return "；".join(prompts)
