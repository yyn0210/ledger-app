# ledger-app-backend

> 🧾 全平台智能记账 App - Spring Boot 3.x 后端服务

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

---

## 📖 项目简介

ledger-app-backend 是全平台智能记账 App 的后端服务，基于 Spring Boot 3.x 构建，提供 RESTful API 接口，支持 Web 端、移动端和 AI 服务的数据交互。

### 核心功能

- 🔐 用户认证与授权（JWT）
- 📚 账本管理（多账本支持）
- 💰 交易记录 CRUD
- 📊 预算管理
- 🔄 周期账单
- 📤 数据导出（Excel/CSV）
- 🔌 WebSocket 实时同步
- 🔔 推送通知

---

## 🛠️ 技术栈

### 核心框架
- **Java**: 17+
- **Spring Boot**: 3.x
- **Spring Security**: 6.x (JWT 认证)
- **Spring Data JPA**: 数据持久化

### 数据库
- **PostgreSQL**: 主数据库
- **Redis**: 缓存 + 会话管理

### 其他依赖
- **Lombok**: 简化 Java 代码
- **MapStruct**: DTO 映射
- **Swagger/OpenAPI**: API 文档
- **Flyway**: 数据库迁移

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- PostgreSQL 14+
- Redis 7+
- Maven 3.8+

### 安装步骤

```bash
# 1. 克隆仓库
git clone https://github.com/yyn0210/ledger-app-backend.git
cd ledger-app-backend

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，配置数据库连接等

# 3. 创建数据库
createdb ledger_app

# 4. 构建项目
mvn clean install

# 5. 运行应用
mvn spring-boot:run
```

### 配置文件

```yaml
# application.yml 关键配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ledger_app
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  redis:
    host: localhost
    port: 6379

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 小时
```

---

## 📡 API 文档

启动应用后访问：
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 主要接口

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `/api/auth/*` | 登录/注册/刷新令牌 |
| 用户 | `/api/users/*` | 用户信息管理 |
| 账本 | `/api/books/*` | 账本 CRUD |
| 交易 | `/api/transactions/*` | 交易记录管理 |
| 预算 | `/api/budgets/*` | 预算管理 |
| 周期账单 | `/api/recurring/*` | 周期账单配置 |
| 导出 | `/api/export/*` | 数据导出 |

---

## 📁 项目结构

```
ledger-app-backend/
├── src/main/java/com/ledger/app/
│   ├── modules/          # 业务模块
│   │   ├── auth/        # 认证模块
│   │   ├── user/        # 用户模块
│   │   ├── book/        # 账本模块
│   │   ├── transaction/ # 交易模块
│   │   ├── budget/      # 预算模块
│   │   ├── recurring/   # 周期账单
│   │   └── export/      # 导出模块
│   ├── common/           # 公共组件
│   │   ├── config/      # 配置类
│   │   ├── exception/   # 异常处理
│   │   └── util/        # 工具类
│   └── LedgerApplication.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/    # Flyway 迁移脚本
├── pom.xml
└── README.md
```

---

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

---

## 📦 部署

### Docker 部署

```bash
# 构建镜像
docker build -t ledger-app-backend:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PASSWORD=secret \
  ledger-app-backend:latest
```

### 生产环境

1. 配置环境变量
2. 启用 HTTPS
3. 配置数据库连接池
4. 启用 Redis 集群
5. 配置日志收集

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

## 👥 团队

- **架构师**: qingsong (青松)
- **后端开发**: chisong (赤松)
- **组织**: yyn0210

---

## 📞 联系方式

- **GitHub**: https://github.com/yyn0210/ledger-app-backend
- **Issues**: https://github.com/yyn0210/ledger-app-backend/issues

---

**Made with ❤️ by the ledger-app team**
