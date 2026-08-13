<div align="center">

# 🧠 BI-SheForum AI · 校园智脑论坛系统

**一个将传统论坛业务与多智能体 AI 深度融合的校园社区平台**

融合 RAG 检索增强生成、多智能体协作编排与内容安全审核，
让论坛从「被动问答」进化为「主动服务」的智能体社区。

</div>

---

## ✨ 项目简介

`BI-SheForum AI` 是一套 **「传统论坛业务系统 + AI 智能引擎」** 的一体化解决方案。

- **业务侧（`bi-sheforum/`）**：一个功能完整的校园论坛，覆盖发帖、评论、点赞、收藏、关注、举报、签到、徽章、经验值等社区运营能力，提供**用户端**与**管理端**两套独立前端。
- **智能侧（`forum_bot/`）**：一个基于 **RAG 架构的多智能体引擎「校园智脑」**，自动扫描论坛帖子、识别意图与情绪、检索历史知识、生成智能回复，并提供内容审核、数据分析、热点识别等增值服务。

两者通过 REST API 通信，可**独立部署、独立升级**，开箱即用。

---

## 🚀 核心特性

| 维度 | 能力 |
|------|------|
| **多智能体协作** | 规划 / 检索 / 审查 / 画像 / 记忆 五类角色协同编排，具备完整推理轨迹，可解释可演示 |
| **双通道检索** | 向量语义检索（Ollama Embedding + ChromaDB）+ BM25 关键词检索，**RRF 融合 + 迭代收敛** |
| **自查-修复闭环** | 生成答案后由审查智能体自动评估质量，依据不足则补检重写，直至达标 |
| **个性化问答** | 基于用户画像（资料 / 收藏 / 发帖兴趣）与会话记忆，生成千人千面的回答 |
| **情绪感知** | 规则优先 + LLM 兜底的情绪预判：负面情绪自动安抚、高紧急度问题优先给出行动指引 |
| **主动引导** | 问题信息不足时主动追问、给出选项、回答后延伸话题，形成对话闭环 |
| **内容安全审核** | 广告 / 辱骂 / 色情 / 诈骗 / 隐私 五维度多级检测，规则引擎 + LLM 复核双通道 |
| **数据洞察** | 响应自然语言数据分析问题，输出趋势报告与热点排行榜 |
| **全本地可部署** | 默认使用 Ollama 本地大模型 + 本地向量库，**零 API 成本、数据不出校、可离线运行** |

---

## 🏗️ 技术栈

### 论坛业务系统

| 端 | 技术 |
|----|------|
| 后端 | Spring Boot 3.1 · Java 19 · MyBatis · MySQL · JWT · PageHelper |
| 用户端 | Vue 3 · Vite 4 · Element Plus · Pinia · Vue Router · ECharts · GSAP · Swiper |
| 管理端 | Vue 3 · Vite 4 · Element Plus · Pinia · ECharts |
| 数据 | MySQL（`forum` 库，utf8mb4）· 上传文件本地存储 |

### AI 智能引擎（forum_bot）

| 层 | 技术 |
|----|------|
| 服务 | Python 3.10+ · Flask · Flask-CORS |
| 向量 | ChromaDB · scikit-learn（TF-IDF 兜底） |
| Embedding | Ollama（`nomic-embed-text`）· sentence-transformers · API 三选一 |
| LLM | Ollama 本地模型（`deepseek-r1:1.5b`，默认）· 云端 OpenAI 兼容接口（可选） |
| 缓存 | Redis（可选，未连接自动降级为内存缓存） |

---

## 🧬 系统架构

