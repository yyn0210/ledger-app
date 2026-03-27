"""
成本监控与优化单元测试
测试 API 调用统计、成本分析、优化建议
"""
import pytest
import time
from unittest.mock import patch

from app.core.gateway_config import ModelConfig, ModelProvider, QualityTier
from app.services.cost_analytics import (
    CostAnalyticsService, CostRecord, ModelCostStats,
    OptimizationRecommendation, TimeRange,
    get_cost_analytics
)


class TestCostRecord:
    """成本记录测试"""
    
    def test_create_record(self):
        """测试创建成本记录"""
        record = CostRecord(
            timestamp=time.time(),
            task_type="nlp",
            model_name="qwen-turbo",
            provider="qwen",
            input_tokens=100,
            output_tokens=50,
            cost=0.0003,
            latency_ms=150.0
        )
        
        assert record.task_type == "nlp"
        assert record.input_tokens == 100
        assert record.cost == 0.0003
        assert record.from_cache is False
    
    def test_to_dict(self):
        """测试转换为字典"""
        record = CostRecord(
            timestamp=1000.0,
            task_type="ocr",
            model_name="qwen-vl-max",
            provider="qwen",
            input_tokens=500,
            output_tokens=200,
            cost=0.014,
            latency_ms=300.0,
            from_cache=True,
            cache_saved=0.014
        )
        
        data = record.to_dict()
        assert data["task_type"] == "ocr"
        assert data["from_cache"] is True
        assert data["cache_saved"] == 0.014


class TestModelCostStats:
    """模型成本统计测试"""
    
    def test_create_stats(self):
        """测试创建统计"""
        stats = ModelCostStats(
            model_name="qwen-turbo",
            provider="qwen",
            task_type="nlp"
        )
        
        assert stats.model_name == "qwen-turbo"
        assert stats.total_requests == 0
        assert stats.total_cost == 0.0
    
    def test_avg_cost_per_request(self):
        """测试平均成本计算"""
        stats = ModelCostStats(
            model_name="qwen-turbo",
            provider="qwen",
            task_type="nlp"
        )
        stats.total_requests = 10
        stats.total_cost = 0.05
        
        assert stats.avg_cost_per_request == 0.005
    
    def test_avg_cost_no_requests(self):
        """测试无请求时的平均成本"""
        stats = ModelCostStats(
            model_name="qwen-turbo",
            provider="qwen",
            task_type="nlp"
        )
        
        assert stats.avg_cost_per_request == 0.0
    
    def test_avg_latency(self):
        """测试平均延迟计算"""
        stats = ModelCostStats(
            model_name="qwen-turbo",
            provider="qwen",
            task_type="nlp"
        )
        stats.total_requests = 10
        stats.total_latency_ms = 1500.0
        
        assert stats.avg_latency_ms == 150.0
    
    def test_cache_hit_rate(self):
        """测试缓存命中率计算"""
        stats = ModelCostStats(
            model_name="qwen-turbo",
            provider="qwen",
            task_type="nlp"
        )
        stats.total_requests = 100
        stats.cache_hits = 60
        
        assert stats.cache_hit_rate == 0.6
    
    def test_to_dict(self):
        """测试转换为字典"""
        stats = ModelCostStats(
            model_name="deepseek-chat",
            provider="deepseek",
            task_type="nlp"
        )
        stats.total_requests = 50
        stats.total_cost = 0.1
        stats.cache_hits = 20
        
        data = stats.to_dict()
        assert data["model_name"] == "deepseek-chat"
        assert data["total_requests"] == 50
        assert data["total_cost"] == "0.1000"
        assert data["cache_hit_rate"] == "40.00%"


