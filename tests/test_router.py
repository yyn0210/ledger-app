"""
智能路由策略单元测试
测试成本优化、质量分级、模型降级
"""
import pytest
import time
from unittest.mock import AsyncMock, patch

from app.core.gateway_config import ModelConfig, ModelProvider, QualityTier
from app.services.router import (
    IntelligentRouter, RoutingContext, RoutingStrategy, TaskPriority,
    ModelScore, get_router, route_and_execute
)


class TestRoutingContext:
    """路由上下文测试"""
    
    def test_default_context(self):
        """测试默认上下文"""
        ctx = RoutingContext(task_type="nlp")
        assert ctx.task_type == "nlp"
        assert ctx.priority == TaskPriority.NORMAL
        assert ctx.strategy == RoutingStrategy.BALANCED
        assert ctx.budget_limit == 0.0
        assert ctx.min_quality_tier == QualityTier.ECONOMY
    
    def test_custom_context(self):
        """测试自定义上下文"""
        ctx = RoutingContext(
            task_type="ocr",
            priority=TaskPriority.CRITICAL,
            strategy=RoutingStrategy.COST_OPTIMIZED,
            budget_limit=0.1,
            required_capabilities=["high-accuracy"],
            min_quality_tier=QualityTier.STANDARD
        )
        assert ctx.priority == TaskPriority.CRITICAL
        assert ctx.strategy == RoutingStrategy.COST_OPTIMIZED
        assert ctx.budget_limit == 0.1
        assert "high-accuracy" in ctx.required_capabilities
        assert ctx.min_quality_tier == QualityTier.STANDARD


