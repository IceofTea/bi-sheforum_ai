# forum_bot 运行说明

> 适用于 v3.0 架构（基于 RAG 的论坛智能问答机器人）
> 配套项目：bi-sheforum（SpringBoot 论坛后端）

---

## 一、系统架构概述

```
┌─────────────────────────────────────────────────────────────┐
│                      论坛用户 (Vue 前端)                     │
│                    http://localhost:5173                     │
└──────────────────────────┬──────────────────────────────────┘
                           │ 发帖 / 浏览
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   论坛后端 (SpringBoot)                       │
│                    http://localhost:4477                     │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP API
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               Python 机器人服务 (本模块)                      │
│                   http://localhost:5000                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ 意图判断  │  │ 向量检索  │  │  LLM生成  │  │ 关联推荐  │   │
│  │ Ollama    │  │ ChromaDB │  │ LongCat  │  │ 标题匹配 │   │
│  │ DeepSeek  │  │ Embed    │  │ API      │  │          │   │
│  │ 1.5B      │  │ Model    │  │          │  │          │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │               Redis 缓存（可选）                       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 工作流程

```
用户发帖 → 机器人检测到新帖子
    │
    ├─ [步骤1] 意图判断 (本地 DeepSeek-R1 1.5B)
    │   判断是否为提问 → 不是则跳过
    │
    ├─ [步骤2] 向量检索 (ChromaDB + Embedding模型)
    │   将问题转为向量 → 余弦相似度检索 → Top-5
    │
    ├─ [步骤3] 阈值判断
    │   ├─ 相似度 > 0.9 → 直接返回原文链接 (不调LLM)
    │   └─ 相似度 ≤ 0.9 → 进入步骤4
    │
    ├─ [步骤4] LLM生成 (LongCat API / 远程大模型)
    │   构建Prompt (问题 + 检索内容) → 生成回答
    │
    ├─ [步骤5] 关联推荐 (标题向量检索)
    │   匹配相似历史问题 → 追加到回答末尾
    │
    └─ [步骤6] 自动回帖 → 标记已回复 → 缓存
```

---

## 二、安装部署（完整步骤）

### 2.1 基础环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| Python | 3.8+ | 推荐 3.10 |
| MySQL | 5.7+ | 论坛后端需要 |
| Java | 19+ | 论坛后端需要 |
| Node.js | 16+ | 论坛前端需要 |
| Ollama | 最新版 | **核心依赖，必须安装** |
| Redis | 7.x（可选） | 用于缓存加速 |

### 2.2 第1步：安装 Ollama（本地模型运行环境）

Ollama 是一个可以让你在本地运行大语言模型的工具，本项目用它来运行：
- **DeepSeek-R1 1.5B** → 用于判断帖子是否为提问（意图判断）
- **nomic-embed-text** → 用于将文本转为语义向量（Embedding）

#### Windows 安装 Ollama

1. 访问 https://ollama.com/download/windows 下载安装包
2. 双击安装，一路默认即可
3. 安装完成后，Ollama 会自动在后台运行，系统托盘会出现羊驼图标

#### 验证 Ollama 是否安装成功

打开 PowerShell（命令提示符），输入：

```bash
ollama --version
```

如果显示版本号（如 `0.1.32`），则表示安装成功。

### 2.3 第2步：下载模型

打开 PowerShell（或 CMD），依次执行以下命令下载所需模型：

#### 下载意图判断模型（DeepSeek-R1 1.5B）

```bash
ollama pull deepseek-r1:1.5b
```

> ⏳ 这个模型约 1.1GB，下载需要几分钟，取决于网速。
> 下载完成后会显示 `success` 提示。

#### 下载嵌入向量模型（nomic-embed-text）

```bash
ollama pull nomic-embed-text
```

> ⏳ 这个模型约 274MB，下载很快。
> 下载完成后会显示 `success` 提示。

#### 验证模型是否下载成功

```bash
ollama list
```

应该能看到如下输出：

```
NAME                        ID              SIZE    MODIFIED
deepseek-r1:1.5b             a42b25d8c55a    1.1 GB   ...
nomic-embed-text            0a109f422b47    274 MB   ...
```

### 2.4 第3步：配置 Ollama 允许跨域访问（重要）

Ollama 默认只监听 `127.0.0.1`，这已经可以工作。但如果你的机器人和后端在不同机器上，需要设置环境变量：

```bash
# 设置 Ollama 监听所有网络接口
set OLLAMA_HOST=0.0.0.0
```

> 如果是长期使用，建议在系统环境变量中设置 `OLLAMA_HOST=0.0.0.0`

设置后需要重启 Ollama（在系统托盘右键 Ollama → Quit，然后重新启动）。

### 2.5 第4步：安装 Python 依赖

进入 forum_bot 目录，激活虚拟环境并安装依赖：

```bash
cd forum_bot

# 激活虚拟环境（如果已存在）
.\venv\Scripts\activate.bat

# 如果还没有虚拟环境，创建一个
python -m venv venv
.\venv\Scripts\activate.bat

