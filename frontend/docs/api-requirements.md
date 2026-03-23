# 前端 API 接口需求清单

**文档版本**: v1.0  
**创建时间**: 2026-03-23  
**前端负责人**: qianduan1  
**后端负责人**: houduan1

---

## 优先级说明

| 优先级 | 说明 | 对应功能 |
|--------|------|----------|
| **P0** | 必需接口，核心功能依赖 | 登录/注册、记账、账本、分类、账户、交易 CRUD |
| **P1** | 重要接口，增强用户体验 | 统计数据、导出功能 |
| **P2** | 可选接口，后续迭代 | 高级功能、优化体验 |

---

## 1. 用户认证（✅ 后端已完成）

### 1.1 用户登录
- **接口**: `POST /api/auth/login`
- **优先级**: P0
- **请求参数**:
  ```json
  {
    "username": "string",      // 用户名/邮箱/手机号
    "password": "string"       // 密码
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "string",       // JWT token
      "expiresIn": 7200,       // 过期时间（秒）
      "user": {
        "id": 1,
        "username": "string",
        "nickname": "string",
        "email": "string",
        "phone": "string",
        "avatar": "string"
      }
    }
  }
  ```

### 1.2 用户注册
- **接口**: `POST /api/auth/register`
- **优先级**: P0
- **请求参数**:
  ```json
  {
    "username": "string",      // 用户名（4-20 位字母或数字）
    "email": "string",         // 邮箱
    "phone": "string",         // 手机号（可选）
    "password": "string"       // 密码（6-20 位）
  }
  ```
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "id": 1,
      "username": "string"
    }
  }
  ```

### 1.3 用户登出
- **接口**: `POST /api/auth/logout`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **响应格式**:
  ```json
  {
    "code": 200,
    "message": "登出成功"
  }
  ```

### 1.4 刷新 Token
- **接口**: `POST /api/auth/refresh`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "token": "string",
      "expiresIn": 7200
    }
  }
  ```

### 1.5 获取当前用户信息
- **接口**: `GET /api/auth/me`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "id": 1,
      "username": "string",
      "nickname": "string",
      "email": "string",
      "phone": "string",
      "avatar": "string",
      "createdAt": "2026-03-23T00:00:00Z"
    }
  }
  ```

### 1.6 修改密码
- **接口**: `PUT /api/auth/password`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "oldPassword": "string",
    "newPassword": "string"
  }
  ```

### 1.7 更新用户资料
- **接口**: `PUT /api/auth/profile`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "nickname": "string",
    "email": "string",
    "phone": "string",
    "avatar": "string"
  }
  ```

---

## 2. 账本管理

### 2.1 获取账本列表
- **接口**: `GET /api/books`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `page`: 页码（默认 1）
  - `pageSize`: 每页数量（默认 10）
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        {
          "id": 1,
          "name": "string",
          "description": "string",
          "color": "#3385ff",
          "icon": "string",
          "memberCount": 1,
          "recordCount": 100,
          "isDefault": true,
          "createdAt": "2026-03-23T00:00:00Z"
        }
      ],
      "total": 10,
      "page": 1,
      "pageSize": 10
    }
  }
  ```

### 2.2 创建账本
- **接口**: `POST /api/books`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "name": "string",          // 账本名称（必填，1-20 字）
    "description": "string",    // 描述（可选，0-100 字）
    "color": "#3385ff",        // 图标颜色
    "icon": "string"           // 图标名称
  }
  ```

### 2.3 更新账本
- **接口**: `PUT /api/books/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**: 同创建账本

### 2.4 删除账本
- **接口**: `DELETE /api/books/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **响应**: 删除成功返回 200

### 2.5 切换当前账本
- **接口**: `PUT /api/books/:id/switch`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
  ```json
  {
    "code": 200,
    "message": "已切换到账本 xxx"
  }
  ```

### 2.6 获取账本详情
- **接口**: `GET /api/books/:id`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`

---

