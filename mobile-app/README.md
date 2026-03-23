# 智能记账 - 移动端

基于 uni-app 开发的智能记账应用，支持 H5 和微信小程序。

## 技术栈

- **框架**: uni-app (Vue 3)
- **UI 库**: uView UI 2.x
- **状态管理**: Pinia
- **HTTP 请求**: Axios
- **构建工具**: Vite 5.x

## 功能特性

- ✅ 首页：月度概览、快捷操作、最近记录
- ✅ 记账：支出/收入记录、分类选择、备注
- ✅ 账本：多账本管理
- ✅ 统计：支出统计、分类排行
- ✅ 我的：用户信息、功能菜单

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发

```bash
# H5 开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin
```

### 构建

```bash
# H5 构建
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin
```

## 项目结构

```
mobile/
├── src/
│   ├── api/              # API 接口
│   │   ├── request.js    # 请求封装
│   │   ├── auth.js       # 认证接口
│   │   ├── book.js       # 账本接口
│   │   └── transaction.js # 交易接口
│   ├── pages/            # 页面
│   │   ├── index/        # 首页
│   │   ├── account/      # 记账页
│   │   ├── book/         # 账本页
│   │   ├── stats/        # 统计页
│   │   └── mine/         # 我的页面
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.js       # 用户状态
│   │   ├── book.js       # 账本状态
│   │   └── app.js        # 应用状态
│   ├── components/       # 组件
│   ├── utils/            # 工具函数
│   ├── static/           # 静态资源
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件
│   ├── pages.json        # 页面配置
│   └── manifest.json     # 应用配置
├── .env                  # 环境变量
├── .env.h5               # H5 环境变量
├── .env.mp-weixin        # 微信小程序环境变量
├── vite.config.js        # Vite 配置
└── package.json          # 项目配置
```

## 环境配置

编辑 `.env` 系列文件配置 API 地址：

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## 开发规范

- 使用 Composition API (`<script setup>`)
- 使用 SCSS 预处理器
- 遵循 Vue 3 最佳实践
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

## 待办事项

- [ ] 集成图表库（ECharts/F2）
- [ ] 登录/注册页面
- [ ] OCR 识别功能
- [ ] 语音记账功能
- [ ] 离线记账功能
- [ ] 数据同步功能

## License

MIT