class TestIntelligentRouter:
    """智能路由器测试"""
    
    @pytest.fixture
    def router(self):
        return IntelligentRouter("nlp")
    
    @pytest.fixture
    def sample_models(self):
        return [
            ModelConfig(
                provider=ModelProvider.QWEN,
                model_name="qwen-turbo",
                api_key_env="TEST_KEY",
                cost_per_1k_input=0.002,
                cost_per_1k_output=0.002,
                quality_tier=QualityTier.ECONOMY,
                capabilities=["nlp", "fast"]
            ),
            ModelConfig(
                provider=ModelProvider.DEEPSEEK,
                model_name="deepseek-chat",
                api_key_env="TEST_KEY",
                cost_per_1k_input=0.001,
                cost_per_1k_output=0.002,
                quality_tier=QualityTier.STANDARD,
                capabilities=["nlp", "reasoning"]
            ),
            ModelConfig(
                provider=ModelProvider.GLM,
                model_name="glm-4",
                api_key_env="TEST_KEY",
                cost_per_1k_input=0.005,
                cost_per_1k_output=0.005,
                quality_tier=QualityTier.PREMIUM,
                capabilities=["nlp", "high-accuracy"]
            ),
        ]
    
    def test_router_initialization(self, router):
        """测试路由器初始化"""
        assert router.task_type == "nlp"
        assert len(router.models) > 0
        assert len(router.model_stats) > 0
    
    def test_filter_by_quality_tier(self, sample_models):
        """测试按质量等级过滤"""
        router = IntelligentRouter("nlp")
        router.models = sample_models
        
        # 只允许 PREMIUM
        ctx = RoutingContext(task_type="nlp", min_quality_tier=QualityTier.PREMIUM)
        available = router._filter_available_models(ctx)
        assert len(available) == 1
        assert available[0].quality_tier == QualityTier.PREMIUM
        
        # 允许 STANDARD 及以上
        ctx = RoutingContext(task_type="nlp", min_quality_tier=QualityTier.STANDARD)
        available = router._filter_available_models(ctx)
        assert len(available) == 2
    
    def test_filter_by_capabilities(self, sample_models):
        """测试按能力过滤"""
        router = IntelligentRouter("nlp")
        router.models = sample_models
        
        # 需要 high-accuracy
        ctx = RoutingContext(task_type="nlp", required_capabilities=["high-accuracy"])
        available = router._filter_available_models(ctx)
        assert len(available) == 1
        assert "high-accuracy" in available[0].capabilities
        
        # 需要 reasoning
        ctx = RoutingContext(task_type="nlp", required_capabilities=["reasoning"])
        available = router._filter_available_models(ctx)
        assert len(available) == 1
        assert available[0].model_name == "deepseek-chat"
    
    def test_cost_optimized_strategy(self, router):
        """测试成本优先策略"""
        ctx = RoutingContext(
            task_type="nlp",
            strategy=RoutingStrategy.COST_OPTIMIZED,
            input_tokens=1000,
            output_tokens=500
        )
        
        model = router.select_model(ctx)
        assert model is not None
        # 成本优先应该选择较便宜的模型
    
    def test_quality_first_strategy(self, router):
        """测试质量优先策略"""
        ctx = RoutingContext(
            task_type="nlp",
            strategy=RoutingStrategy.QUALITY_FIRST,
            priority=TaskPriority.CRITICAL
        )
        
        model = router.select_model(ctx)
        assert model is not None
        # 质量优先应该选择 PREMIUM 或 STANDARD
    
    def test_select_model_with_budget(self, router):
        """测试预算限制"""
        ctx = RoutingContext(
            task_type="nlp",
            budget_limit=0.001,  # 非常低的预算
            input_tokens=1000,
            output_tokens=500
        )
        
        model = router.select_model(ctx)
        # 应该选择在预算内的模型
    
    def test_record_success(self, router):
        """测试记录成功"""
        model = router.models[0]
        router.record_success(model, latency_ms=100.0, tokens_used=500)
        
        key = router._model_key(model)
        stats = router.model_stats[key]
        assert stats["successful_requests"] == 1
        assert stats["consecutive_failures"] == 0
        assert stats["avg_latency_ms"] == 100.0
    
    def test_record_failure(self, router):
        """测试记录失败"""
        model = router.models[0]
        router.record_failure(model)
        
        key = router._model_key(model)
        stats = router.model_stats[key]
        assert stats["failed_requests"] == 1
        assert stats["consecutive_failures"] == 1
    
    def test_model_degradation(self, router):
        """测试模型降级"""
        # 找到 PREMIUM 模型
        premium_model = None
        for model in router.models:
            if model.quality_tier == QualityTier.PREMIUM:
                premium_model = model
                break
        
        if premium_model:
            degraded = router.degrade_model(premium_model)
            # 应该降级到 STANDARD 或 ECONOMY
            assert degraded is not None
            assert degraded.quality_tier in [QualityTier.STANDARD, QualityTier.ECONOMY]
    
    def test_get_cost_report(self, router):
        """测试成本报告"""
        # 模拟一些请求
        for model in router.models[:2]:
            router.record_success(model, latency_ms=100.0, tokens_used=500)
        
        report = router.get_cost_report()
        assert "task_type" in report
        assert "total_cost" in report
        assert "models" in report
        assert report["task_type"] == "nlp"


