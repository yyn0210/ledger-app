"""
AI 模型网关 - 多模型负载均衡、失败重试、超时控制
"""
import asyncio
import time
import random
from typing import Dict, List, Optional, Any, Callable
from dataclasses import dataclass, field
from enum import Enum
from loguru import logger
import httpx

from app.core.gateway_config import (
    gateway_config, ModelConfig, ModelProvider, 
    DEFAULT_MODEL_CONFIGS
)


class CircuitState(Enum):
    """熔断器状态"""
    CLOSED = "closed"       # 正常状态
    OPEN = "open"           # 熔断状态
    HALF_OPEN = "half_open" # 半开状态 (试探)


@dataclass
class CircuitBreaker:
    """熔断器实现"""
    model_key: str
    failure_count: int = 0
    last_failure_time: float = 0
    state: CircuitState = CircuitState.CLOSED
    success_count: int = 0
    
    def record_failure(self):
        """记录失败"""
        self.failure_count += 1
        self.last_failure_time = time.time()
        
        if self.failure_count >= gateway_config.circuit_breaker_threshold:
            self.state = CircuitState.OPEN
            logger.warning(f"熔断器打开：{self.model_key} (失败 {self.failure_count} 次)")
    
    def record_success(self):
        """记录成功"""
        if self.state == CircuitState.HALF_OPEN:
            self.success_count += 1
            if self.success_count >= 3:  # 连续 3 次成功则关闭熔断器
                self.reset()
        else:
            self.failure_count = max(0, self.failure_count - 1)
    
    def reset(self):
        """重置熔断器"""
        self.failure_count = 0
        self.success_count = 0
        self.state = CircuitState.CLOSED
        logger.info(f"熔断器关闭：{self.model_key}")
    
    def can_execute(self) -> bool:
        """检查是否可以执行请求"""
        if not gateway_config.circuit_breaker_enabled:
            return True
            
        if self.state == CircuitState.CLOSED:
            return True
        elif self.state == CircuitState.OPEN:
            # 检查是否超过熔断超时
            if time.time() - self.last_failure_time > gateway_config.circuit_breaker_timeout:
                self.state = CircuitState.HALF_OPEN
                logger.info(f"熔断器半开：{self.model_key} (试探请求)")
                return True
            return False
        else:  # HALF_OPEN
            return True


@dataclass
class ModelStats:
    """模型统计信息"""
    model_key: str
    total_requests: int = 0
    successful_requests: int = 0
    failed_requests: int = 0
    total_latency_ms: float = 0
    current_weight: int = 0
    
    @property
    def success_rate(self) -> float:
        if self.total_requests == 0:
            return 1.0
        return self.successful_requests / self.total_requests
    
    @property
    def avg_latency_ms(self) -> float:
        if self.successful_requests == 0:
            return 0
        return self.total_latency_ms / self.successful_requests


@dataclass
class LoadBalancer:
    """加权轮询负载均衡器"""
    models: List[ModelConfig]
    stats: Dict[str, ModelStats] = field(default_factory=dict)
    circuit_breakers: Dict[str, CircuitBreaker] = field(default_factory=dict)
    current_index: int = 0
    current_weight: int = 0
    
    def __post_init__(self):
        # 初始化统计和熔断器
        for model in self.models:
            key = f"{model.provider.value}:{model.model_name}"
            self.stats[key] = ModelStats(model_key=key)
            self.circuit_breakers[key] = CircuitBreaker(model_key=key)
    
    def get_available_models(self) -> List[ModelConfig]:
        """获取可用的模型列表 (排除禁用和熔断的)"""
        available = []
        for model in self.models:
            if not model.enabled:
                continue
            key = f"{model.provider.value}:{model.model_name}"
            if self.circuit_breakers[key].can_execute():
                available.append(model)
        return available
    
    def select_model(self) -> Optional[ModelConfig]:
        """选择一个模型 (加权轮询)"""
        available = self.get_available_models()
        if not available:
            logger.error("没有可用的模型")
            return None
        
        if len(available) == 1:
            return available[0]
        
        # 加权轮询算法
        total_weight = sum(m.weight for m in available)
        if total_weight == 0:
            return random.choice(available)
        
        # 随机权重选择
        rand = random.uniform(0, total_weight)
        current = 0
        for model in available:
            current += model.weight
            if rand <= current:
                return model
        
        return available[-1]
    
    def record_success(self, model: ModelConfig, latency_ms: float):
        """记录成功请求"""
        key = f"{model.provider.value}:{model.model_name}"
        if key in self.stats:
            self.stats[key].total_requests += 1
            self.stats[key].successful_requests += 1
            self.stats[key].total_latency_ms += latency_ms
        if key in self.circuit_breakers:
            self.circuit_breakers[key].record_success()
    
    def record_failure(self, model: ModelConfig):
        """记录失败请求"""
        key = f"{model.provider.value}:{model.model_name}"
        if key in self.stats:
            self.stats[key].total_requests += 1
            self.stats[key].failed_requests += 1
        if key in self.circuit_breakers:
            self.circuit_breakers[key].record_failure()
    
    def get_stats(self) -> Dict[str, Dict]:
        """获取统计信息"""
        return {
            key: {
                "total_requests": stats.total_requests,
                "successful_requests": stats.successful_requests,
                "failed_requests": stats.failed_requests,
                "success_rate": f"{stats.success_rate:.2%}",
                "avg_latency_ms": f"{stats.avg_latency_ms:.2f}",
            }
            for key, stats in self.stats.items()
        }


