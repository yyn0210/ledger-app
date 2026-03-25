# 账本应用 API 测试指南

## 认证模块 API

### API 端点列表

### 1. 用户注册
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 123456789,
      "username": "testuser",
      "email": "test@example.com",
      "nickname": "测试用户",
      "avatarUrl": null,
      "phone": "13800138000"
    }
  }
}
```

---

### 2. 用户登录
```bash
POST /api/auth/login
Content-Type: application/json

{
  "account": "testuser",
  "password": "123456",
  "rememberMe": false
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 123456789,
      "username": "testuser",
      "email": "test@example.com",
      "nickname": "测试用户",
      "avatarUrl": null,
      "phone": "13800138000"
    }
  }
}
```

---

### 3. 用户登出
```bash
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4. 获取当前用户信息
```bash
GET /api/auth/me
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": null,
    "phone": "13800138000"
  }
}
```

---

## curl 测试命令

### 注册新用户
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "123456",
    "nickname": "测试用户",
    "phone": "13800138000"
  }'
```

### 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "testuser",
    "password": "123456"
  }'
```

### 获取当前用户信息
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {你的 accessToken}"
```

### 登出
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer {你的 accessToken}"
```

---

## Swagger UI

启动应用后访问：http://localhost:8080/api/swagger-ui.html

---

## 数据库表结构

### users 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 ID |
| username | VARCHAR(50) | 用户名（唯一） |
| email | VARCHAR(100) | 邮箱（唯一） |
| password_hash | VARCHAR(255) | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| avatar_url | VARCHAR(255) | 头像 URL |
| phone | VARCHAR(20) | 手机号 |
| status | INTEGER | 状态：0-禁用，1-正常 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | INTEGER | 逻辑删除：0-未删除，1-已删除 |

---

## 安全配置

- **密码加密**: BCrypt
- **Token 类型**: JWT
- **Token 有效期**: 24 小时（86400000 毫秒）
- **公开接口**: `/api/auth/**`, `/api/swagger-ui/**`, `/api/v3/api-docs/**`
- **受保护接口**: 其他所有接口需要 Bearer Token 认证
