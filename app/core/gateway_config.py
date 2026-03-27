"""
AI 模型网关配置
支持多模型负载均衡、失败重试、超时控制
"""
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
from enum import Enum


class ModelProvider(str, Enum):
    """模型提供商"""
    QWEN = "qwen"           # 阿里通义千问
    DEEPSEEK = "deepseek"   # 深度求索
    IFLYTEK = "iflytek"     # 科大讯飞
    GLM = "glm"             # 智谱 AI


class ModelConfig(BaseModel):
    """单个模型配置"""
    provider: ModelProvider
    model_name: str
    api_key_env: str
    weight: int = Field(default=1, ge=0)  # 负载均衡权重
    timeout: int = Field(default=30, ge=5)  # 超时时间 (秒)
    max_retries: int = Field(default=3, ge=0)  # 最大重试次数
    enabled: bool = True  # 是否启用


class GatewayConfig(BaseModel):
    """网关配置"""
    # 负载均衡策略
    load_balance_strategy: str = "weighted_round_robin"  # weighted_round_robin / random / least_connections
    
    # 重试配置
    retry_base_delay: float = 1.0  # 基础延迟 (秒)
    retry_max_delay: float = 30.0  # 最大延迟 (秒)
    retry_exponential_base: float = 2.0  # 指数退避基数
    
    # 超时配置
    default_timeout: int = 30  # 默认超时 (秒)
    connect_timeout: int = 10  # 连接超时 (秒)
    
    # 熔断配置
    circuit_breaker_enabled: bool = True
    circuit_breaker_threshold: int = 5  # 失败次数阈值
    circuit_breaker_timeout: int = 60  # 熔断超时 (秒)
    
    # 模型配置列表
    models: Dict[str, ModelConfig] = {}
    
    class Config:
        arbitrary_types_allowed = True


# 默认模型配置
DEFAULT_MODEL_CONFIGS = {
    "ocr": [
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-vl-max",
            api_key_env="DASHSCOPE_API_KEY",
            weight=3,
            timeout=60,
            max_retries=3
        ),
        ModelConfig(
            provider=ModelProvider.DEEPSEEK,
            model_name="deepseek-vl",
            api_key_env="DEEPSEEK_API_KEY",
            weight=2,
            timeout=60,
            max_retries=3
        ),
    ],
    "stt": [
        ModelConfig(
            provider=ModelProvider.IFLYTEK,
            model_name="iflytek-stt",
            api_key_env="IFLYTEK_API_KEY",
            weight=2,
            timeout=120,
            max_retries=3
        ),
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-audio",
            api_key_env="DASHSCOPE_API_KEY",
            weight=1,
            timeout=120,
            max_retries=3
        ),
    ],
    "nlp": [
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-turbo",
            api_key_env="DASHSCOPE_API_KEY",
            weight=2,
            timeout=30,
            max_retries=3
        ),
        ModelConfig(
            provider=ModelProvider.DEEPSEEK,
            model_name="deepseek-chat",
            api_key_env="DEEPSEEK_API_KEY",
            weight=2,
            timeout=30,
            max_retries=3
        ),
        ModelConfig(
            provider=ModelProvider.GLM,
            model_name="glm-4",
            api_key_env="GLM_API_KEY",
            weight=1,
            timeout=30,
            max_retries=3
        ),
    ],
}

# 网关单例配置
gateway_config = GatewayConfig(
    load_balance_strategy="weighted_round_robin",
    retry_base_delay=1.0,
    retry_max_delay=30.0,
    retry_exponential_base=2.0,
    default_timeout=30,
    connect_timeout=10,
    circuit_breaker_enabled=True,
    circuit_breaker_threshold=5,
    circuit_breaker_timeout=60,
    models=DEFAULT_MODEL_CONFIGS,
)