class ModelGateway:
    """AI 模型网关 - 统一管理多模型调用"""
    
    def __init__(self, task_type: str = "nlp"):
        """
        初始化网关
        
        Args:
            task_type: 任务类型 (ocr/stt/nlp)
        """
        self.task_type = task_type
        self.models = DEFAULT_MODEL_CONFIGS.get(task_type, [])
        self.load_balancer = LoadBalancer(models=self.models)
        logger.info(f"ModelGateway 初始化完成 - 任务类型：{task_type}, 模型数：{len(self.models)}")
    
    async def execute(
        self,
        execute_fn: Callable,
        *args,
        **kwargs
    ) -> Any:
        """
        执行模型调用 (带重试和超时控制)
        
        Args:
            execute_fn: 执行函数 (接收 model_config 作为第一个参数)
            *args: 位置参数
            **kwargs: 关键字参数
            
        Returns:
            模型调用结果
            
        Raises:
            Exception: 所有重试失败后抛出异常
        """
        last_exception = None
        max_retries = max((m.max_retries for m in self.models), default=3)
        
        for attempt in range(max_retries + 1):
            # 选择模型
            model = self.load_balancer.select_model()
            if not model:
                raise Exception("没有可用的模型")
            
            model_key = f"{model.provider.value}:{model.model_name}"
            start_time = time.time()
            
            try:
                logger.info(f"尝试调用模型：{model_key} (尝试 {attempt + 1}/{max_retries + 1})")
                
                # 执行调用 (带超时)
                timeout = model.timeout or gateway_config.default_timeout
                result = await asyncio.wait_for(
                    execute_fn(model, *args, **kwargs),
                    timeout=timeout
                )
                
                # 记录成功
                latency_ms = (time.time() - start_time) * 1000
                self.load_balancer.record_success(model, latency_ms)
                logger.info(f"模型调用成功：{model_key} (耗时 {latency_ms:.2f}ms)")
                
                return result
                
            except asyncio.TimeoutError as e:
                latency_ms = (time.time() - start_time) * 1000
                logger.error(f"模型调用超时：{model_key} (超时 {timeout}s, 耗时 {latency_ms:.2f}ms)")
                self.load_balancer.record_failure(model)
                last_exception = e
                
            except Exception as e:
                latency_ms = (time.time() - start_time) * 1000
                logger.error(f"模型调用失败：{model_key} (错误：{str(e)})")
                self.load_balancer.record_failure(model)
                last_exception = e
            
            # 计算重试延迟 (指数退避)
            if attempt < max_retries:
                delay = min(
                    gateway_config.retry_base_delay * (gateway_config.retry_exponential_base ** attempt),
                    gateway_config.retry_max_delay
                )
                logger.info(f"{delay:.2f}s 后重试...")
                await asyncio.sleep(delay)
        
        # 所有重试失败
        error_msg = f"所有重试失败 (共 {max_retries + 1} 次尝试)"
        logger.error(error_msg)
        if last_exception:
            raise last_exception
        raise Exception(error_msg)
    
    def get_stats(self) -> Dict:
        """获取网关统计信息"""
        return {
            "task_type": self.task_type,
            "total_models": len(self.models),
            "available_models": len(self.load_balancer.get_available_models()),
            "models": self.load_balancer.get_stats(),
        }


# 预创建的网关节例
_ocr_gateway: Optional[ModelGateway] = None
_stt_gateway: Optional[ModelGateway] = None
_nlp_gateway: Optional[ModelGateway] = None


def get_gateway(task_type: str) -> ModelGateway:
    """获取或创建网关节例"""
    global _ocr_gateway, _stt_gateway, _nlp_gateway
    
    if task_type == "ocr":
        if _ocr_gateway is None:
            _ocr_gateway = ModelGateway("ocr")
        return _ocr_gateway
    elif task_type == "stt":
        if _stt_gateway is None:
            _stt_gateway = ModelGateway("stt")
        return _stt_gateway
    elif task_type == "nlp":
        if _nlp_gateway is None:
            _nlp_gateway = ModelGateway("nlp")
        return _nlp_gateway
    else:
        return ModelGateway(task_type)


# 便捷函数
async def call_with_gateway(
    task_type: str,
    execute_fn: Callable,
    *args,
    **kwargs
) -> Any:
    """
    使用网关调用模型
    
    Args:
        task_type: 任务类型 (ocr/stt/nlp)
        execute_fn: 执行函数
        *args: 位置参数
        **kwargs: 关键字参数
        
    Returns:
        模型调用结果
    """
    gateway = get_gateway(task_type)
    return await gateway.execute(execute_fn, *args, **kwargs)
