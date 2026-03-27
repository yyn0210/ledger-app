# 快速启动脚本

#!/bin/bash

set -e

echo "🚀 启动 Ledger App Backend 服务..."

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 停止并清理旧容器
echo "🧹 清理旧容器..."
docker-compose down

# 启动服务
echo "🐳 启动 MySQL, Redis, MinIO 和后端服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

# 显示日志
echo ""
echo "📋 查看日志：docker-compose logs -f"
echo "🌐 访问 Swagger: http://localhost:8080/swagger-ui.html"
echo "🔍 访问 Actuator: http://localhost:8080/api/actuator/health"
echo ""
echo "✅ 服务启动完成！"