# 安装所有依赖
pip install -r requirements.txt
```

> ⏳ `chromadb` 和 `sentence-transformers` 安装可能需要几分钟。
> 如果网络慢，可以使用国内镜像：`pip install -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt`

### 2.6 第5步：安装 Redis（可选但推荐）

Redis 用于缓存高频问题，加快响应速度。如果没有 Redis，系统会自动使用内存缓存。

#### Windows 安装 Redis

1. 访问 https://github.com/microsoftarchive/redis/releases
2. 下载 `Redis-x64-xxx.msi` 安装包
3. 双击安装，勾选"添加到系统 PATH"
4. 安装完成后，启动 Redis 服务：

```bash
redis-server
```

#### 验证 Redis 是否运行

```bash
redis-cli ping
# 应该返回: PONG
```

### 2.7 第6步：启动论坛后端

确保 SpringBoot 论坛后端已经启动：

```bash
cd bi-sheforum/forum
mvn clean package -DskipTests
java -jar target/forum.admin-0.0.1-SNAPSHOT.jar
```

验证后端是否正常运行：
- 浏览器访问 http://localhost:4477
- 或者在 PowerShell 执行：`curl http://localhost:4477/api/threads/all`

### 2.8 第7步：配置机器人

编辑 `config.py`，或者通过环境变量配置关键参数：

#### 必须配置

```bash
# 如果论坛后端端口不是4477
set FORUM_API_URL=http://localhost:4477

# 设置 LLM API Key（用于回答生成，如果使用远程大模型）
set LLM_API_KEY=your_api_key_here
```

#### 可选配置

```bash
# 如果 Ollama 不在本机或端口不是11434
set OLLAMA_BASE_URL=http://localhost:11434

# 如果要使用其他 Embedding 模型
set EMBEDDING_MODEL=nomic-embed-text

# 如果要使用其他意图判断模型
set INTENT_MODEL=deepseek-r1:1.5b

# 如果要切换嵌入后端（ollama / sentence-transformers / api）
set EMBEDDING_BACKEND=ollama

# 论坛前端地址（用于生成帖子链接）
set FORUM_FRONTEND_URL=http://localhost:5173
```

### 2.9 第8步：启动机器人

首次启动建议先重建索引，再启动 API 服务。

#### 方式A：一键启动（推荐）

```bash
.\venv\Scripts\activate.bat
python bot.py api
```

这会自动：
1. 连接论坛后端，创建/获取机器人用户
2. 加载或重建向量索引
3. 启动 HTTP API 服务（端口 5000）
4. 每隔 60 秒自动扫描新帖子并回复

#### 方式B：分步操作

```bash
.\venv\Scripts\activate.bat

# 第一步：重建向量索引（首次运行必须执行）
python bot.py index

# 第二步：启动 API 服务
python bot.py api
```

#### 方式C：单次扫描

```bash
.\venv\Scripts\activate.bat
python bot.py scan
```

### 2.10 第9步：验证机器人是否正常工作

打开另一个 PowerShell 窗口，执行以下命令测试：

```bash
# 1. 检查机器人状态
curl http://localhost:5000/api/bot/status

# 2. 测试问答（发送一个问题）
curl -X POST http://localhost:5000/api/bot/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"query\": \"校园卡丢了怎么办\"}"

# 3. 手动回复某个帖子
curl -X POST http://localhost:5000/api/bot/reply ^
  -H "Content-Type: application/json" ^
  -d "{\"threadId\": 1}"

# 4. 查看机器人统计
curl http://localhost:5000/api/bot/stats
```

---

## 三、配置文件详解

`config.py` 中所有配置项说明：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `FORUM_API_URL` | `http://localhost:4477` | 论坛后端地址 |
| `BOT_USERNAME` | `智能助手` | 机器人用户名 |
| `BOT_NICKNAME` | `校园小助手` | 机器人昵称 |
| `SIMILARITY_THRESHOLD` | `0.9` | 直接返回的相似度阈值 |
| `TOP_K` | `5` | 检索最相似的 K 条帖子 |
| `MAX_CONTEXT_LENGTH` | `2000` | 上下文最大字符数 |
| `RECOMMEND_TOP_K` | `3` | 关联推荐数量 |
| `RECOMMEND_THRESHOLD` | `0.7` | 关联推荐的相似度阈值 |
| `EMBEDDING_BACKEND` | `ollama` | 嵌入后端 (`ollama`/`sentence-transformers`/`api`) |
| `EMBEDDING_MODEL` | `nomic-embed-text` | 嵌入模型名称 |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama 服务地址 |
| `INTENT_MODEL` | `deepseek-r1:1.5b` | 意图判断模型 |
| `LLM_MODEL` | `LongCat-Flash-Chat` | 回答生成的 LLM 模型 |
| `LLM_API_KEY` | (硬编码兜底) | LLM API 密钥 |
| `LLM_BASE_URL` | `https://api.longcat.chat/...` | LLM API 地址 |
| `FORUM_FRONTEND_URL` | `http://localhost:5173` | 论坛前端地址 |
| `API_HOST` | `0.0.0.0` | 机器人 API 监听地址 |
| `API_PORT` | `5000` | 机器人 API 端口 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_TTL` | `3600` | 缓存过期时间（秒） |

---

## 四、API 接口文档

机器人启动后会提供以下 HTTP 接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/bot/status` | 查看机器人状态 |
| GET | `/api/bot/stats` | 查看详细统计 |
| POST | `/api/bot/index` | 重建向量索引 `{"force": true}` |
| POST | `/api/bot/reply` | 手动回复帖子 `{"threadId": 1}` |
| GET | `/api/bot/search?q=关键词` | 语义搜索相似帖子 |
| POST | `/api/bot/ask` | 问答接口 `{"query": "问题"}` |
| POST | `/api/bot/ask/async` | 异步问答接口（并发检索+生成） |
| POST | `/api/bot/clear_cache` | 清空缓存 |
| GET | `/api/bot/status` | 机器人状态 |

