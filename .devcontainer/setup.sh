#!/usr/bin/env bash
# 一次性初始化（Codespaces 容器创建后执行）：构建前端、生成 H2 schema、打包后端单 jar。
set -e
cd "$(dirname "$0")/.."
ROOT="$(pwd)"
echo "==> [1/5] 构建用户端前端 ..."
cd "$ROOT/bi-sheforum/vue-forum-user"
npm install --no-audit --no-fund
npm run build
rm -rf "$ROOT/bi-sheforum/forum/src/main/resources/static"/*
mkdir -p "$ROOT/bi-sheforum/forum/src/main/resources/static"
cp -r dist/* "$ROOT/bi-sheforum/forum/src/main/resources/static/"

echo "==> [2/5] 构建管理端前端 ..."
cd "$ROOT/bi-sheforum/vue-forum-admin"
npm install --no-audit --no-fund
npm run build
mkdir -p "$ROOT/bi-sheforum/forum/src/main/resources/static/admin"
cp -r dist/* "$ROOT/bi-sheforum/forum/src/main/resources/static/admin/"

echo "==> [3/5] 内置示例图片 ..."
mkdir -p "$ROOT/bi-sheforum/forum/src/main/resources/static/upload"
cp "$ROOT"/bi-sheforum/upload/*.jpg "$ROOT/bi-sheforum/forum/src/main/resources/static/upload/" 2>/dev/null || true

echo "==> [4/5] 生成 H2 兼容 schema ..."
python3 "$ROOT/tools/migrate_sql.py"

echo "==> [5/5] 打包后端单 jar ..."
cd "$ROOT/bi-sheforum/forum"
mvn -q -DskipTests package

echo "==> 构建完成：$ROOT/bi-sheforum/forum/target/forum.admin-0.0.1-SNAPSHOT.jar"