# 分类管理模块 API 测试指南

## API 端点列表

### 1. 获取分类列表（按类型分组）
```bash
GET /api/categories?bookId={bookId}
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "1": [
      {
        "id": 1,
        "bookId": 0,
        "parentId": 0,
        "name": "餐饮",
        "icon": "🍕",
        "type": 1,
        "sortOrder": 1,
        "isPreset": 1,
        "createdAt": "2026-03-23T10:00:00",
        "updatedAt": "2026-03-23T10:00:00"
      },
      {
        "id": 2,
        "bookId": 0,
        "parentId": 0,
        "name": "交通",
        "icon": "🚗",
        "type": 1,
        "sortOrder": 2,
        "isPreset": 1,
        "createdAt": "2026-03-23T10:00:00",
        "updatedAt": "2026-03-23T10:00:00"
      }
    ],
    "2": [
      {
        "id": 11,
        "bookId": 0,
        "parentId": 0,
        "name": "工资",
        "icon": "💰",
        "type": 2,
        "sortOrder": 1,
        "isPreset": 1,
        "createdAt": "2026-03-23T10:00:00",
        "updatedAt": "2026-03-23T10:00:00"
      }
    ]
  }
}
```

**说明:**
- `type=1`: 支出分类
- `type=2`: 收入分类
- `bookId=0`: 预置分类（全局可用）
- `isPreset=1`: 预置分类（不可修改/删除）

---

### 2. 获取分类详情
```bash
GET /api/categories/{id}?bookId={bookId}
Authorization: Bearer {accessToken}
```

---

### 3. 获取预置分类模板
```bash
GET /api/categories/presets
Authorization: Bearer {accessToken}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "1": [
      {"id": 1, "name": "餐饮", "icon": "🍕", "type": 1},
      {"id": 2, "name": "交通", "icon": "🚗", "type": 1},
      {"id": 3, "name": "购物", "icon": "🛍️", "type": 1},
      {"id": 4, "name": "娱乐", "icon": "🎬", "type": 1},
      {"id": 5, "name": "居住", "icon": "🏠", "type": 1},
      {"id": 6, "name": "医疗", "icon": "🏥", "type": 1},
      {"id": 7, "name": "教育", "icon": "📚", "type": 1},
      {"id": 8, "name": "通讯", "icon": "📱", "type": 1},
      {"id": 9, "name": "人情", "icon": "🧧", "type": 1},
      {"id": 10, "name": "其他", "icon": "📦", "type": 1}
    ],
    "2": [
      {"id": 11, "name": "工资", "icon": "💰", "type": 2},
      {"id": 12, "name": "奖金", "icon": "🎁", "type": 2},
      {"id": 13, "name": "理财", "icon": "📈", "type": 2},
      {"id": 14, "name": "兼职", "icon": "💼", "type": 2},
      {"id": 15, "name": "其他", "icon": "📦", "type": 2}
    ]
  }
}
```

---

### 4. 创建自定义分类
```bash
POST /api/categories
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "bookId": 123456789,
  "parentId": 0,
  "name": "外卖",
  "icon": "🍔",
  "type": 1,
  "sortOrder": 11
}
```

**请求参数说明:**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookId | Long | ✅ | 账本 ID |
| parentId | Long | ❌ | 父分类 ID（0 表示一级分类） |
| name | String | ✅ | 分类名称（最多 50 字符） |
| icon | String | ❌ | 图标（emoji 或 URL） |
| type | Integer | ✅ | 类型：1-支出，2-收入 |
| sortOrder | Integer | ❌ | 排序顺序 |

---

### 5. 更新分类
```bash
PUT /api/categories/{id}?bookId={bookId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": " updated 名称",
  "icon": "🍟",
  "sortOrder": 5
}
```

**注意:** 预置分类不可修改

---

### 6. 删除分类
```bash
DELETE /api/categories/{id}?bookId={bookId}
Authorization: Bearer {accessToken}
```