```
┌────────────────────────────────────────────────────────────────────────┐
│                         BI-SheForum AI                                │
├────────────────────────────┬───────────────────────────────────────────┤
│   ① 论坛业务系统            │   ② 校园智脑 · AI 引擎                    │
│   ┌────────────────────┐   │   ┌────────────────────────────────────┐  │
│   │ vue-forum-user     │   │   │        CampusAgent (Coordinator)   │  │
│   │ 用户端 :5173        │   │   │  ┌─────────┐ ┌─────────┐          │  │
│   ├────────────────────┤   │   │  │Planner  │ │Retriever│          │  │
│   │ vue-forum-admin    │   │   │  │规划智能体│ │检索智能体│          │  │
│   │ 管理端 :5173*       │   │   │  └────┬────┘ └────┬────┘          │  │
│   ├────────────────────┤   │   │       │  ┌─────────┐              │  │
│   │ Spring Boot 后端    │◀──┼───┼──────▶│  │ Critic  │              │  │
│   │ REST API :4477     │   │   │       │  │审查智能体│              │  │
│   │  ├─ MySQL (forum)  │   │   │       │  └─────────┘              │  │
│   │  └─ 上传文件存储    │   │   │  ┌─────────┐ ┌─────────┐          │  │
│   └────────────────────┘   │   │  │ Persona │ │ Memory  │          │  │
│                            │   │  │ 用户画像 │ │ 会话记忆 │          │  │
│                            │   │  └─────────┘ └─────────┘          │  │
│                            │   │                                    │  │
│                            │   │  ┌────────────────────────────┐    │  │
│                            │   │  │  辅助 Agent                 │    │  │
│                            │   │  │ Emotion情绪预判             │    │  │
│                            │   │  │ Clarify 主动引导            │    │  │
│                            │   │  │ Moderator 内容审核          │    │  │
│                            │   │  │ Analytics 数据分析          │    │  │
│                            │   │  │ HotTopic 热点识别           │    │  │
│                            │   │  └────────────────────────────┘    │  │
│                            │   └────────────────────────────────────┘  │
│                            │   Flask API :5000 · ChromaDB 向量库       │
└────────────────────────────┴───────────────────────────────────────────┘
```

> `*` 两个前端默认均监听 5173，需同时运行时可在各自 `vite.config.js` 中调整端口。

### 智能回复流水线

```
帖子发布
   │
   ▼
意图判断 ──── 否（纯分享/闲聊）────▶ 跳过
   │是
   ▼
情绪预判（情感极性 · 意图类别 · 紧急度）──── 高紧急 ▶ 附加行动指引 + 联系方式
   │
   ▼
双通道检索（向量语义 + BM25 关键词）────▶ RRF 融合 ──▶ 迭代收敛（多轮补检）
   │
   ▼
阈值判断：相似度 > 0.9 ──是──▶ 直接复用历史优质答案（节约 Token）
   │否
   ▼
构建 Prompt（注入用户画像 + 会话记忆 + 检索上下文）
   │
   ▼
LLM 生成回答 ──▶ 审查智能体评估 ──不达标──▶ 补检重写（至多 N 轮）
   │达标
   ▼
信息不足？──▶ 主动追问 + 选项引导
   │
   ▼
自动回帖（智能助手身份）并标记已回复
```

---

## 📂 项目结构

```
bi-sheforum_ai/
├── bi-sheforum/                      # ① 论坛业务系统
│   ├── forum/                        #    Spring Boot 后端
│   │   └── src/main/java/com/henry/forum/admin/
│   │       ├── controller/           #    20+ REST 控制器
│   │       ├── service/              #     业务逻辑层
│   │       ├── mapper/               #     MyBatis 数据访问层
│   │       ├── entity/               #     实体模型
│   │       ├── config/               #     全局配置 / 拦截器
│   │       └── util/                 #     工具类（JWT 等）
│   ├── vue-forum-user/               #    用户端前端（:5173）
│   ├── vue-forum-admin/              #    管理端前端（:5174）
│   ├── upload/                       #    上传资源目录
│   └── forum.sql                     #    数据库初始化脚本（MySQL）
│
├── forum_bot/                        # ② 校园智脑 · AI 引擎
│   ├── agent.py                      #    CampusAgent 多智能体协作编排核心
│   ├── bot.py                        #    机器人主服务（索引 / 扫描 / API）
│   ├── embedding.py                  #    向量嵌入 + ChromaDB 存储检索
│   ├── generator.py                  #    LLM 生成 / RAG 系统 / 论坛 API
│   ├── intent.py                     #    意图判断器（关键词 + LLM 兜底）
│   ├── emotion.py                    #    情绪与紧急度预判
│   ├── clarify.py                    #    主动式澄清与引导
│   ├── moderation.py                 #    内容安全审核
│   ├── analytics.py                  #    数据分析智能体
│   ├── hot_topic.py                  #    热点话题识别
│   ├── cache.py                      #    Redis / 内存缓存
│   ├── scene_config.py               #    场景配置中心（换场景只改这一个文件）
│   ├── config.py                     #    全局配置（后端 / 模型 / 阈值）
│   ├── requirements.txt              #    Python 依赖
│   ├── web/index.html                #    机器人状态看板
│   ├── run_bot.bat / start_all.bat   #    一键启动脚本
│   └── vector_store/                 #    ChromaDB 向量数据目录
│
├── skills/                           # AI 协作技能库（开发辅助）
├── temp/                             # 临时文件（不入库）
└── README.md                         # 本文档
```

