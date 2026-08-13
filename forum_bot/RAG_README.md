# 基于RAG架构的论坛智能问答机器人系统

## 一、项目概述

本项目是一个基于RAG（检索增强生成）技术架构的论坛智能问答机器人系统，主要解决传统论坛检索中关键词匹配无法理解语义，以及大语言模型直接生成回答容易产生不准确内容的问题。

### 核心特性

1. **语义检索 + 大模型生成**双阶段流程
2. **意图判断**模块，使用LLM判断是否为提问
3. **向量数据库**存储，使用TF-IDF/Chroma进行语义匹配
4. **阈值判断**，高相似度（>0.9）直接返回历史答案
5. **LLM增强生成**，低相似度时调用大模型生成回答

---

## 二、系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        论坛后端 (SpringBoot)                    │
│                         http://localhost:4477                   │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Python RAG机器人服务                       │
│                         http://localhost:5000                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │   帖子获取   │───▶│  意图判断     │───▶│  向量检索     │      │
│  │  ForumAPI   │    │  LLM判断     │    │  TF-IDF      │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│                                                  │              │
│                             ┌─────────────────────┘              │
│                             ▼                                    │
│                     ┌───────────────┐                           │
│                     │   阈值判断     │  相似度 > 0.9?           │
│                     └───────────────┘                           │
│                        │              │                          │
│                       Yes             No                        │
│                        ▼              ▼                         │
│              ┌────────────┐    ┌────────────┐                  │
│              │ 直接返回   │    │ LLM生成    │                  │
│              │ 历史答案   │    │ 回答       │                  │
│              └────────────┘    └────────────┘                  │
│                        │              │                          │
│                        └──────────────┘                         │
│                                 ▼                               │
│                        ┌────────────┐                           │
│                        │  自动回帖  │                           │
│                        └────────────┘                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、模块说明

### 1. config.py - 配置文件

```python
FORUM_API_URL = "http://localhost:4477"  # 论坛后端地址
BOT_USERNAME = "智能助手"
BOT_NICKNAME = "校园小助手"

SIMILARITY_THRESHOLD = 0.9   # 相似度阈值
TOP_K = 5                     # 检索Top-K条

CHROMA_PERSIST_DIR = "./vector_store"  # 向量存储目录

# LLM配置 - 使用LongCat API
LLM_MODEL = "LongCat-Flash-Chat"
LLM_API_KEY = "ak_22z9pL3wy8eX89f7dD9oT8fo3fP9z"
LLM_BASE_URL = "https://api.longcat.chat/openai/v1"
INTENT_MODEL = "LongCat-Flash-Thinking"  # 意图判断模型
```

### 2. embedding.py - 向量嵌入模块

**功能**：
- 将帖子内容转换为向量表示
- 存储到本地向量数据库
- 语义相似度检索

**实现**：
```python
class EmbeddingEngine:
    def __init__(self):
        # 优先使用ChromaDB，失败则使用TF-IDF
        self.vectorizer = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))

    def add_post(self, post_id: int, content: str, meta: dict):
        # 添加帖子到向量索引

    def search(self, query: str, top_k: int = 5) -> list:
        # 向量检索，返回Top-K相似帖子

    def get_similar_posts(self, query: str, threshold: float = 0.9) -> list:
        # 获取高于阈值的相似帖子
```

### 3. generator.py - LLM生成模块

**功能**：
- 意图判断：判断用户是否为提问
- 回答生成：基于上下文生成回答
- 论坛API：与后端通信

**实现**：
```python
class LLMGenerator:
    def judge_intent(self, text: str) -> dict:
        # 判断是否为提问
        # 关键词快速判断 + LLM二次确认

    def generate_with_context(self, query: str, context_docs: list) -> str:
        # 构建Prompt，调用LLM生成回答

class ForumAPI:
    def get_threads(self, limit: int) -> list:
        # 获取帖子列表

    def post_comment(self, thread_id: int, user_id: int, comment: str) -> bool:
        # 发表评论（回帖）

class RAGSystem:
    def answer_question(self, thread: dict) -> str:
        # 完整的RAG回答流程
```

### 4. bot.py - 核心机器人

**功能**：
- 帖子索引
- 自动扫描和回复
- API服务

**核心流程**：
```python
def process_thread(self, thread: dict) -> bool:
    # 1. 检查是否已回复
    # 2. 向量检索相似帖子
    # 3. 阈值判断
    # 4. 直接返回或LLM生成
    # 5. 自动回帖
    # 6. 记录已回复
```

---

## 四、工作流程

### 4.1 帖子索引流程

```
启动机器人
     │
     ▼
加载已有向量索引（如果有）
     │
     ▼
调用 /api/threads/all 获取所有帖子
     │
     ▼
遍历每条帖子 ──────────┐
     │                  │
     ▼                  ▼
提取帖子ID和内容    获取帖子ID和内容
     │                  │
     ▼                  ▼
add_post()           add_post()
     │                  │
     └────────┬─────────┘
              ▼
       保存向量到本地文件
```

### 4.2 自动回复流程

