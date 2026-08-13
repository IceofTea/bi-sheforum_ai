import requests
import json
from config import OLLAMA_BASE_URL, INTENT_MODEL, LLM_API_KEY, LLM_BASE_URL, LLM_MODEL

# 意图判断的预设 Prompt
INTENT_SYSTEM_PROMPT = """你是一个论坛帖子的意图判断助手。你的任务是判断用户发布的帖子标题是否属于"提问"。

判断标准：
1. 帖子标题包含问号（？?）、疑问词（如何、怎么、请问、求解、有没有、能不能、是否、什么、哪里、为什么）→ 是提问
2. 帖子在询问方法、建议、原因、可能性、意见 → 是提问
3. 纯陈述、分享、通知、感慨、灌水 → 不是提问

请只回答一个 JSON，格式：
{"is_question": true/false, "reason": "简要原因"}"""


class IntentJudger:
    """基于本地蒸馏模型的意图判断器
    - 默认使用 Ollama 运行的 DeepSeek-R1 1.5B 蒸馏模型
    - 关键词优先匹配（零成本快速过滤）
    - LLM 精确判断（兜底）
    - 支持 API 回退
    """

    def __init__(self):
        self._ollama_available = self._check_ollama()
        if not self._ollama_available:
            print(f"[Intent] Ollama 不可用，将回退到关键词匹配")

    def _check_ollama(self) -> bool:
        try:
            resp = requests.get(f"{OLLAMA_BASE_URL}/api/tags", timeout=5)
            models = [m["name"] for m in resp.json().get("models", [])]
            available = any(INTENT_MODEL in m for m in models)
            if available:
                print(f"[Intent] Ollama 模型 '{INTENT_MODEL}' 已就绪")
            else:
                print(f"[Intent] Ollama 中未找到模型 '{INTENT_MODEL}'，将使用关键词匹配")
            return available
        except Exception:
            return False

    def judge(self, text: str) -> dict:
        """判断文本是否为提问
        返回: {"is_question": bool, "reason": str, "source": str}
        """
        if not text or not text.strip():
            return {"is_question": False, "reason": "文本为空", "source": "rule"}

        # ── 第一阶段：关键词快速匹配（零成本） ──
        result = self._keyword_match(text)
        if result is not None:
            return result

        # ── 第二阶段：本地模型精确判断 ──
        if self._ollama_available:
            result = self._ollama_judge(text)
            if result is not None:
                return result

        # ── 第三阶段：远程 API 回退 ──
        result = self._api_fallback(text)
        if result is not None:
            return result

        return {"is_question": False, "reason": "所有判断方式均失败", "source": "fallback"}

    def _keyword_match(self, text: str) -> dict or None:
        text_clean = text.strip()
        # 强信号：问号
        if "？" in text_clean or "?" in text_clean:
            return {"is_question": True, "reason": "包含问号", "source": "keyword"}

        # 强信号：典型疑问词
        strong_question_words = ["如何", "怎么", "怎样", "为什么", "啥", "吗"]
        if any(w in text_clean for w in strong_question_words):
            return {"is_question": True, "reason": "包含疑问词", "source": "keyword"}

        # 中信号：提问句式
        question_phrases = ["请问", "求解", "有没有", "能不能", "是否", "可否", "求问", "有谁", "哪里"]
        if any(p in text_clean for p in question_phrases):
            return {"is_question": True, "reason": "包含提问句式", "source": "keyword"}

        # 非提问关键词
        non_question_keywords = ["分享", "通知", "公告", "求加精", "水贴", "灌水"]
        if any(k in text_clean for k in non_question_keywords):
            return {"is_question": False, "reason": "非提问关键词", "source": "keyword"}

        return None  # 需要进一步判断

    def _ollama_judge(self, text: str) -> dict or None:
        try:
            prompt = f"帖子标题：{text}\n\n请判断这是否是一个提问。"
            resp = requests.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={
                    "model": INTENT_MODEL,
                    "prompt": f"{INTENT_SYSTEM_PROMPT}\n\n{prompt}",
                    "stream": False,
                    "options": {"temperature": 0.1, "num_predict": 128}
                },
                timeout=30
            )
            if resp.status_code == 200:
                raw = resp.json().get("response", "")
                result = self._parse_response(raw)
                if result:
                    result["source"] = "ollama"
                    return result
        except Exception as e:
            print(f"[Intent] Ollama 调用失败: {e}")
        return None

    def _parse_response(self, raw: str) -> dict or None:
        try:
            # 提取 JSON
            start = raw.find("{")
            end = raw.rfind("}") + 1
            if start >= 0 and end > start:
                data = json.loads(raw[start:end])
                return {
                    "is_question": bool(data.get("is_question", False)),
                    "reason": data.get("reason", raw[:100])
                }
        except Exception:
            pass
        # 兜底：关键词匹配
        if "是" in raw and "否" not in raw:
            return {"is_question": True, "reason": raw[:100]}
        return None

    def _api_fallback(self, text: str) -> dict or None:
        if not LLM_API_KEY:
            return None
        try:
            headers = {
                "Authorization": f"Bearer {LLM_API_KEY}",
                "Content-Type": "application/json"
            }
            payload = {
                "model": LLM_MODEL,
                "messages": [
                    {"role": "system", "content": INTENT_SYSTEM_PROMPT},
                    {"role": "user", "content": f"帖子标题：{text}"}
                ],
                "max_tokens": 128,
                "temperature": 0.1
            }
            resp = requests.post(
                f"{LLM_BASE_URL}/chat/completions",
                headers=headers, json=payload, timeout=15
            )
            if resp.status_code == 200:
                content = resp.json()["choices"][0]["message"]["content"]
                result = self._parse_response(content)
                if result:
                    result["source"] = "api"
                    return result
        except Exception as e:
            print(f"[Intent] API 回退失败: {e}")
        return None
