"""
应用配置
"""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # 应用配置
    APP_ENV: str = "development"
    DEBUG: bool = True
    LOG_LEVEL: str = "INFO"
    
    # AI 服务配置
    DASHSCOPE_API_KEY: str = ""
    IFLYTEK_APP_ID: str = ""
    IFLYTEK_API_KEY: str = ""
    IFLYTEK_API_SECRET: str = ""
    DEEPSEEK_API_KEY: str = ""
    
    # Redis 配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: str = ""
    
    # MinIO 配置
    MINIO_ENDPOINT: str = "localhost:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin"
    MINIO_BUCKET: str = "ledger-app"
    MINIO_SECURE: bool = False
    
    # CORS 配置
    ALLOWED_ORIGINS: List[str] = ["*"]
    
    # 文件限制
    MAX_IMAGE_SIZE: int = 10 * 1024 * 1024  # 10MB
    MAX_AUDIO_SIZE: int = 10 * 1024 * 1024  # 10MB
    ALLOWED_IMAGE_TYPES: List[str] = ["image/jpeg", "image/png", "image/webp"]
    ALLOWED_AUDIO_TYPES: List[str] = ["audio/mpeg", "audio/wav", "audio/mp4", "audio/amr"]
    
    # 缓存过期时间（秒）
    OCR_CACHE_EXPIRE: int = 86400  # 24 小时
    STT_CACHE_EXPIRE: int = 86400
    NLP_CACHE_EXPIRE: int = 3600  # 1 小时
    
    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
