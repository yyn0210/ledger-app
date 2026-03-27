"""
AI 模型网关单元测试
测试负载均衡、失败重试、超时控制、熔断器
"""
import pytest
import asyncio
import time
from unittest.mock import AsyncMock, patch, MagicMock
from typing import List

from app.core.gateway_config import ModelConfig, ModelProvider, GatewayConfig
from app.services.gateway import (
    ModelGateway, LoadBalancer, CircuitBreaker, ModelStats,
    CircuitState, get_gateway, call_with_gateway
)


class TestCircuitBreaker:
    """熔断器测试"""
    
    def test_initial_state(self):
        """测试初始状态"""
        cb = CircuitBreaker(model_key="test:model")
        assert cb.state == CircuitState.CLOSED
        assert cb.failure_count == 0
        assert cb.can_execute() is True
    
    def test_opens_after_threshold(self):
        """测试达到阈值后打开"""
        cb = CircuitBreaker(model_key="test:model")
        
        # 模拟 5 次失败
        for i in range(5):
            cb.record_failure()
        
        assert cb.state == CircuitState.OPEN
        assert cb.can_execute() is False
    
    def test_half_open_after_timeout(self):
        """测试超时后半开"""
        cb = CircuitBreaker(model_key="test:model")
        
        # 触发熔断
        for i in range(5):
            cb.record_failure()
        
        assert cb.state == CircuitState.OPEN
        
        # 等待超时 (模拟)
        cb.last_failure_time -= 70  # 超过 60s 超时
        
        assert cb.can_execute() is True
        assert cb.state == CircuitState.HALF_OPEN
    
    def test_closes_after_success(self):
        """测试成功后关闭"""
        cb = CircuitBreaker(model_key="test:model")
        cb.state = CircuitState.HALF_OPEN
        
        # 模拟 3 次成功
        for i in range(3):
            cb.record_success()
        
        assert cb.state == CircuitState.CLOSED
        assert cb.failure_count == 0


class TestLoadBalancer:
    """负载均衡器测试"""
    
    @pytest.fixture
    def sample_models(self) -> List[ModelConfig]:
        return [
            ModelConfig(
                provider=ModelProvider.QWEN,
                model_name="qwen-turbo",
                api_key_env="TEST_KEY",
                weight=3,
                timeout=30
            ),
            ModelConfig(
                provider=ModelProvider.DEEPSEEK,
                model_name="deepseek-chat",
                api_key_env="TEST_KEY",
                weight=2,
                timeout=30
            ),
            ModelConfig(
                provider=ModelProvider.GLM,
                model_name="glm-4",
                api_key_env="TEST_KEY",
                weight=1,
                timeout=30
            ),
        ]
    
    def test_initialization(self, sample_models):
        """测试初始化"""
        lb = LoadBalancer(models=sample_models)
        assert len(lb.models) == 3
        assert len(lb.stats) == 3
        assert len(lb.circuit_breakers) == 3
    
    def test_get_available_models(self, sample_models):
        """测试获取可用模型"""
        lb = LoadBalancer(models=sample_models)
        available = lb.get_available_models()
        assert len(available) == 3
        
        # 禁用一个模型
        sample_models[0].enabled = False
        lb = LoadBalancer(models=sample_models)
        available = lb.get_available_models()
        assert len(available) == 2
    
    def test_select_model_weighted(self, sample_models):
        """测试加权选择"""
        lb = LoadBalancer(models=sample_models)
        
        # 多次选择，权重高的应该被选中更多
        selections = {}
        for _ in range(100):
            model = lb.select_model()
            key = model.model_name
            selections[key] = selections.get(key, 0) + 1
        
        # qwen-turbo (weight=3) 应该被选中最多
        assert selections.get("qwen-turbo", 0) > selections.get("glm-4", 0)
    
    def test_record_success(self, sample_models):
        """测试记录成功"""
        lb = LoadBalancer(models=sample_models)
        model = sample_models[0]
        
        lb.record_success(model, 100.0)  # 100ms latency
        
        key = "qwen:qwen-turbo"
        assert lb.stats[key].total_requests == 1
        assert lb.stats[key].successful_requests == 1
        assert lb.stats[key].avg_latency_ms == 100.0
    
    def test_record_failure(self, sample_models):
        """测试记录失败"""
        lb = LoadBalancer(models=sample_models)
        model = sample_models[0]
        
        lb.record_failure(model)
        
        key = "qwen:qwen-turbo"
        assert lb.stats[key].failed_requests == 1
        assert lb.stats[key].total_requests == 1
    
    def test_get_stats(self, sample_models):
        """测试获取统计"""
        lb = LoadBalancer(models=sample_models)
        model = sample_models[0]
        
        lb.record_success(model, 100.0)
        lb.record_failure(model)
        
        stats = lb.get_stats()
        assert "qwen:qwen-turbo" in stats
        assert stats["qwen:qwen-turbo"]["total_requests"] == 2
        assert stats["qwen:qwen-turbo"]["successful_requests"] == 1


