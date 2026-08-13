# 论坛AI机器人使用说明

## 启动步骤

### 1. 确保论坛后端运行
论坛后端需要运行在 http://localhost:4477

### 2. 激活虚拟环境
```bash
cd forum_bot
call .\venv\Scripts\activate.bat
```

### 3. 启动机器人服务
```bash
python bot.py api
```
机器人会在 http://localhost:5000 启动API服务

### 4. 创建向量索引（首次运行或更新时）
```bash
python -c "from bot import ForumBot; b = ForumBot(); b.index_all_threads(force=True)"
```

### 5. 手动触发一次回复测试
```bash
python -c "from bot import ForumBot; b = ForumBot(); b.scan_and_reply(limit=10)"
```

## 运行模式

1. **api 模式**: 启动REST API服务，持续监控并自动回复
   ```bash
   python bot.py api
   ```

2. **scan 模式**: 扫描并回复一次
   ```bash
   python bot.py scan
   ```

3. **index 模式**: 重新索引所有帖子
   ```bash
   python bot.py index
   ```

## API端点

- `GET /api/bot/status` - 查看机器人状态
- `POST /api/bot/index` - 重建索引 {"force": true}
- `POST /api/bot/reply` - 手动回复 {"threadId": 1}
- `GET /api/bot/search?q=关键词` - 搜索相似帖子

## 注意

- 当前使用模板回复（如需LLM生成回复，需要配置有效的API Key）
- 机器人会自动跳过已回复的帖子
- 监控间隔为60秒