---

## ⚡ 快速开始

### 环境准备

| 依赖 | 版本建议 | 用途 |
|------|----------|------|
| JDK | 19+ | 后端编译运行 |
| Maven | 3.6+ | 后端依赖管理 |
| Node.js | 16+ | 前端构建 |
| MySQL | 5.7+ | 业务数据存储 |
| Python | 3.10+ | AI 引擎 |
| Ollama | 最新 | 本地大模型 / Embedding（可选，也可走云端 API） |

### 第一步：启动论坛业务系统

```bash
# 1. 初始化数据库
mysql -uroot -p -e "CREATE DATABASE forum DEFAULT CHARSET utf8mb4;"
mysql -uroot -p forum < bi-sheforum/forum.sql

# 2. 启动 Spring Boot 后端（默认端口 4477）
cd bi-sheforum/forum
mvn spring-boot:run

# 3. 启动用户端（默认端口 5173）
cd ../vue-forum-user
npm install
npm run dev

# 4. 启动管理端（默认端口 5173，如需与用户端并存请调整端口）
cd ../vue-forum-admin
npm install
npm run dev
```

> 后端数据库连接配置见 `bi-sheforum/forum/src/main/resources/application.yml`。

### 第二步：准备 AI 引擎

```bash
# 1. 安装并启动 Ollama，拉取默认模型
ollama pull deepseek-r1:1.5b      # 问答/意图模型
ollama pull nomic-embed-text      # 向量嵌入模型

# 2. 创建 Python 虚拟环境并安装依赖
cd forum_bot
python -m venv venv
.\venv\Scripts\activate.bat
pip install -r requirements.txt
```

> 若使用云端 LLM，可跳过 Ollama 安装，改用 `config.py` 中 OpenAI 兼容的 `api` 后端。

### 第三步：启动 AI 引擎

```bash
cd forum_bot

# 首次运行：建立帖子向量索引
python bot.py index

# 模式一：持续监控 + 自动回复（Flask API，默认端口 5000）
python bot.py api

# 模式二：手动扫描回复一次
python bot.py scan

# 模式三：一键启动（API + 索引）
.\start_all.bat
```

启动后可访问 `http://localhost:5000` 查看机器人状态看板。

---

## 🔌 API 端点

### 机器人服务（`http://localhost:5000`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/bot/status` | 查看机器人运行状态 |
| POST | `/api/bot/index` | 重建向量索引（`{"force": true}`） |
| POST | `/api/bot/reply` | 手动触发回复（`{"threadId": 1}`） |
| GET | `/api/bot/search?q=关键词` | 语义搜索相似帖子 |

### 论坛业务（Spring Boot，`http://localhost:4477`）

覆盖用户、帖子、评论、点赞、收藏、关注、举报、签到、徽章、消息、上传等 20+ 个资源，
由 `controller/` 下 20 个 REST 控制器提供，前端经 Vite 代理转发（`/api`、`/upload`、`/vercode`）。

---

## ⚙️ 配置速览

AI 引擎所有参数集中在 `forum_bot/config.py`，支持环境变量覆盖：