```
定时扫描 (每60秒)
     │
     ▼
获取最新帖子列表
     │
     ▼
过滤未回复的帖子
     │
     ▼
遍历每个帖子 ──────────┐
     │                  │
     ▼                  ▼
获取帖子标题        获取帖子标题
     │                  │
     ▼                  ▼
向量检索            向量检索
(similarity > 0.9?)  (top_k=5)
     │                  │
    Yes                No
     │                  │
     ▼                 ▼
直接返回历史答案    调用LLM生成回答
     │                  │
     └────────┬─────────┘
              ▼
         自动回帖
              │
              ▼
         标记已回复
```

### 4.3 RAG完整流程

```
用户提问帖
     │
     ▼
意图判断 ──────────┐
     │              │
    是             否
     │              │
     ▼              ▼
向量检索         跳过
     │
     ▼
检查相似度 > 0.9?
     │
    Yes    No
     │      │
     ▼      ▼
直接返回   检索Top-5
答案       相似内容
     │         │
     │         ▼
     │    构建Prompt
     │         │
     │         ▼
     │    调用LLM生成
     │         │
     └────┬────┘
          ▼
       回帖给用户
```

---

## 五、启动说明

### 5.1 环境要求

```bash
# Python虚拟环境
forum_bot/venv/

# 依赖包
flask>=2.3.0
flask-cors>=4.0.0
numpy>=1.24.0
requests>=2.28.0
scikit-learn>=1.0.0
chromadb>=0.4.0  # 可选
```

### 5.2 启动步骤

#### 步骤1：启动论坛后端

确保SpringBoot后端运行在 `http://localhost:4477`

#### 步骤2：激活虚拟环境

```bash
cd forum_bot
.\venv\Scripts\activate.bat
```

#### 步骤3：启动API服务（可选）

```bash
python bot.py api
# 访问 http://localhost:5000 查看状态
```

#### 步骤4：创建向量索引（首次运行）

```bash
python -c "from bot import ForumBot; b = ForumBot(); b.index_all_threads(force=True)"
```

#### 步骤5：触发自动回复

```bash
python -c "from bot import ForumBot; b = ForumBot(); b.scan_and_reply(limit=10)"
```

### 5.3 运行模式

| 模式 | 命令 | 说明 |
|------|------|------|
| API服务 | `python bot.py api` | 启动REST API，持续监控 |
| 扫描模式 | `python bot.py scan` | 执行一次扫描回复 |
| 索引模式 | `python bot.py index` | 重建向量索引 |

### 5.4 API端点

- `GET /api/bot/status` - 查看机器人状态
- `POST /api/bot/index` - 重建索引 `{"force": true}`
- `POST /api/bot/reply` - 手动回复 `{"threadId": 1}`
- `GET /api/bot/search?q=关键词` - 搜索相似帖子

---

## 六、关键技术点

### 6.1 向量检索

使用TF-IDF将文本转换为向量，通过余弦相似度计算语义相似性。

```python
query_vec = vectorizer.transform([query]).toarray()
similarities = cosine_similarity(query_vec, vectors)[0]
top_indices = np.argsort(similarities)[-top_k:][::-1]
```

### 6.2 阈值判断

设计意图判断和阈值检测函数：
- 相似度 > 0.9：直接返回历史答案（节约token）
- 相似度 < 0.9：调用LLM生成（保证准确性）

### 6.3 Prompt设计

```python
system_prompt = """你是一个校园论坛的智能问答助手。
你的回答必须基于提供的参考资料。
1. 只基于给定的上下文回答，不要编造信息
2. 如果无法从参考资料中找到答案，请明确说明
3. 回答要简洁明了，语言要友好"""
```

### 6.4 模块化设计

- `EmbeddingEngine`: 向量存储和检索
- `LLMGenerator`: 大模型调用
- `ForumAPI`: 论坛接口通信
- `RAGSystem`: RAG流程封装
- `ForumBot`: 核心业务逻辑

---

## 七、优化方向

1. **意图判断优化**：使用DeepSeek等小模型进行蒸馏判断
2. **向量数据库**：使用Chroma替代TF-IDF，提升语义理解能力
3. **响应速度**：接入搜索框，实现毫秒级响应
4. **关联推荐**：检索相似问题，主动推荐给用户
5. **重排机制**：引入Rerank流程，提高召回精度

---

## 八、文件结构

```
forum_bot/
├── config.py          # 配置文件
├── bot.py             # 核心机器人
├── embedding.py       # 向量嵌入模块
├── generator.py       # LLM生成模块
├── requirements.txt   # 依赖
├── vector_store/      # 向量存储目录
│   └── tfidf_index.pkl
├── run_bot.bat        # 启动脚本
└── README.md          # 说明文档
```

---

## 九、注意事项

1. **LLM API Key**：当前使用LongCat API，需正确配置
2. **后端重启**：修改UserInfoController后需重启后端
3. **向量索引**：首次运行需要创建索引，后续会自动加载
4. **编码问题**：Windows环境下可能存在编码问题，代码中已做处理
5. **已回复记录**：机器人会记录已回复的帖子ID，避免重复回复