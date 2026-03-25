# 停止服务脚本

#!/bin/bash

set -e

echo "🛑 停止 Ledger App Backend 服务..."

# 停止所有服务
docker-compose down

echo "✅ 服务已停止"

# 可选：清理数据卷
if [ "$1" == "--clean" ]; then
    echo "🧹 清理数据卷..."
    docker volume rm ledger-app_mysql-data
    docker volume rm ledger-app_redis-data
    docker volume rm ledger-app_minio-data
    docker volume rm ledger-app_app-logs
    echo "✅ 数据卷已清理"
fi