**注意:** 
- 预置分类不可删除
- 有子分类时不可删除父分类

---

### 7. 初始化账本分类
```bash
POST /api/categories/init?bookId={bookId}
Authorization: Bearer {accessToken}
```

**说明:** 将预置分类复制到指定账本

---

## curl 测试命令

### 获取分类列表
```bash
curl -X GET "http://localhost:8080/api/categories?bookId=123456789" \
  -H "Authorization: Bearer {你的 accessToken}"
```

### 获取预置分类
```bash
curl -X GET http://localhost:8080/api/categories/presets \
  -H "Authorization: Bearer {你的 accessToken}"
```

### 创建分类
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {你的 accessToken}" \
  -d '{
    "bookId": 123456789,
    "name": "外卖",
    "icon": "🍔",
    "type": 1,
    "sortOrder": 11
  }'
```

### 更新分类
```bash
curl -X PUT "http://localhost:8080/api/categories/100?bookId=123456789" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {你的 accessToken}" \
  -d '{
    "name": "updated 名称",
    "icon": "🍟"
  }'
```

### 删除分类
```bash
curl -X DELETE "http://localhost:8080/api/categories/100?bookId=123456789" \
  -H "Authorization: Bearer {你的 accessToken}"
```

---

## 数据库表结构

### categories 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 ID |
| book_id | BIGINT | 账本 ID（0=预置分类） |
| parent_id | BIGINT | 父分类 ID（0=一级分类） |
| name | VARCHAR(50) | 分类名称 |
| icon | VARCHAR(50) | 图标 |
| type | INTEGER | 类型：1-支出，2-收入 |
| sort_order | INTEGER | 排序顺序 |
| is_preset | INTEGER | 是否预置：0-否，1-是 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | INTEGER | 逻辑删除 |

**索引:**
- `idx_categories_book_id` - 账本 ID 索引

---

## 预置分类列表

### 支出分类 (type=1)

| ID | 名称 | 图标 | 排序 |
|----|------|------|------|
| 1 | 餐饮 | 🍕 | 1 |
| 2 | 交通 | 🚗 | 2 |
| 3 | 购物 | 🛍️ | 3 |
| 4 | 娱乐 | 🎬 | 4 |
| 5 | 居住 | 🏠 | 5 |
| 6 | 医疗 | 🏥 | 6 |
| 7 | 教育 | 📚 | 7 |
| 8 | 通讯 | 📱 | 8 |
| 9 | 人情 | 🧧 | 9 |
| 10 | 其他 | 📦 | 10 |

### 收入分类 (type=2)

| ID | 名称 | 图标 | 排序 |
|----|------|------|------|
| 11 | 工资 | 💰 | 1 |
| 12 | 奖金 | 🎁 | 2 |
| 13 | 理财 | 📈 | 3 |
| 14 | 兼职 | 💼 | 4 |
| 15 | 其他 | 📦 | 5 |

---

## 业务规则

### 1. 预置分类
- `book_id=0` 表示全局预置分类
- 所有用户都可以使用预置分类
- 预置分类**不可修改、不可删除**
- 可以通过 `initBookCategories` 复制到用户账本

### 2. 自定义分类
- 用户可以为自己的账本创建自定义分类
- 自定义分类可以修改和删除
- 删除前需先删除子分类

### 3. 父子分类
- `parent_id=0` 表示一级分类
- 支持二级分类（不支持更深层次）
- 删除父分类前必须先删除子分类

### 4. 分类类型
- `type=1`: 支出分类
- `type=2`: 收入分类
- 分类创建后类型不可修改

---

## 异常处理

| 场景 | 错误码 | 错误信息 |
|------|--------|----------|
| 未认证 | 401 | 未认证 |
| 分类不存在 | 404 | 分类不存在 |
| 修改预置分类 | 400 | 预置分类不可修改 |
| 删除预置分类 | 400 | 预置分类不可删除 |
| 删除有子分类的父分类 | 400 | 请先删除子分类 |
