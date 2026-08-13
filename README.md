# BI-SheForum AI

校园论坛业务系统与基于 RAG 的多智能体问答引擎的集成项目。论坛业务由
Spring Boot + Vue 3 实现，AI 引擎为独立 Python 服务，两者通过 REST API 解耦通信。

## 1. 系统组成

| 子系统 | 路径 | 说明 |
|--------|------|------|
| 论坛业务系统 | `bi-sheforum/` | Spring Boot 3 后端 + 用户端 / 管理端两套 Vue 前端 + MySQL 数据库脚本 |
| AI 智能引擎 | `forum_bot/` | 基于检索增强生成（RAG）的多智能体协作服务，自动回复、内容审核、数据分析 |

通信方式：AI 引擎通过 HTTP 调用论坛后端 REST API 获取帖子、回帖与统计数据；
双方均可在本地独立部署、独立升级。

## 2. 技术栈

### 2.1 论坛业务系统

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.1.0 · Java 19 · MyBatis 2.3.0 · java-jwt 4.2.1 · PageHelper 1.4.6 |
| 用户端前端 | Vue 3.2 · Vite 4.1 · Element Plus 2.3 · Pinia 2.0 · Vue Router 4 · ECharts 5 · GSAP · Swiper |
| 管理端前端 | Vue 3.2 · Vite 4.1 · Element Plus 2.3 · Pinia 2.0 · ECharts 5 |
| 数据层 | MySQL（库名 `forum`，utf8mb4）· 本地文件存储（上传资源） |
| 鉴权 | JWT（无状态认证） |

### 2.2 AI 智能引擎

| 层 | 技术 |
|----|------|
| 服务框架 | Python 3.10+ · Flask · Flask-CORS |
| 向量存储 | ChromaDB（持久化目录 `vector_store/`），scikit-learn TF-IDF 作为降级方案 |
| Embedding | 三后端可切换：`ollama`（默认，`nomic-embed-text`，768 维）· `sentence-transformers` · `api` |
| LLM | 双后端可切换：`ollama`（默认，`deepseek-r1:1.5b`）· `api`（OpenAI 兼容接口） |
| 缓存 | Redis（可选，不可用时自动降级为进程内缓存） |

## 3. 架构与数据流

### 3.1 整体架构

```
┌───────────────────────────────┬──────────────────────────────────────────────┐
│  ① 论坛业务系统               │  ② AI 智能引擎                               │
│                               │                                              │
│  ┌─────────────────────────┐  │  ┌────────────────────────────────────────┐  │
│  │ vue-forum-user :5173    │  │  │  CampusAgent（总控 / 编排）             │  │
│  │ vue-forum-admin :5173*  │  │  │   ├─ PlannerAgent   查询理解 / 子查询   │  │
│  ├─────────────────────────┤  │  │   ├─ RetrieverAgent 双通道检索 + RRF    │  │
│  │ Spring Boot REST :4477  │◀─┼──┼─▶│   ├─ CriticAgent   自查-修复闭环     │  │
│  │  ├─ MySQL :3306/forum   │  │  │   ├─ PersonaProvider 用户画像          │  │
│  │  └─ upload/ 文件存储    │  │  │   └─ Memory         会话记忆            │  │
│  └─────────────────────────┘  │  │                                        │  │
│                               │  │  辅助 Agent：Emotion / Clarify /       │  │
│                               │  │  Moderator / Analytics / HotTopic      │  │
│                               │  └────────────────────────────────────────┘  │
│                               │  Flask API :5000 · ChromaDB :vector_store   │
└───────────────────────────────┴──────────────────────────────────────────────┘
```

`*` 两个前端默认端口均为 5173，需同时运行时在各自 `vite.config.js` 中调整。

### 3.2 智能回复流水线

```
帖子发布
  ├─ 意图判断（intent.py）──非提问（分享/闲聊）──▶ 跳过
  ▼
情绪预判（emotion.py）：情感极性 / 意图类别 / 紧急度 ──高紧急──▶ 附加行动指引
  ▼
双通道检索（embedding.py + BM25）──▶ RRF 融合 ──▶ 迭代收敛（至多 AGENT_MAX_ROUNDS 轮）
  ▼
相似度 ≥ SIMILARITY_THRESHOLD(0.9) ──是──▶ 直接复用历史优质回答（省 Token）
  │否
  ▼
构建 Prompt：检索上下文 + 用户画像(Persona) + 会话记忆(Memory)
  ▼
LLM 生成（generator.py）──▶ CriticAgent 评估 ──不达标──▶ 补充检索并重写（闭环）
  │达标
  ▼
Clarify 信息缺口检测 ──信息不足──▶ 追问 + 选项引导
  ▼
自动回帖（bot.py，以智能助手身份）并记录已回复，避免重复回复
```

## 4. 目录结构