---

## 五、常见问题

### Q1: 启动时报错 "Ollama 连接失败"

**原因**: Ollama 未安装或未运行。
**解决**:
```bash
# 1. 确认 Ollama 已安装
ollama --version

# 2. 确认 Ollama 正在运行（系统托盘有羊驼图标）

# 3. 确认端口可以访问
curl http://localhost:11434/api/tags
```

### Q2: 启动时报错 "模型未找到"

**原因**: 未下载所需的模型。
**解决**:
```bash
ollama pull deepseek-r1:1.5b
ollama pull nomic-embed-text
```

### Q3: 机器人可以启动但不会回复帖子

**原因**: 论坛后端未启动或端口不对。
**解决**:
1. 确认论坛后端运行在 `http://localhost:4477`
2. 在浏览器测试：`http://localhost:4477/api/threads/all`
3. 如果端口不同，设置环境变量：`set FORUM_API_URL=http://localhost:你的端口`

### Q4: 意图判断一直返回 false

**原因**: Ollama 的 DeepSeek 模型响应格式解析失败。
**解决**: 在 `intent.py` 的 `_ollama_judge` 方法中调整模型参数，或在 `config.py` 降低 `temperature` 值。

### Q5: 关联推荐不生效

**原因**: 标题集合中没有数据。
**解决**: 重新运行索引：
```bash
python bot.py index
```

### Q6: Redis 连接失败

**原因**: Redis 未安装或未运行。
**解决**: 系统会自动使用内存缓存，不影响核心功能。如需 Redis，安装后启动：
```bash
redis-server
```

---

## 六、项目文件结构

```
forum_bot/
├── config.py              # 配置文件（所有参数）
├── embedding.py           # 嵌入引擎（支持 Ollama / sentence-transformers）
├── intent.py              # 意图判断（本地 DeepSeek-R1 1.5B）
├── generator.py           # LLM生成器 + ForumAPI + RAGSystem + AsyncRAG
├── cache.py               # Redis缓存（含内存兜底）
├── bot.py                 # 核心机器人（完整 RAG 流程）
├── __init__.py            # 包导出
├── requirements.txt       # Python 依赖
├── forum_bot运行说明.md    # 本说明书
├── vector_store/          # ChromaDB 持久化目录
│   └── chroma.sqlite3     # 向量数据库文件（自动生成）
├── bot.log                # 运行日志（自动生成）
├── run_bot.bat            # 快速启动脚本
├── start_api.bat          # API启动脚本
├── test_rag.py            # RAG 测试脚本
└── venv/                  # Python 虚拟环境
```

---

## 七、技术要点总结

1. **意图判断**: 使用本地 DeepSeek-R1 1.5B 蒸馏模型，通过 Ollama 调用。关键词零成本过滤 → 本地模型精确判断 → 远程 API 兜底的三级策略。

2. **嵌入向量**: 使用 nomic-embed-text 模型（通过 Ollama）将文本转为 768 维向量，取代传统的 TF-IDF 方法。

3. **Chunk 策略**: 利用论坛每层楼的字数限制，直接将每层文本作为一个 chunk，无需额外分块。

4. **ChromaDB**: 使用两个集合：`forum_posts`（内容检索）和 `forum_titles`（关联推荐），分别存储不同类型的数据。

5. **关联推荐**: 通过标题向量集合，计算当前问题与历史问题的语义相似度，在回答末尾追加推荐。

6. **阈值机制**: 相似度 > 0.9 时直接返回原文链接（不调 LLM，节约 token）；否则走 LLM 生成。

7. **缓存系统**: 优先使用 Redis，不可用时自动降级到内存缓存。

8. **异步支持**: 通过 `aiohttp` 和 `asyncio` 实现并发 API 调用，提升响应速度。

9. **上下文裁剪**: 限制上下文长度不超过 `MAX_CONTEXT_LENGTH`（默认2000字符），避免超长输入。

10. **模块化设计**: 各组件独立封装，预留接口便于后续扩展（Rerank、毫秒级响应、大数据推送等）。