```python
FORUM_API_URL      = "http://localhost:4477"   # 论坛后端地址
LLM_BACKEND        = "ollama"                   # ollama | api
LLM_OLLAMA_MODEL   = "deepseek-r1:1.5b"         # 本地生成模型
EMBEDDING_BACKEND  = "ollama"                   # ollama | sentence-transformers | api
SIMILARITY_THRESHOLD = 0.9                      # 直接复用历史答案阈值
AGENT_MAX_ROUNDS   = 3                          # 检索-评估循环最大轮数
AGENT_DUAL_RETRIEVAL = True                     # 双通道检索（向量 + BM25）
AGENT_SELF_CRITIQUE  = True                     # 自查-修复闭环
PERSONA_ENABLED    = True                       # 用户画像个性化
MEMORY_ENABLED     = True                       # 会话记忆
EMOTION_USE_LLM    = False                      # 情绪预判是否启用 LLM 兜底
CLARIFY_USE_LLM    = False                      # 主动引导是否启用 LLM 生成
```

> 环境变量示例：`set LLM_BACKEND=api`、`set LLM_MODEL=your-model`、`set LLM_API_KEY=your-key`
> 云端 LLM 仅在显式配置时启用；代码内默认不依赖任何云端密钥即可全本地运行。

---

## 🧩 多智能体设计

| Agent | 职责 | 创新点 |
|-------|------|--------|
| **PlannerAgent 规划智能体** | 意图识别、子查询分解（≤3）、查询改写 | 把模糊提问结构化为可检索的子问题 |
| **RetrieverAgent 检索智能体** | 向量语义 + BM25 双通道检索，RRF 融合 + 迭代收敛 | 语义互补提升召回，多轮补检逼近目标 |
| **CriticAgent 审查智能体** | 生成后质量评估、缺失信息定位、触发重写 | 自查-修复闭环，答案质量可保证 |
| **PersonaProvider 画像** | 基于用户资料 / 收藏 / 发帖兴趣构建画像 | 千人千面，回答贴合提问者背景 |
| **Memory 记忆** | 会话上下文 + 用户反馈偏好管理 | 多轮对话有记忆，体验连贯 |
| **Emotion 情绪预判** | 情感极性、情绪标签、紧急度判定 | 负面情绪自动安抚、紧急问题优先行动指引 |
| **Clarify 主动引导** | 信息缺口识别、追问 + 选项、回答后延伸 | 从「被动问答」升级为「主动服务」 |
| **Moderator 内容审核** | 广告 / 辱骂 / 色情 / 诈骗 / 隐私五维检测 | 规则引擎 + LLM 复核双通道，守护社区安全 |
| **Analytics 数据分析** | 聚合统计 + LLM 生成分析报告 | 自然语言驱动数据洞察 |
| **HotTopic 热点识别** | 板块热度 + 发帖频率 + 关键词聚类 | 自动发现校园热点话题 |

**场景无关设计**：所有业务词条（情感词典、意图词典、追问话术、行动指引）全部收敛于
`scene_config.py`。将该引擎移植到电商客服、政务问答、医疗导诊等新场景时，**仅需修改这一个配置文件**，
引擎代码零改动即可复用。

---

## 🧪 演示建议（答辩 / 展示）

1. **推理轨迹可视化**：开启 `AGENT_EXPLAIN`，完整展示「规划 → 检索 → 审查 → 修复」推理链条；
2. **情绪感知**：发布一条带抱怨语气的帖子，观察机器人安抚式回复与紧急行动指引；
3. **主动引导**：发布模糊提问，观察机器人追问并给出可点选选项；
4. **内容审核**：提交含联系方式 / 辱骂的帖子，观察风险拦截与处理建议；
5. **离线可跑**：断开外网，全链路 Ollama 本地推理演示，体现「数据不出校、零成本」。

---

## 🗺️ 演进路线（Roadmap）

- [ ] 接入 **Rerank 重排**，进一步提升召回精度
- [ ] 前端集成**对话式问答入口**，实现毫秒级响应
- [ ] 意图蒸馏：使用小模型对意图判断做低成本蒸馏
- [ ] 相似问题**关联推荐**，主动推送给提问用户
- [ ] 支持更多向量数据库（Milvus / Qdrant）与多租户隔离
- [ ] 评论/帖子自动摘要与知识沉淀，反哺向量库

---

## 📄 版权说明

本项目为校园实践作品，代码与文档保留全部权利。
使用前请自行配置好本地环境与模型资源；涉及的任何密钥请通过环境变量管理，切勿提交至公共仓库。

---

<div align="center">

**Built with ❤️ for campus community · 让每一所校园，都有自己的智脑**

</div>