```
bi-sheforum_ai/
├── bi-sheforum/                    # 论坛业务系统
│   ├── forum/                      #   Spring Boot 后端（:4477）
│   │   └── src/main/
│   │       ├── java/com/henry/forum/admin/
│   │       │   ├── controller/     #     REST 控制器（20 个资源）
│   │       │   ├── service/        #     业务逻辑层
│   │       │   ├── mapper/         #     MyBatis 数据访问
│   │       │   ├── entity/         #     实体模型
│   │       │   ├── config/         #     配置 / 拦截器
│   │       │   └── util/           #     JWT 等工具
│   │       └── resources/          #     application.yml + 增量迁移 SQL
│   ├── vue-forum-user/             #   用户端前端（Vite :5173）
│   ├── vue-forum-admin/            #   管理端前端（Vite :5173）
│   ├── upload/                     #   上传资源目录
│   └── forum.sql                   #   数据库初始化脚本（全量建表 + 示例数据）
│
├── forum_bot/                      # AI 智能引擎
│   ├── agent.py                    #   CampusAgent 多智能体编排（Planner/Retriever/Critic/Persona/Memory）
│   ├── bot.py                      #   机器人主入口：index / scan / api 三种模式
│   ├── generator.py                #   LLM 生成器 · ForumAPI 客户端 · RAG 流程封装
│   ├── embedding.py                #   向量嵌入 + ChromaDB/TF-IDF 检索
│   ├── intent.py                   #   意图判断（关键词 + LLM 兜底）
│   ├── emotion.py                  #   情绪 / 紧急度预判
│   ├── clarify.py                  #   主动澄清与引导
│   ├── moderation.py               #   内容安全审核（广告/辱骂/色情/诈骗/隐私）
│   ├── analytics.py                #   数据分析（聚合 + LLM 报告）
│   ├── hot_topic.py                #   热点话题识别（聚类 + 频率）
│   ├── cache.py                    #   Redis / 内存缓存
│   ├── scene_config.py             #   场景配置中心（业务词条集中于此）
│   ├── config.py                   #   全局参数（环境变量可覆盖）
│   ├── requirements.txt            #   Python 依赖清单
│   ├── web/index.html              #   机器人状态看板
│   ├── run_bot.bat · start_all.bat #   Windows 启动脚本
│   └── vector_store/               #   ChromaDB 持久化数据
│
├── skills/                         # AI 协作开发技能库（非运行时依赖）
└── README.md
```

## 5. 环境要求

| 组件 | 版本 | 用途 |
|------|------|------|
| JDK | 19+ | 后端编译运行 |
| Maven | 3.6+ | 后端依赖 |
| Node.js | 16+ | 前端构建 |
| MySQL | 5.7+ | 业务数据 |
| Python | 3.10+ | AI 引擎 |
| Ollama | 最新 | 本地 LLM / Embedding（使用云端 API 时可省略） |

## 6. 部署与启动

### 6.1 论坛业务系统

```bash
# 初始化数据库（库名 forum）
mysql -uroot -p -e "CREATE DATABASE forum DEFAULT CHARSET utf8mb4;"
mysql -uroot -p forum < bi-sheforum/forum.sql

# 启动后端（默认 :4477）
cd bi-sheforum/forum
mvn spring-boot:run

# 启动用户端（默认 :5173）
cd ../vue-forum-user
npm install && npm run dev

# 启动管理端（默认 :5173，与用户端并存请调整端口）
cd ../vue-forum-admin
npm install && npm run dev
```

数据库连接等参数在 `bi-sheforum/forum/src/main/resources/application.yml` 中配置。

### 6.2 AI 智能引擎

```bash
# 1. 准备本地模型（使用云端 API 可跳过）
ollama pull deepseek-r1:1.5b     # 生成 / 意图模型
ollama pull nomic-embed-text     # 向量嵌入模型

# 2. 安装依赖
cd forum_bot
python -m venv venv
.\venv\Scripts\activate.bat
pip install -r requirements.txt

# 3. 建立帖子向量索引（首次运行或数据变更后）
python bot.py index

# 4. 启动 API 服务（持续监控并自动回复，默认 :5000）
python bot.py api
```

### 6.3 运行模式

| 模式 | 命令 | 行为 |
|------|------|------|
| `api` | `python bot.py api` | 启动 Flask API，定时扫描新帖并自动回复 |
| `scan` | `python bot.py scan` | 执行一次扫描回复后退出 |
| `index` | `python bot.py index` | 全量重建向量索引 |

## 7. 配置项