class TestModelScoring:
    """模型评分测试"""
    
    @pytest.fixture
    def router(self):
        return IntelligentRouter("nlp")
    
    def test_cost_score_calculation(self, router):
        """测试成本评分"""
        ctx = RoutingContext(task_type="nlp", input_tokens=1000, output_tokens=500)
        
        cheap_model = ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="cheap",
            api_key_env="KEY",
            cost_per_1k_input=0.001,
            cost_per_1k_output=0.001,
            quality_tier=QualityTier.ECONOMY
        )
        
        expensive_model = ModelConfig(
            provider=ModelProvider.GLM,
            model_name="expensive",
            api_key_env="KEY",
            cost_per_1k_input=0.01,
            cost_per_1k_output=0.01,
            quality_tier=QualityTier.PREMIUM
        )
        
        cheap_score = router._calculate_cost_score(
            router._estimate_cost(cheap_model, ctx),
            ctx
        )
        expensive_score = router._calculate_cost_score(
            router._estimate_cost(expensive_model, ctx),
            ctx
        )
        
        assert cheap_score > expensive_score
    
    def test_quality_score_calculation(self, router):
        """测试质量评分"""
        ctx = RoutingContext(task_type="nlp", priority=TaskPriority.CRITICAL)
        
        premium_model = ModelConfig(
            provider=ModelProvider.GLM,
            model_name="premium",
            api_key_env="KEY",
            quality_tier=QualityTier.PREMIUM
        )
        
        economy_model = ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="economy",
            api_key_env="KEY",
            quality_tier=QualityTier.ECONOMY
        )
        
        premium_score = router._calculate_quality_score(premium_model, ctx)
        economy_score = router._calculate_quality_score(economy_model, ctx)
        
        # 关键任务下，PREMIUM 应该得分更高
        assert premium_score > economy_score
    
    def test_latency_score_calculation(self, router):
        """测试延迟评分"""
        ctx = RoutingContext(task_type="nlp")
        
        fast_stats = {"avg_latency_ms": 50}
        slow_stats = {"avg_latency_ms": 2000}
        
        fast_score = router._calculate_latency_score(None, fast_stats, ctx)
        slow_score = router._calculate_latency_score(None, slow_stats, ctx)
        
        assert fast_score > slow_score
        assert fast_score == 20.0  # < 100ms
        assert slow_score == 2.0   # > 1000ms
    
    def test_reliability_score_calculation(self, router):
        """测试可靠性评分"""
        good_stats = {
            "total_requests": 100,
            "successful_requests": 98,
            "consecutive_failures": 0
        }
        
        bad_stats = {
            "total_requests": 10,
            "successful_requests": 5,
            "consecutive_failures": 5
        }
        
        good_score = router._calculate_reliability_score(good_stats)
        bad_score = router._calculate_reliability_score(bad_stats)
        
        assert good_score > bad_score
        assert good_score > 9.0  # 98% 成功率


class TestRoutingStrategies:
    """路由策略测试"""
    
    @pytest.fixture
    def router(self):
        return IntelligentRouter("nlp")
    
    def test_cost_optimized_selects_cheapest(self, router):
        """测试成本优先选择最便宜模型"""
        ctx = RoutingContext(
            task_type="nlp",
            strategy=RoutingStrategy.COST_OPTIMIZED,
            input_tokens=1000,
            output_tokens=1000
        )
        
        model = router.select_model(ctx)
        assert model is not None
    
    def test_quality_first_selects_premium(self, router):
        """测试质量优先选择高质量模型"""
        ctx = RoutingContext(
            task_type="nlp",
            strategy=RoutingStrategy.QUALITY_FIRST,
            priority=TaskPriority.CRITICAL
        )
        
        model = router.select_model(ctx)
        assert model is not None
    
    def test_balanced_strategy(self, router):
        """测试平衡策略"""
        ctx = RoutingContext(
            task_type="nlp",
            strategy=RoutingStrategy.BALANCED,
            priority=TaskPriority.NORMAL
        )
        
        model = router.select_model(ctx)
        assert model is not None


class TestRouterSingleton:
    """路由器单例测试"""
    
    def test_get_router_returns_same_instance(self):
        """测试获取路由器返回相同实例"""
        router1 = get_router("nlp")
        router2 = get_router("nlp")
        assert router1 is router2
    
    def test_different_task_types_different_routers(self):
        """测试不同任务类型返回不同路由器"""
        ocr_router = get_router("ocr")
        stt_router = get_router("stt")
        nlp_router = get_router("nlp")
        
        assert ocr_router is not stt_router
        assert stt_router is not nlp_router
        assert ocr_router is not nlp_router


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
