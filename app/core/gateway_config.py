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
    LOCAL = "local"         # 本地模型


class QualityTier(str, Enum):
    """质量等级"""
    PREMIUM = "premium"       # 最高质量 (高成本)
    STANDARD = "standard"     # 标准质量 (中等成本)
    ECONOMY = "economy"       # 经济质量 (低成本)


class ModelConfig(BaseModel):
    """单个模型配置"""
    provider: ModelProvider
    model_name: str
    api_key_env: str
    weight: int = Field(default=1, ge=0)  # 负载均衡权重
    timeout: int = Field(default=30, ge=5)  # 超时时间 (秒)
    max_retries: int = Field(default=3, ge=0)  # 最大重试次数
    enabled: bool = True  # 是否启用
    
    # 成本配置 (每 1000 tokens 的价格，单位：元)
    cost_per_1k_input: float = Field(default=0.0, ge=0)  # 输入价格
    cost_per_1k_output: float = Field(default=0.0, ge=0)  # 输出价格
    
    # 质量等级
    quality_tier: QualityTier = QualityTier.STANDARD
    
    # 能力标签 (用于智能路由)
    capabilities: List[str] = []  # e.g., ["ocr", "multilingual", "fast"]
    
    # 速率限制
    rpm: int = Field(default=100, ge=1)  # 每分钟请求数限制
    tpm: int = Field(default=10000, ge=1)  # 每分钟 token 数限制


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
            max_retries=3,
            cost_per_1k_input=0.02,
            cost_per_1k_output=0.02,
            quality_tier=QualityTier.PREMIUM,
            capabilities=["ocr", "high-accuracy", "receipt"]
        ),
        ModelConfig(
            provider=ModelProvider.DEEPSEEK,
            model_name="deepseek-vl",
            api_key_env="DEEPSEEK_API_KEY",
            weight=2,
            timeout=60,
            max_retries=3,
            cost_per_1k_input=0.01,
            cost_per_1k_output=0.01,
            quality_tier=QualityTier.STANDARD,
            capabilities=["ocr", "multilingual"]
        ),
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-vl-plus",
            api_key_env="DASHSCOPE_API_KEY",
            weight=1,
            timeout=60,
            max_retries=3,
            cost_per_1k_input=0.005,
            cost_per_1k_output=0.005,
            quality_tier=QualityTier.ECONOMY,
            capabilities=["ocr", "fast"]
        ),
    ],
    "stt": [
        ModelConfig(
            provider=ModelProvider.IFLYTEK,
            model_name="iflytek-stt",
            api_key_env="IFLYTEK_API_KEY",
            weight=2,
            timeout=120,
            max_retries=3,
            cost_per_1k_input=0.008,
            quality_tier=QualityTier.PREMIUM,
            capabilities=["stt", "chinese", "high-accuracy"]
        ),
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-audio",
            api_key_env="DASHSCOPE_API_KEY",
            weight=1,
            timeout=120,
            max_retries=3,
            cost_per_1k_input=0.005,
            quality_tier=QualityTier.STANDARD,
            capabilities=["stt", "multilingual"]
        ),
    ],
    "nlp": [
        ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-turbo",
            api_key_env="DASHSCOPE_API_KEY",
            weight=2,
            timeout=30,
            max_retries=3,
            cost_per_1k_input=0.002,
            cost_per_1k_output=0.002,
            quality_tier=QualityTier.ECONOMY,
            capabilities=["nlp", "fast", "chat"]
        ),
        ModelConfig(
            provider=ModelProvider.DEEPSEEK,
            model_name="deepseek-chat",
            api_key_env="DEEPSEEK_API_KEY",
            weight=2,
            timeout=30,
            max_retries=3,
            cost_per_1k_input=0.001,
            cost_per_1k_output=0.002,
            quality_tier=QualityTier.STANDARD,
            capabilities=["nlp", "reasoning", "chat"]
        ),
        ModelConfig(
            provider=ModelProvider.GLM,
            model_name="glm-4",
            api_key_env="GLM_API_KEY",
            weight=1,
            timeout=30,
            max_retries=3,
            cost_per_1k_input=0.005,
            cost_per_1k_output=0.005,
            quality_tier=QualityTier.PREMIUM,
            capabilities=["nlp", "reasoning", "high-accuracy"]
        ),
        # Local models (fallback, ~0 cost)
        ModelConfig(
            provider=ModelProvider.LOCAL,
            model_name="qwen2.5-0.5b",
            api_key_env="",
            weight=1,
            timeout=60,
            max_retries=1,
            cost_per_1k_input=0.0,
            cost_per_1k_output=0.0,
            quality_tier=QualityTier.ECONOMY,
            capabilities=["nlp", "local", "offline", "cpu"]
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
