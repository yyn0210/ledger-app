"""
AI 服务核心配置
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """应用配置"""
    
    # 应用配置
    APP_NAME: str = "Ledger AI Service"
    APP_VERSION: str = "0.0.1"
    DEBUG: bool = True
    
    # 服务器配置
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # 阿里百炼 DashScope 配置
    DASHSCOPE_API_KEY: Optional[str] = None
    DASHSCOPE_QWEN_VL_MODEL: str = "qwen-vl-plus"  # OCR 模型
    DASHSCOPE_QWEN_MODEL: str = "qwen-turbo"  # 文本模型
    
    # DeepSeek 配置
    DEEPSEEK_API_KEY: Optional[str] = None
    DEEPSEEK_API_URL: str = "https://api.deepseek.com/v1"
    DEEPSEEK_MODEL: str = "deepseek-chat"
    
    # 讯飞星火配置
    IFLYTEK_APP_ID: Optional[str] = None
    IFLYTEK_API_KEY: Optional[str] = None
    IFLYTEK_API_SECRET: Optional[str] = None
    IFLYTEK_STT_MODEL: str = "iat"  # 语音听写
    
    # Redis 配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: Optional[str] = None
    REDIS_TTL: int = 3600  # 缓存过期时间（秒）
    
    # MinIO 配置
    MINIO_ENDPOINT: str = "localhost:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin"
    MINIO_BUCKET: str = "ledger-app"
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FILE: str = "logs/ai-service.log"
    
    class Config:
        env_file = ".env"
        case_sensitive = True


# 全局配置实例
settings = Settings()
