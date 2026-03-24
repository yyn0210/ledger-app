# 智能记账 App - Web 前端

基于 Vue3 + Vite + Naive UI 的全平台智能记账应用 Web 前端。

## 技术栈

- **框架**: Vue 3.4.x
- **构建工具**: Vite 5.x
- **UI 组件库**: Naive UI 2.x
- **状态管理**: Pinia 2.x
- **路由**: Vue Router 4.x
- **HTTP 请求**: Axios 1.x
- **图表**: ECharts 5.x
- **工具库**: lodash-es, dayjs

## 项目结构

```
ledger-app-web/
├── src/
│   ├── api/            # API 接口封装
│   ├── assets/         # 资源文件
│   ├── components/     # 公共组件
│   ├── composables/    # 组合式函数
│   ├── router/         # 路由配置
│   ├── stores/         # Pinia 状态管理
│   ├── utils/          # 工具函数
│   ├── views/          # 页面组件
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── Dockerfile          # Docker 镜像配置
├── docker-compose.yml  # Docker Compose 配置
├── package.json        # 项目依赖
├── vite.config.js      # Vite 配置
└── README.md           # 项目文档
```

## 快速开始

### 环境要求

- Node.js >= 18.x
- npm >= 9.x

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 代码检查

```bash
npm run lint
```

### 代码格式化

```bash
npm run format
```

## Docker 部署

### 构建镜像

```bash
docker build -t ledger-app-web .
```

### 使用 Docker Compose

```bash
docker-compose up -d
```

访问 http://localhost

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| VITE_APP_TITLE | 应用标题 | 智能记账 |
| VITE_API_BASE_URL | API 基础 URL | http://localhost:8080/api/v1 |
| VITE_UPLOAD_MAX_SIZE | 上传文件大小限制 | 10485760 |

## API 接口

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `GET /auth/me` - 获取当前用户信息
- `POST /auth/logout` - 退出登录

### 账本接口
- `GET /books` - 获取账本列表
- `POST /books` - 创建账本
- `GET /books/:id` - 获取账本详情
- `PUT /books/:id` - 更新账本
- `DELETE /books/:id` - 删除账本

### 交易接口
- `GET /transactions` - 获取交易列表
- `POST /transactions` - 创建交易
- `GET /transactions/:id` - 获取交易详情
- `PUT /transactions/:id` - 更新交易
- `DELETE /transactions/:id` - 删除交易

### AI 接口
- `POST /ai/classify` - AI 智能分类
- `GET /ai/analyze` - AI 智能分析
- `GET /ai/budget-suggestion` - AI 预算建议

## 开发规范

### 代码风格

- 使用 ESLint + Prettier 统一代码风格
- 使用 2 空格缩进
- 字符串使用单引号
- 行尾不加分号
- 最大行宽 100 字符

### 组件命名

- 组件文件使用 PascalCase 命名
- 组件名称使用多单词
- 示例：`TransactionForm.vue`, `CategoryPicker.vue`

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 相关文档

- [Vue3 官方文档](https://vuejs.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Naive UI 文档](https://www.naiveui.com/)
- [Pinia 文档](https://pinia.vuejs.org/)

## License

MIT
