@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 校园智脑 · 一键启动
color 0A

echo ============================================================
echo   校园智脑 — LLM 多智能体问答助手 一键启动
echo   参赛组别：开放赛题一（生成式大语言模型与智能体）
echo ============================================================
echo.

set "BOT_DIR=%~dp0"

echo [1/6] 检测 Python 环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo   [错误] 未检测到 Python，请先安装 Python 3.9+ 并加入 PATH
    pause
    exit /b 1
)
for /f "tokens=2 delims= " %%v in ('python --version 2^>^&1') do echo   Python %%v OK

echo.
echo [2/6] 检测 Ollama...
curl -s http://localhost:11434/api/tags >nul 2>&1
if errorlevel 1 (
    echo   [提示] Ollama 未运行，请先启动：ollama serve
    echo   [提示] 并拉取模型：ollama pull deepseek-r1:1.5b ^&^& ollama pull nomic-embed-text
    pause
    exit /b 1
)
echo   Ollama 服务 OK

echo.
echo [3/6] 安装依赖（首次运行）...
cd /d "%BOT_DIR%"
if exist requirements.txt (
    pip install -r requirements.txt -q
)

echo.
echo [4/6] 启动 Mock 论坛后端（端口 4477）...
start "校园智脑-Mock论坛" cmd /k "cd /d %BOT_DIR% && python mock_forum.py"
timeout /t 3 /nobreak >nul

echo.
echo [5/6] 启动智能体 API 服务 + 展示面板（端口 5000）...
start "校园智脑-智能体引擎" cmd /k "cd /d %BOT_DIR% && python bot.py api"

echo.
echo [6/6] 等待服务就绪并打开演示面板...
timeout /t 8 /nobreak >nul
start http://localhost:5000

echo.
echo ============================================================
echo   启动完成！浏览器将打开 http://localhost:5000
echo   - 若向量索引未建立，请等待服务自动加载
echo   - 关闭窗口即可停止服务
echo   提示：修改 config.py 可切换 LLM 后端 / 模型 / 场景
echo        场景资源统一在 scene_config.py 中配置
echo ============================================================
pause