## 3. 分类管理

### 3.1 获取分类列表
- **接口**: `GET /api/categories`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `type`: 分类类型（expense/income）
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "defaults": [   // 默认分类（不可删除）
        {
          "id": 1,
          "name": "餐饮",
          "type": "expense",
          "color": "#ff9900",
          "icon": "FastFood",
          "sort": 1,
          "isDefault": true
        }
      ],
      "customs": [    // 自定义分类
        {
          "id": 101,
          "name": "自定义分类",
          "type": "expense",
          "color": "#95a5a6",
          "icon": "ellipsisHorizontal",
          "sort": 99,
          "isDefault": false
        }
      ]
    }
  }
  ```

### 3.2 创建分类
- **接口**: `POST /api/categories`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "name": "string",        // 分类名称（必填，1-10 字）
    "type": "expense",       // 分类类型（expense/income）
    "color": "#ff9900",      // 图标颜色
    "icon": "string",        // 图标名称
    "sort": 1                // 排序（数字越小越靠前）
  }
  ```

### 3.3 更新分类
- **接口**: `PUT /api/categories/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**: 同创建分类

### 3.4 删除分类
- **接口**: `DELETE /api/categories/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **注意**: 默认分类不可删除，返回 403

---

## 4. 账户管理

### 4.1 获取账户列表
- **接口**: `GET /api/accounts`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "id": 1,
        "name": "string",
        "type": "cash",          // cash/bank/credit/alipay/wechat/other
        "typeName": "现金",
        "color": "#52c41a",
        "icon": "Cash",
        "balance": 1500.00,      // 余额（负数表示负债）
        "note": "string",
        "createdAt": "2026-03-23T00:00:00Z"
      }
    ]
  }
  ```

### 4.2 创建账户
- **接口**: `POST /api/accounts`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "name": "string",        // 账户名称（必填）
    "type": "cash",          // 账户类型
    "balance": 0,            // 初始余额
    "note": "string"         // 备注（可选）
  }
  ```

### 4.3 更新账户
- **接口**: `PUT /api/accounts/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**: 同创建账户

### 4.4 删除账户
- **接口**: `DELETE /api/accounts/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`

### 4.5 获取账户总览
- **接口**: `GET /api/accounts/summary`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "totalAssets": 38451.60,    // 总资产
      "totalDebt": 3200.00,       // 总负债
      "netAssets": 35251.60       // 净资产
    }
  }
  ```

---

## 5. 交易记录

### 5.1 获取交易列表
- **接口**: `GET /api/transactions`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `bookId`: 账本 ID（可选）
  - `type`: 交易类型（expense/income，可选）
  - `categoryId`: 分类 ID（可选）
  - `startDate`: 开始日期（可选）
  - `endDate`: 结束日期（可选）
  - `page`: 页码（默认 1）
  - `pageSize`: 每页数量（默认 20）
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        {
          "id": 1,
          "type": "expense",
          "amount": 68.00,
          "categoryId": 1,
          "categoryName": "餐饮",
          "categoryIcon": "FastFood",
          "categoryColor": "#ff9900",
          "bookId": 1,
          "bookName": "日常账本",
          "accountId": 1,
          "accountName": "支付宝",
          "date": "2026-03-23",
          "note": "午餐",
          "createdAt": "2026-03-23T12:00:00Z"
        }
      ],
      "total": 100,
      "page": 1,
      "pageSize": 20
    }
  }
  ```

### 5.2 创建交易
- **接口**: `POST /api/transactions`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "type": "expense",       // expense/income
    "amount": 68.00,         // 金额（必填）
    "categoryId": 1,         // 分类 ID（必填）
    "bookId": 1,             // 账本 ID（必填）
    "accountId": 1,          // 账户 ID（可选）
    "date": "2026-03-23",    // 交易日期（可选，默认今天）
    "note": "string"         // 备注（可选）
  }
  ```

