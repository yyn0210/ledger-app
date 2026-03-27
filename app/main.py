"""
AI 服务 - FastAPI 入口
Ledger App AI Service
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger
import redis.asyncio as redis

from app.core.config import settings
from app.api import ocr, stt, nlp, prompts

# 创建 FastAPI 应用
app = FastAPI(
    title="Ledger App AI Service",
    description="AI 服务接口 - OCR/STT/NLP/Prompt 管理",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Redis 连接池
redis_pool: redis.Redis


@app.on_event("startup")
async def startup_event():
    """应用启动时初始化"""
    global redis_pool
    redis_pool = redis.Redis(
        host=settings.REDIS_HOST,
        port=settings.REDIS_PORT,
        db=settings.REDIS_DB,
        password=settings.REDIS_PASSWORD,
        decode_responses=True
    )
    logger.info("AI Service 启动成功")
    logger.info(f"Redis 连接：{settings.REDIS_HOST}:{settings.REDIS_PORT}")


@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭时清理资源"""
    await redis_pool.close()
    logger.info("AI Service 已关闭")


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy", "version": "1.0.0"}


# 注册路由
app.include_router(ocr.router, prefix="/api/ocr", tags=["OCR 识别"])
app.include_router(stt.router, prefix="/api/stt", tags=["语音 STT"])
app.include_router(nlp.router, prefix="/api/nlp", tags=["语义理解"])
app.include_router(prompts.router, prefix="/api/prompts", tags=["Prompt 管理"])


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
