
---

## 账本管理模块 API

### 1. 获取我的账本列表
```bash
GET /api/books
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 123456789,
      "userId": 987654321,
      "name": "我的账本",
      "icon": "📊",
      "color": "#18A058",
      "type": 1,
      "isDefault": 1,
      "sortOrder": 0,
      "createdAt": "2026-03-23T10:16:00",
      "updatedAt": "2026-03-23T10:16:00"
    },
    {
      "id": 123456790,
      "userId": 987654321,
      "name": "旅行账本",
      "icon": "✈️",
      "color": "#1890FF",
      "type": 2,
      "isDefault": 0,
      "sortOrder": 1,
      "createdAt": "2026-03-23T10:20:00",
      "updatedAt": "2026-03-23T10:20:00"
    }
  ]
}
```

---

### 2. 获取账本详情
```bash
GET /api/books/{id}
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "userId": 987654321,
    "name": "我的账本",
    "icon": "📊",
    "color": "#18A058",
    "type": 1,
    "isDefault": 1,
    "sortOrder": 0,
    "createdAt": "2026-03-23T10:16:00",
    "updatedAt": "2026-03-23T10:16:00"
  }
}
```

---

### 3. 创建新账本
```bash
POST /api/books
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "旅行账本",
  "icon": "✈️",
  "color": "#1890FF",
  "type": 2,
  "setAsDefault": false,
  "sortOrder": 1
}
```

**请求参数说明:**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ✅ | 账本名称（最多 50 字符） |
| icon | String | ❌ | 图标（emoji 或 URL） |
| color | String | ❌ | 颜色（16 进制） |
| type | Integer | ❌ | 类型：1-普通，2-旅行，3-装修，4-婚礼 |
| setAsDefault | Boolean | ❌ | 是否设为默认账本 |
| sortOrder | Integer | ❌ | 排序顺序 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456790,
    "userId": 987654321,
    "name": "旅行账本",
    "icon": "✈️",
    "color": "#1890FF",
    "type": 2,
    "isDefault": 0,
    "sortOrder": 1,
    "createdAt": "2026-03-23T10:20:00",
    "updatedAt": "2026-03-23T10:20:00"
  }
}
```

---

### 4. 更新账本
```bash
PUT /api/books/{id}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "更新的账本名称",
  "icon": "📝",
  "color": "#FA8C16",
  "sortOrder": 2,
  "setAsDefault": true
}
```

**请求参数说明:**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ❌ | 账本名称 |
| icon | String | ❌ | 图标 |
| color | String | ❌ | 颜色 |
| sortOrder | Integer | ❌ | 排序顺序 |
| setAsDefault | Boolean | ❌ | 是否设为默认账本 |

---

### 5. 删除账本（软删除）
```bash
DELETE /api/books/{id}
Authorization: Bearer {accessToken}
```

**注意:** 默认账本不能删除

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 6. 获取或创建默认账本
```bash
GET /api/books/default
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123456789,
    "userId": 987654321,
    "name": "我的账本",
    "icon": "📊",
    "color": "#18A058",
    "type": 1,
    "isDefault": 1,
    "sortOrder": 0,
    "createdAt": "2026-03-23T10:16:00",
    "updatedAt": "2026-03-23T10:16:00"
  }
}
```

---

## curl 测试命令

### 获取账本列表
```bash
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer {你的 accessToken}"
```

### 创建账本
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {你的 accessToken}" \
  -d '{
    "name": "旅行账本",
    "icon": "✈️",
    "color": "#1890FF",
    "type": 2,
    "setAsDefault": false
  }'
```

### 更新账本
```bash
curl -X PUT http://localhost:8080/api/books/123456789 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {你的 accessToken}" \
  -d '{
    "name": "更新的名称",
    "sortOrder": 1
  }'
```

### 删除账本
```bash
curl -X DELETE http://localhost:8080/api/books/123456789 \
  -H "Authorization: Bearer {你的 accessToken}"
```

---

## 数据库表结构

### books 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 ID |
| user_id | BIGINT | 用户 ID（外键） |
| name | VARCHAR(50) | 账本名称 |
| icon | VARCHAR(50) | 图标 |
| color | VARCHAR(20) | 颜色 |
| type | INTEGER | 类型：1-普通，2-旅行，3-装修，4-婚礼 |
| is_default | INTEGER | 是否默认：0-否，1-是 |
| sort_order | INTEGER | 排序顺序 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | INTEGER | 逻辑删除：0-未删除，1-已删除 |

**索引:**
- `idx_books_user_id` - 用户 ID 索引

---

## 权限控制逻辑

### 1. JWT Token 验证
- 所有账本接口都需要 Bearer Token 认证
- 通过 `JwtAuthenticationFilter` 解析 Token 获取用户 ID
- 未认证请求返回 401

### 2. 数据隔离
- 用户只能访问自己的账本（通过 `user_id` 过滤）
- 查询时强制添加 `WHERE user_id = ? AND deleted = 0`
- 操作前验证账本归属（`findByIdAndUserId`）

### 3. 业务规则
- 每个用户必须有且仅有一个默认账本
- 创建新账本并设为默认时，自动取消原默认账本
- 默认账本不能删除
- 删除采用软删除（`deleted = 1`）

### 4. 异常处理
| 场景 | 错误码 | 错误信息 |
|------|--------|----------|
| 未认证 | 401 | 未认证 |
| 账本不存在 | 404 | 账本不存在或无权访问 |
| 删除默认账本 | 400 | 不能删除默认账本 |
