#!/usr/bin/env bash
# 每次 Codespaces 启动时拉起应用（幂等：先杀旧进程再启动）。
set -e
cd "$(dirname "$0")/.."
ROOT="$(pwd)"
JAR="$ROOT/bi-sheforum/forum/target/forum.admin-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR" ]; then
  echo "jar 不存在，请等待 postCreate 构建完成，或手动执行 bash .devcontainer/setup.sh"
  exit 1
fi

pkill -f "forum.admin-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
cd "$ROOT/bi-sheforum/forum"
nohup java -jar "$JAR" --spring.profiles.active=h2 --server.port=4477 > /tmp/forum.log 2>&1 &

for i in $(seq 1 30); do
  if curl -sf http://localhost:4477/ > /dev/null 2>&1; then
    echo "==> 论坛已启动：http://localhost:4477（Codespaces 将自动转发 4477 端口）"
    echo "==> 用户端 http://localhost:4477/ · 管理端 http://localhost:4477/admin/"
    exit 0
  fi
  sleep 2
done
echo "==> 应用启动超时，请查看 /tmp/forum.log"
exit 1