class TestModelGateway:
    """模型网关测试"""
    
    @pytest.fixture
    def gateway(self):
        return ModelGateway(task_type="nlp")
    
    @pytest.mark.asyncio
    async def test_successful_call(self, gateway):
        """测试成功调用"""
        async def mock_execute(model, *args, **kwargs):
            return {"result": "success"}
        
        result = await gateway.execute(mock_execute)
        assert result == {"result": "success"}
    
    @pytest.mark.asyncio
    async def test_retry_on_failure(self, gateway):
        """测试失败重试"""
        call_count = 0
        
        async def mock_execute(model, *args, **kwargs):
            nonlocal call_count
            call_count += 1
            if call_count < 3:
                raise Exception("Simulated failure")
            return {"result": "success"}
        
        result = await gateway.execute(mock_execute)
        assert result == {"result": "success"}
        assert call_count >= 3  # 至少重试了 2 次
    
    @pytest.mark.asyncio
    async def test_timeout_control(self, gateway):
        """测试超时控制"""
        async def mock_execute(model, *args, **kwargs):
            await asyncio.sleep(10)  # 模拟长时间运行
            return {"result": "success"}
        
        with pytest.raises(asyncio.TimeoutError):
            await gateway.execute(mock_execute, timeout=0.1)
    
    @pytest.mark.asyncio
    async def test_all_retries_fail(self, gateway):
        """测试所有重试失败"""
        async def mock_execute(model, *args, **kwargs):
            raise Exception("Always fails")
        
        with pytest.raises(Exception) as exc_info:
            await gateway.execute(mock_execute)
        
        assert "所有重试失败" in str(exc_info.value)
    
    @pytest.mark.asyncio
    async def test_get_stats(self, gateway):
        """测试获取统计"""
        stats = gateway.get_stats()
        assert "task_type" in stats
        assert "total_models" in stats
        assert "models" in stats


class TestGatewayIntegration:
    """网关集成测试"""
    
    @pytest.mark.asyncio
    async def test_call_with_gateway(self):
        """测试便捷函数"""
        async def mock_execute(model, text):
            return {"text": text, "model": model.model_name}
        
        result = await call_with_gateway("nlp", mock_execute, "Hello")
        assert "text" in result
        assert result["text"] == "Hello"
    
    @pytest.mark.asyncio
    async def test_multiple_task_types(self):
        """测试多任务类型网关"""
        ocr_gateway = get_gateway("ocr")
        stt_gateway = get_gateway("stt")
        nlp_gateway = get_gateway("nlp")
        
        assert ocr_gateway.task_type == "ocr"
        assert stt_gateway.task_type == "stt"
        assert nlp_gateway.task_type == "nlp"
        
        # 单例测试 - 应该返回同一个实例
        assert get_gateway("ocr") is ocr_gateway


class TestModelStats:
    """模型统计测试"""
    
    def test_success_rate(self):
        """测试成功率计算"""
        stats = ModelStats(model_key="test:model")
        stats.total_requests = 10
        stats.successful_requests = 8
        stats.failed_requests = 2
        
        assert stats.success_rate == 0.8
    
    def test_success_rate_no_requests(self):
        """测试无请求时的成功率"""
        stats = ModelStats(model_key="test:model")
        assert stats.success_rate == 1.0
    
    def test_avg_latency(self):
        """测试平均延迟计算"""
        stats = ModelStats(model_key="test:model")
        stats.successful_requests = 3
        stats.total_latency_ms = 300.0
        
        assert stats.avg_latency_ms == 100.0
    
    def test_avg_latency_no_success(self):
        """测试无成功时的平均延迟"""
        stats = ModelStats(model_key="test:model")
        assert stats.avg_latency_ms == 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