AI 引擎全部参数集中于 `forum_bot/config.py`，支持环境变量覆盖（同名大写）。

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `FORUM_API_URL` | `http://localhost:4477` | 论坛后端地址 |
| `SIMILARITY_THRESHOLD` | `0.9` | 复用历史答案的相似度阈值 |
| `TOP_K` | `5` | 检索返回的候选条数 |
| `LLM_BACKEND` | `ollama` | `ollama`（本地）\| `api`（OpenAI 兼容） |
| `LLM_OLLAMA_MODEL` | `deepseek-r1:1.5b` | 本地生成模型 |
| `LLM_MODEL` / `LLM_BASE_URL` | LongCat Flash | 云端模型名与接口地址 |
| `LLM_API_KEY` | 环境变量 | 云端接口密钥，仅 `api` 后端使用 |
| `EMBEDDING_BACKEND` | `ollama` | `ollama` \| `sentence-transformers` \| `api` |
| `AGENT_MAX_ROUNDS` | `3` | 检索-评估循环最大轮数 |
| `AGENT_SCORE_THRESHOLD` | `0.65` | 相关性达标阈值 |
| `AGENT_DUAL_RETRIEVAL` | `true` | 启用向量 + BM25 双通道检索 |
| `AGENT_SELF_CRITIQUE` | `true` | 启用自查-修复闭环 |
| `AGENT_EXPLAIN` | `true` | 输出推理轨迹（可解释性） |
| `PERSONA_ENABLED` | `true` | 启用用户画像个性化 |
| `MEMORY_ENABLED` | `true` | 启用会话记忆 |
| `EMOTION_USE_LLM` / `CLARIFY_USE_LLM` | `false` | 情绪预判 / 主动引导是否启用 LLM 兜底（默认纯规则，零成本） |
| `REDIS_HOST` / `REDIS_PORT` | `localhost:6379` | 缓存，不可用时自动降级 |
| `API_HOST` / `API_PORT` | `0.0.0.0:5000` | 机器人 API 监听地址 |

## 8. API 接口

### 8.1 机器人服务（Flask，:5000）

| 方法 | 路径 | 请求 | 说明 |
|------|------|------|------|
| GET | `/api/bot/status` | — | 机器人运行状态 |
| POST | `/api/bot/index` | `{"force": true}` | 重建向量索引 |
| POST | `/api/bot/reply` | `{"threadId": 1}` | 手动触发回复 |
| GET | `/api/bot/search` | `?q=关键词` | 语义搜索相似帖子 |

### 8.2 论坛业务（Spring Boot，:4477）

覆盖用户、帖子、评论、点赞、收藏、关注、签到、徽章、消息、上传等资源，
由 `controller/` 下 20 个控制器提供；前端经 Vite 代理（`/api`、`/upload`、`/vercode`）转发。

## 9. 多智能体架构

| Agent | 输入 | 输出 | 职责 |
|-------|------|------|------|
| `PlannerAgent` | 用户原始提问 | 意图 / 子查询（≤3）/ 关键词 | 查询理解与改写 |
| `RetrieverAgent` | 查询文本 | 排序后的上下文片段 | 向量语义 + BM25 双通道检索，RRF 融合，迭代收敛 |
| `CriticAgent` | 问题 + 检索片段 + 生成答案 | `{satisfied, missing}` | 质量评估，触发补检重写 |
| `PersonaProvider` | 用户资料 / 收藏 / 发帖 | 画像上下文 | 生成个性化回答 |
| `Memory` | 会话记录 / 反馈 | 记忆上下文 | 多轮对话记忆 |
| `Emotion` | 用户文本 | 极性 / 情绪标签 / 紧急度 | 回答语气调节、紧急行动指引 |
| `Clarify` | 问题 + 意图 | 追问 + 选项 / 延伸话题 | 信息缺口补全 |
| `Moderator` | 帖子 / 评论文本 | 风险等级 + 处理建议 | 内容安全五维检测 |
| `Analytics` | 自然语言分析问题 | 结构化统计 + LLM 报告 | 数据洞察 |
| `HotTopic` | 帖子流 | 热点话题榜单 | 关键词聚类 + 发帖频率 |

场景无关性：情感词典、意图词典、追问话术、紧急行动指引等业务词条全部收敛于
`scene_config.py`。迁移到其他场景（电商客服、政务问答等）只需替换该文件的数据源，
引擎代码无需改动。

## 10. 测试

仓库内提供以下验证脚本（`forum_bot/`）：

| 脚本 | 验证目标 |
|------|----------|
| `test_bot.py` | 机器人主流程冒烟测试 |
| `test_rag.py` | RAG 检索 / 生成链路 |
| `test_llm.py` / `test_llm_api.py` | 本地 / 云端 LLM 调用 |
| `test_single.py` | 单帖处理链路 |
| `eval_benchmark.py` | 检索质量基准 |
| `check_api.py` / `check_comments.py` / `check_reply.py` | 论坛接口连通性与回帖校验 |

## 11. 已知限制

- 意图判断与情绪预判默认基于规则，复杂表述的准确率依赖 LLM 兜底配置（`EMOTION_USE_LLM` 等）。
- 双前端默认端口相同（5173），并存时需手动调整。
- 向量库为本地持久化，多实例部署需自行替换为共享存储。
- `config.py` 与 `application.yml` 中存在硬编码示例密钥 / 密码，生产环境须改用环境变量。

## 12. Roadmap

- Rerank 重排提升召回精度
- 前端对话式问答入口（毫秒级响应）
- 意图判断的小模型蒸馏
- 相似问题关联推荐
- 多向量库适配（Milvus / Qdrant）与多租户隔离

## 13. 许可

本项目为校园实践作品，未指定开源许可证，保留所有权利。
部署前请自行配置本地环境与模型资源；任何密钥请通过环境变量管理，勿提交至公共仓库。