# AI Service - 全平台智能记账 App AI 服务

## 项目简介

基于 FastAPI 的 AI 服务，提供小票 OCR 识别、截图识别、语音 STT、语义理解等功能。

## 技术栈

- **框架**: FastAPI
- **Python**: 3.10+
- **大模型服务**:
  - 阿里百炼 DashScope（Qwen-VL OCR、Qwen 文本）
  - DeepSeek（语义理解）
  - 讯飞星火（语音 STT）
- **缓存**: Redis
- **服务器**: Uvicorn

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置环境变量

创建 `.env` 文件：

```env
# 阿里百炼
DASHSCOPE_API_KEY=your-dashscope-api-key

# DeepSeek
DEEPSEEK_API_KEY=your-deepseek-api-key

# 讯飞星火
IFLYTEK_APP_ID=your-iflytek-app-id
IFLYTEK_API_KEY=your-iflytek-api-key
IFLYTEK_API_SECRET=your-iflytek-api-secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 3. 启动服务

```bash
# 开发模式
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 生产模式
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### 4. 访问 API 文档

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## API 接口

### OCR 识别

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/ocr/receipt` | 小票 OCR 识别 |
| POST | `/api/ocr/screenshot` | 截图识别 |
| POST | `/api/ocr/upload` | 上传图片识别 |

### 语音识别

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/stt` | 语音转文字 |

### 语义理解

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/nlp/classify` | 交易智能分类 |
| GET | `/api/nlp/prompts` | Prompt 模板列表 |
| POST | `/api/nlp/prompts/{name}/render` | 渲染 Prompt 模板 |

### 系统接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/` | 应用信息 |
| GET | `/health` | 健康检查 |

## 项目结构

```
ai-service/
├── app/
│   ├── main.py          # FastAPI 入口
│   ├── api/             # API 路由
│   │   ├── ocr.py       # OCR 接口
│   │   ├── stt.py       # 语音识别
│   │   └── nlp.py       # 语义理解
│   ├── services/        # 服务层
│   │   ├── qwen.py      # 通义千问
│   │   ├── deepseek.py  # DeepSeek
│   │   └── iflytek.py   # 讯飞
│   ├── core/            # 核心配置
│   │   ├── config.py    # 配置管理
│   │   └── prompts.py   # Prompt 模板
│   └── utils/           # 工具函数
│       └── redis.py     # Redis 客户端
├── requirements.txt
├── .env.example
├── README.md
└── logs/
```

## 开发说明

### 添加新的 AI 服务

1. 在 `app/services/` 目录创建服务类
2. 在 `app/api/` 目录创建对应的 API 路由
3. 在 `app/main.py` 中注册路由

### 添加新的 Prompt 模板

在 `app/core/prompts.py` 的 `_init_templates` 方法中添加：

```python
self.add_template(PromptTemplate(
    name="your_template_name",
    template="Your prompt template here",
    description="Template description"
))
```

## 部署

### Docker 部署（推荐）

```bash
docker build -t ledger-ai-service .
docker run -p 8000:8000 --env-file .env ledger-ai-service
```

### 直接部署

```bash
# 安装依赖
pip install -r requirements.txt

# 启动服务
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

## 日志

日志文件位于 `logs/ai-service.log`，按 500MB 轮转，保留 10 天。

## 许可证

Apache 2.0

## 联系方式

- 作者：Chisong (赤松)
- 团队：简洛团队
