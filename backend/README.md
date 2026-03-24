# 全平台智能记账应用 - 后端服务

基于 Spring Boot 3.x 的全平台智能记账应用后端服务。

## 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: PostgreSQL 15
- **ORM**: MyBatis Plus + JPA
- **缓存**: Redis 7
- **对象存储**: MinIO
- **API 文档**: SpringDoc OpenAPI (Swagger)
- **认证**: JWT
- **构建工具**: Maven
- **容器化**: Docker & Docker Compose

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose (可选)

### 使用 Docker Compose 启动（推荐）

```bash
# 启动所有服务（PostgreSQL, Redis, MinIO, Application）
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### 本地开发

1. 确保 PostgreSQL、Redis、MinIO 已启动
2. 修改 `application-dev.yml` 配置
3. 运行应用：

```bash
mvn spring-boot:run
```

## 访问地址

- **API 服务**: http://localhost:8080/api
- **Swagger 文档**: http://localhost:8080/api/swagger-ui.html
- **MinIO 控制台**: http://localhost:9001

## 项目结构

```
src/main/java/com/ledger/app/
├── common/
│   ├── config/          # 配置类
│   ├── constant/        # 常量定义
│   ├── exception/       # 异常处理
│   └── result/          # 统一响应
├── modules/
│   ├── auth/           # 认证模块
│   ├── book/           # 账本模块
│   ├── category/       # 分类模块
│   ├── account/        # 账户模块
│   ├── transaction/    # 交易模块
│   └── budget/         # 预算模块
└── LedgerApplication.java
```

## 数据库初始化

数据库表结构在 `src/main/resources/schema.sql` 中定义。

使用 Docker Compose 时会自动执行初始化脚本。

## API 文档

启动应用后访问：http://localhost:8080/api/swagger-ui.html

## 开发计划

- [ ] 用户认证模块
- [ ] 账本管理
- [ ] 分类管理
- [ ] 账户管理
- [ ] 交易记录
- [ ] 预算管理
- [ ] 数据统计与分析
- [ ] 文件上传（MinIO）

## License

MIT