class TestCostAnalyticsService:
    """成本分析服务测试"""
    
    @pytest.fixture
    def analytics(self):
        """创建成本分析服务"""
        return CostAnalyticsService()
    
    @pytest.fixture
    def sample_model(self):
        """示例模型配置"""
        return ModelConfig(
            provider=ModelProvider.QWEN,
            model_name="qwen-turbo",
            api_key_env="TEST_KEY",
            cost_per_1k_input=0.002,
            cost_per_1k_output=0.002,
            quality_tier=QualityTier.ECONOMY
        )
    
    def test_record_request(self, analytics, sample_model):
        """测试记录请求"""
        analytics.record_request(
            task_type="nlp",
            model=sample_model,
            input_tokens=1000,
            output_tokens=500,
            latency_ms=100.0
        )
        
        assert len(analytics.records) == 1
        assert analytics.records[0].cost == 0.003  # (1000/1000)*0.002 + (500/1000)*0.002
    
    def test_record_request_from_cache(self, analytics, sample_model):
        """测试记录缓存请求"""
        analytics.record_request(
            task_type="nlp",
            model=sample_model,
            input_tokens=1000,
            output_tokens=500,
            latency_ms=10.0,
            from_cache=True
        )
        
        assert len(analytics.records) == 1
        assert analytics.records[0].from_cache is True
        assert analytics.records[0].cache_saved == 0.003
    
    def test_update_model_stats(self, analytics, sample_model):
        """测试更新模型统计"""
        analytics.record_request(
            task_type="nlp",
            model=sample_model,
            input_tokens=1000,
            output_tokens=500,
            latency_ms=100.0
        )
        
        key = "qwen:qwen-turbo"
        assert key in analytics.model_stats
        assert analytics.model_stats[key].total_requests == 1
        assert analytics.model_stats[key].total_cost == 0.003
    
    def test_update_daily_cost(self, analytics, sample_model):
        """测试更新每日成本"""
        analytics.record_request(
            task_type="nlp",
            model=sample_model,
            input_tokens=1000,
            output_tokens=500,
            latency_ms=100.0
        )
        
        today = time.strftime("%Y-%m-%d")
        assert today in analytics.daily_costs
    
    def test_get_model_stats(self, analytics, sample_model):
        """测试获取模型统计"""
        analytics.record_request(
            task_type="nlp",
            model=sample_model,
            input_tokens=1000,
            output_tokens=500,
            latency_ms=100.0
        )
        
        stats = analytics.get_model_stats("qwen-turbo")
        assert stats["model_name"] == "qwen-turbo"
        assert stats["total_requests"] == 1
    
    def test_get_cost_summary_daily(self, analytics, sample_model):
        """测试获取每日成本摘要"""
        # 记录多个请求
        for i in range(10):
            analytics.record_request(
                task_type="nlp",
                model=sample_model,
                input_tokens=1000,
                output_tokens=500,
                latency_ms=100.0,
                from_cache=(i % 3 == 0)  # 30% 缓存命中
            )
        
        summary = analytics.get_cost_summary(TimeRange.DAILY)
        
        assert summary["time_range"] == "daily"
        assert summary["total_requests"] == 10
        assert "total_cost" in summary
        assert "cache_hit_rate" in summary
    
    def test_get_daily_costs(self, analytics):
        """测试获取每日成本"""
        daily_costs = analytics.get_daily_costs(days=7)
        
        assert len(daily_costs) == 7
        assert "date" in daily_costs[0]
        assert "cost" in daily_costs[0]
    
    def test_set_budget_limit(self, analytics):
        """测试设置预算限制"""
        analytics.set_budget_limit(100.0)
        
        assert analytics.budget_limit == 100.0
    
    def test_get_budget_status(self, analytics):
        """测试获取预算状态"""
        analytics.set_budget_limit(100.0)
        
        status = analytics.get_budget_status()
        
        assert status["enabled"] is True
        assert status["limit"] == "100.00"
        assert "spent" in status
        assert "remaining" in status
    
    def test_budget_alert(self, analytics, sample_model):
        """测试预算告警"""
        analytics.set_budget_limit(0.001)  # 非常低的预算
        
        # 记录高成本请求
        for i in range(10):
            analytics.record_request(
                task_type="nlp",
                model=sample_model,
                input_tokens=1000,
                output_tokens=500,
                latency_ms=100.0
            )
        
        assert len(analytics.budget_alerts) > 0
    
    def test_generate_recommendations(self, analytics, sample_model):
        """测试生成优化建议"""
        # 记录一些请求以生成建议
        for i in range(100):
            analytics.record_request(
                task_type="nlp",
                model=sample_model,
                input_tokens=1000,
                output_tokens=500,
                latency_ms=100.0,
                from_cache=False
            )
        
        recommendations = analytics.generate_recommendations()
        
        assert len(recommendations) > 0
        assert all(isinstance(r, OptimizationRecommendation) for r in recommendations)
    
    def test_export_report(self, analytics, sample_model):
        """测试导出报告"""
        # 记录一些请求
        for i in range(10):
            analytics.record_request(
                task_type="nlp",
                model=sample_model,
                input_tokens=1000,
                output_tokens=500,
                latency_ms=100.0
            )
        
        report = analytics.export_report(TimeRange.DAILY)
        
        assert report["report_type"] == "cost_analysis"
        assert "generated_at" in report
        assert "summary" in report
        assert "recommendations" in report
        assert "budget_status" in report


class TestOptimizationRecommendation:
    """优化建议测试"""
    
    def test_create_recommendation(self):
        """测试创建优化建议"""
        rec = OptimizationRecommendation(
            priority=1,
            category="cost",
            title="优化模型使用",
            description="切换到更经济的模型",
            potential_savings=0.5,
            action="使用 Qwen-Turbo"
        )
        
        assert rec.priority == 1
        assert rec.category == "cost"
        assert rec.potential_savings == 0.5
    
    def test_to_dict(self):
        """测试转换为字典"""
        rec = OptimizationRecommendation(
            priority=2,
            category="cache",
            title="提高缓存命中率",
            description="增加缓存 TTL",
            potential_savings=1.0,
            action="设置 TTL 为 24 小时"
        )
        
        data = rec.to_dict()
        assert data["priority"] == 2
        assert data["category"] == "cache"
        assert data["potential_savings"] == "1.00"


class TestCostAnalyticsSingleton:
    """成本分析服务单例测试"""
    
    def test_get_cost_analytics_returns_same_instance(self):
        """测试获取成本分析服务返回相同实例"""
        analytics1 = get_cost_analytics()
        analytics2 = get_cost_analytics()
        assert analytics1 is analytics2


class TestTimeRange:
    """时间范围枚举测试"""
    
    def test_time_range_values(self):
        """测试时间范围值"""
        assert TimeRange.DAILY.value == "daily"
        assert TimeRange.WEEKLY.value == "weekly"
        assert TimeRange.MONTHLY.value == "monthly"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
