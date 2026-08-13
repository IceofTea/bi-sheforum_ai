import os

# ═══════════════════════════════════════════
# 论坛后端
# ═══════════════════════════════════════════
FORUM_API_URL = os.getenv("FORUM_API_URL", "http://localhost:4477")

# 机器人身份
BOT_USERNAME = "智能助手"
BOT_NICKNAME = "校园小助手"

# ═══════════════════════════════════════════
# RAG 核心参数
# ═══════════════════════════════════════════
SIMILARITY_THRESHOLD = 0.9        # 直接返回的相似度阈值
TOP_K = 5                         # 检索 Top-K 帖子
MAX_CONTEXT_LENGTH = 2000         # 最大上下文长度（字符数）
RECOMMEND_TOP_K = 3               # 关联问题推荐数量
RECOMMEND_THRESHOLD = 0.7         # 关联推荐的相似度阈值

# ═══════════════════════════════════════════
# 向量存储（ChromaDB）
# ═══════════════════════════════════════════
CHROMA_PERSIST_DIR = "./vector_store"
COLLECTION_POSTS = "forum_posts"    # 帖子内容集合
COLLECTION_TITLES = "forum_titles"  # 标题集合（用于关联推荐）

# ═══════════════════════════════════════════
# Embedding 模型
# 支持后端: "ollama" | "sentence-transformers" | "api"
# ═══════════════════════════════════════════
EMBEDDING_BACKEND = os.getenv("EMBEDDING_BACKEND", "ollama")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")
EMBEDDING_DIMENSION = 768  # nomic-embed-text 输出维度

# ═══════════════════════════════════════════
# LLM 后端
# 支持后端: "ollama"(本地,零成本) | "api"(云端OpenAI兼容接口)
# ═══════════════════════════════════════════
LLM_BACKEND = os.getenv("LLM_BACKEND", "ollama")

# ── Ollama 本地模型 ──
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
LLM_OLLAMA_MODEL = os.getenv("LLM_OLLAMA_MODEL", "deepseek-r1:1.5b")  # 问答/生成主模型
INTENT_MODEL = os.getenv("INTENT_MODEL", "deepseek-r1:1.5b")  # 意图判断模型

# ── 远程 LLM API（可选，OpenAI 兼容） ──
LLM_MODEL = os.getenv("LLM_MODEL", "LongCat-Flash-Chat")
LLM_API_KEY = os.getenv("LLM_API_KEY", "ak_22z9pL3wy8eX89f7dD9oT8fo3fP9z")
if not os.getenv("LLM_API_KEY"):
    print("[警告] LLM_API_KEY 未设置环境变量，使用代码中硬编码的默认Key")
LLM_BASE_URL = os.getenv("LLM_BASE_URL", "https://api.longcat.chat/openai/v1")

# ═══════════════════════════════════════════
# 智能体（Agent）循环
# ═══════════════════════════════════════════
AGENT_MAX_ROUNDS = int(os.getenv("AGENT_MAX_ROUNDS", "3"))       # 检索-评估循环最大轮数
AGENT_SCORE_THRESHOLD = float(os.getenv("AGENT_SCORE_THRESHOLD", "0.65"))  # 相关性达标阈值
AGENT_QUERY_REWRITE = os.getenv("AGENT_QUERY_REWRITE", "true") != "false"  # 是否启用查询改写
AGENT_EXPLAIN = os.getenv("AGENT_EXPLAIN", "true") != "false"     # 是否输出推理轨迹(供可解释性文档)
AGENT_SELF_CRITIQUE = os.getenv("AGENT_SELF_CRITIQUE", "true") != "false"  # 生成后自查-修复闭环
AGENT_DUAL_RETRIEVAL = os.getenv("AGENT_DUAL_RETRIEVAL", "true") != "false"  # 双通道检索(向量+BM25)
EMOTION_USE_LLM = os.getenv("EMOTION_USE_LLM", "false") != "false"  # 情绪预判: 是否启用LLM增强(默认纯规则零成本)
CLARIFY_USE_LLM = os.getenv("CLARIFY_USE_LLM", "false") != "false"  # 主动引导: 是否启用LLM生成追问(默认规则零成本)

# ═══════════════════════════════════════════
# 用户画像个性化
# ═══════════════════════════════════════════
PERSONA_ENABLED = os.getenv("PERSONA_ENABLED", "true") != "false"  # 是否启用个性化
PERSONA_MAX_ITEMS = int(os.getenv("PERSONA_MAX_ITEMS", "8"))       # 用户画像上下文最多条目

# ═══════════════════════════════════════════
# 数据分析 Agent
# ═══════════════════════════════════════════
ANALYTICS_TOP_K = int(os.getenv("ANALYTICS_TOP_K", "10"))          # 趋势/排行统计取的条数
HOT_TOPIC_MIN_COUNT = int(os.getenv("HOT_TOPIC_MIN_COUNT", "2"))    # 热点话题最小帖子数

# ═══════════════════════════════════════════
# 记忆（会话记忆 + 用户反馈）
# ═══════════════════════════════════════════
MEMORY_ENABLED = os.getenv("MEMORY_ENABLED", "true") != "false"     # 是否启用记忆
MEMORY_MAX_TURNS = int(os.getenv("MEMORY_MAX_TURNS", "6"))          # 会话记忆最大轮数

# ═══════════════════════════════════════════
# Bot API 服务
# ═══════════════════════════════════════════
API_HOST = "0.0.0.0"
API_PORT = 5000

# 论坛前端地址（用于生成帖子链接）
FORUM_FRONTEND_URL = os.getenv("FORUM_FRONTEND_URL", "http://localhost:5173")

# ═══════════════════════════════════════════
# Redis 缓存（可选）
# ═══════════════════════════════════════════
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
REDIS_DB = int(os.getenv("REDIS_DB", "0"))
REDIS_TTL = 3600
