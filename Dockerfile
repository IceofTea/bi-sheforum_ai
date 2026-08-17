# 多阶段构建：单 jar 全栈镜像（Spring Boot + 内嵌 H2 + Vue 前端静态资源）
# 构建：  docker build -t bi-sheforum-ai .
# 运行：  docker run -p 4477:4477 -v forum-data:/app/data bi-sheforum-ai

# ---- Stage 1: 前端 ----
FROM node:18-alpine AS fe
WORKDIR /app
COPY bi-sheforum/vue-forum-user/package*.json ./user/
RUN cd user && npm install --no-audit --no-fund
COPY bi-sheforum/vue-forum-user ./user
RUN cd user && npm run build
COPY bi-sheforum/vue-forum-admin/package*.json ./admin/
RUN cd admin && npm install --no-audit --no-fund
COPY bi-sheforum/vue-forum-admin ./admin
RUN cd admin && npm run build

# ---- Stage 2: 生成 H2 schema ----
FROM python:3-alpine AS tools
WORKDIR /t
COPY tools ./tools
COPY bi-sheforum/forum.sql ./bi-sheforum/forum.sql
COPY bi-sheforum/forum/src/main/resources/badge_migration.sql ./bi-sheforum/forum/src/main/resources/badge_migration.sql
RUN python tools/migrate_sql.py

# ---- Stage 3: 后端打包 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY bi-sheforum/forum/pom.xml ./forum/pom.xml
COPY bi-sheforum/forum/src ./forum/src
COPY --from=fe /app/user/dist ./forum/src/main/resources/static/
COPY --from=fe /app/admin/dist ./forum/src/main/resources/static/admin/
COPY --from=tools /t/bi-sheforum/forum/src/main/resources/db ./forum/src/main/resources/db
COPY bi-sheforum/upload ./forum/src/main/resources/static/upload
RUN cd forum && mvn -q -DskipTests package

# ---- Stage 4: 运行 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/forum/target/*.jar ./forum.jar
EXPOSE 4477
VOLUME ["/app/data"]
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/forum.jar --spring.profiles.active=h2"]