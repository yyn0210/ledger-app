"""
AI 服务 FastAPI 入口
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from app.core.config import settings
from app.api import ocr, stt, nlp

# 导入 models（用于 Swagger 文档中的 schema 定义）
from app.models import ocr as ocr_models
from app.models import stt as stt_models
from app.models import nlp as nlp_models
from app.models import recommend as recommend_models
from app.enums import screenshot as screenshot_enums
# 导入服务（用于初始化）
from app.services import prompt_manager as prompt_service

# 配置日志
logger.add(
    settings.LOG_FILE,
    rotation="500 MB",
    retention="10 days",
    level=settings.LOG_LEVEL
)

# 创建 FastAPI 应用
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="全平台智能记账 App - AI 服务",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境需要限制
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(ocr.router)
app.include_router(stt.router)
app.include_router(nlp.router)

# 导入推荐路由（避免循环导入）
from app.api import recommend as recommend_router
app.include_router(recommend_router.router)

# 导入 Prompt 路由
from app.api import prompt as prompt_router
app.include_router(prompt_router.router, prefix="")


@app.on_event("startup")
async def startup_event():
    """应用启动事件"""
    logger.info(f"{settings.APP_NAME} v{settings.APP_VERSION} starting...")
    logger.info(f"DashScope API: {'configured' if settings.DASHSCOPE_API_KEY else 'not configured'}")
    logger.info(f"DeepSeek API: {'configured' if settings.DEEPSEEK_API_KEY else 'not configured'}")
    logger.info(f"iFlytek API: {'configured' if settings.IFLYTEK_APP_ID else 'not configured'}")


@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭事件"""
    logger.info(f"{settings.APP_NAME} shutting down...")


@app.get("/")
async def root():
    """根路径"""
    return {
        "name": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {
        "status": "healthy",
        "version": settings.APP_VERSION
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG
    )