### 5.3 更新交易
- **接口**: `PUT /api/transactions/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**: 同创建交易

### 5.4 删除交易
- **接口**: `DELETE /api/transactions/:id`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`

### 5.5 获取交易详情
- **接口**: `GET /api/transactions/:id`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`

---

## 6. 统计数据

### 6.1 月度统计
- **接口**: `GET /api/stats/monthly`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `year`: 年份（可选，默认当前年）
  - `month`: 月份（可选，默认当前月）
  - `bookId`: 账本 ID（可选）
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": {
      "totalExpense": 3280.50,     // 总支出
      "totalIncome": 8500.00,      // 总收入
      "balance": 5219.50,          // 结余
      "dailyExpense": 109.35,      // 日均支出
      "dailyIncome": 283.33,       // 日均收入
      "balanceRate": 61.4          // 结余率（%）
    }
  }
  ```

### 6.2 分类统计
- **接口**: `GET /api/stats/category`
- **优先级**: P0
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `type`: 统计类型（expense/income）
  - `year`: 年份
  - `month`: 月份
  - `bookId`: 账本 ID（可选）
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "categoryId": 1,
        "categoryName": "餐饮",
        "categoryIcon": "FastFood",
        "categoryColor": "#ff9900",
        "amount": 1280.50,
        "percent": 39.0
      }
    ]
  }
  ```

### 6.3 趋势数据
- **接口**: `GET /api/stats/trend`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `type`: 趋势类型（expense/income）
  - `range`: 时间范围（day/week/month/year）
  - `startDate`: 开始日期
  - `endDate`: 结束日期
- **响应格式**:
  ```json
  {
    "code": 200,
    "data": [
      {
        "date": "2026-03-01",
        "expense": 120.00,
        "income": 0
      }
    ]
  }
  ```

### 6.4 支出排行
- **接口**: `GET /api/stats/ranking`
- **优先级**: P1
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `year`: 年份
  - `month`: 月份
  - `limit`: 返回数量（默认 10）
- **响应格式**: 同分类统计

---

## 7. 数据导出（P2）

### 7.1 导出数据
- **接口**: `POST /api/export`
- **优先级**: P2
- **请求头**: `Authorization: Bearer {token}`
- **请求参数**:
  ```json
  {
    "format": "excel",         // excel/csv/pdf
    "type": "transactions",    // 导出类型
    "startDate": "2026-01-01",
    "endDate": "2026-03-23",
    "bookId": 1                // 账本 ID（可选）
  }
  ```
- **响应**: 返回文件下载链接或二进制流

---

## 8. 错误响应格式

所有接口错误统一返回格式：

```json
{
  "code": 400,              // 错误码
  "message": "错误信息",     // 错误描述
  "data": null
}
```

**常见错误码**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权/Token 过期
- `403`: 禁止访问（如删除默认分类）
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## 9. 通用规范

### 9.1 认证方式
- 所有需要认证的接口需在请求头中携带 JWT Token
- 格式：`Authorization: Bearer {token}`

### 9.2 日期格式
- 请求和响应中的日期统一使用 ISO 8601 格式
- 示例：`2026-03-23T12:00:00Z` 或 `2026-03-23`

### 9.3 金额格式
- 所有金额字段使用数字类型，保留 2 位小数
- 示例：`1280.50`

### 9.4 分页规范
- 页码从 1 开始
- 默认每页 20 条
- 响应包含 total、page、pageSize 字段

---

## 10. 接口开发优先级

### 第一阶段（P0 - 核心功能）
- [x] 用户认证（已完成）
- [ ] 账本 CRUD
- [ ] 分类 CRUD
- [ ] 账户 CRUD
- [ ] 交易记录 CRUD

### 第二阶段（P1 - 增强功能）
- [ ] 月度统计
- [ ] 分类统计
- [ ] 趋势数据
- [ ] 账户总览

### 第三阶段（P2 - 可选功能）
- [ ] 数据导出
- [ ] 高级筛选

---

**文档结束**
