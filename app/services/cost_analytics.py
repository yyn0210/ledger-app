"""
AI 服务成本监控与优化 - API 调用统计、成本分析、优化建议
"""
import time
from typing import Dict, List, Optional, Any
from dataclasses import dataclass, field
from enum import Enum
from datetime import datetime, timedelta
from loguru import logger

from app.core.gateway_config import ModelConfig, ModelProvider, QualityTier


class TimeRange(str, Enum):
    """时间范围"""
    DAILY = "daily"       # 每日
    WEEKLY = "weekly"     # 每周
    MONTHLY = "monthly"   # 每月


@dataclass
class CostRecord:
    """成本记录"""
    timestamp: float
    task_type: str
    model_name: str
    provider: str
    input_tokens: int
    output_tokens: int
    cost: float
    latency_ms: float
    from_cache: bool = False
    cache_saved: float = 0.0
    
    def to_dict(self) -> Dict:
        return {
            "timestamp": self.timestamp,
            "task_type": self.task_type,
            "model_name": self.model_name,
            "provider": self.provider,
            "input_tokens": self.input_tokens,
            "output_tokens": self.output_tokens,
            "cost": self.cost,
            "latency_ms": self.latency_ms,
            "from_cache": self.from_cache,
            "cache_saved": self.cache_saved,
        }


@dataclass
class ModelCostStats:
    """模型成本统计"""
    model_name: str
    provider: str
    task_type: str
    total_requests: int = 0
    total_input_tokens: int = 0
    total_output_tokens: int = 0
    total_cost: float = 0.0
    total_latency_ms: float = 0.0
    cache_hits: int = 0
    cache_saved: float = 0.0
    
    @property
    def avg_cost_per_request(self) -> float:
        if self.total_requests == 0:
            return 0.0
        return self.total_cost / self.total_requests
    
    @property
    def avg_latency_ms(self) -> float:
        if self.total_requests == 0:
            return 0.0
        return self.total_latency_ms / self.total_requests
    
    @property
    def cache_hit_rate(self) -> float:
        if self.total_requests == 0:
            return 0.0
        return self.cache_hits / self.total_requests
    
    def to_dict(self) -> Dict:
        return {
            "model_name": self.model_name,
            "provider": self.provider,
            "task_type": self.task_type,
            "total_requests": self.total_requests,
            "total_input_tokens": self.total_input_tokens,
            "total_output_tokens": self.total_output_tokens,
            "total_cost": f"{self.total_cost:.4f}",
            "avg_cost_per_request": f"{self.avg_cost_per_request:.4f}",
            "avg_latency_ms": f"{self.avg_latency_ms:.2f}",
            "cache_hits": self.cache_hits,
            "cache_saved": f"{self.cache_saved:.4f}",
            "cache_hit_rate": f"{self.cache_hit_rate:.2%}",
        }


@dataclass
class OptimizationRecommendation:
    """优化建议"""
    priority: int  # 1-5, 1 最高
    category: str  # cost/model/cache
    title: str
    description: str
    potential_savings: float  # 预估节省 (元/月)
    action: str
    
    def to_dict(self) -> Dict:
        return {
            "priority": self.priority,
            "category": self.category,
            "title": self.title,
            "description": self.description,
            "potential_savings": f"{self.potential_savings:.2f}",
            "action": self.action,
        }


class CostAnalyticsService:
    """成本分析服务 - 统一监控和优化 AI 服务成本"""
    
    def __init__(self):
        """初始化成本分析服务"""
        self.records: List[CostRecord] = []
        self.model_stats: Dict[str, ModelCostStats] = {}
        self.daily_costs: Dict[str, float] = {}  # date -> cost
        self.budget_limit: float = 0.0  # 0 表示无限制
        self.budget_alerts: List[Dict] = []
        logger.info("CostAnalyticsService 初始化完成")
    
    def record_request(
        self,
        task_type: str,
        model: ModelConfig,
        input_tokens: int,
        output_tokens: int,
        latency_ms: float,
        from_cache: bool = False
    ):
        """
        记录 API 调用
        
        Args:
            task_type: 任务类型
            model: 使用的模型配置
            input_tokens: 输入 token 数
            output_tokens: 输出 token 数
            latency_ms: 延迟 (毫秒)
            from_cache: 是否来自缓存
        """
        # 计算成本
        cost = (input_tokens / 1000) * model.cost_per_1k_input
        cost += (output_tokens / 1000) * model.cost_per_1k_output
        
        # 计算缓存节省
        cache_saved = cost if from_cache else 0.0
        
        # 创建记录
        record = CostRecord(
            timestamp=time.time(),
            task_type=task_type,
            model_name=model.model_name,
            provider=model.provider.value,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            cost=cost,
            latency_ms=latency_ms,
            from_cache=from_cache,
            cache_saved=cache_saved
        )
        
        # 存储记录
        self.records.append(record)
        
        # 更新模型统计
        self._update_model_stats(record, model)
        
        # 更新每日成本
        self._update_daily_cost(record)
        
        # 检查预算
        self._check_budget()
    
    def _update_model_stats(self, record: CostRecord, model: ModelConfig):
        """更新模型统计"""
        key = f"{record.provider}:{record.model_name}"
        
        if key not in self.model_stats:
            self.model_stats[key] = ModelCostStats(
                model_name=record.model_name,
                provider=record.provider,
                task_type=record.task_type
            )
        
        stats = self.model_stats[key]
        stats.total_requests += 1
        stats.total_input_tokens += record.input_tokens
        stats.total_output_tokens += record.output_tokens
        stats.total_cost += record.cost
        stats.total_latency_ms += record.latency_ms
        
        if record.from_cache:
            stats.cache_hits += 1
            stats.cache_saved += record.cache_saved
    
    def _update_daily_cost(self, record: CostRecord):
        """更新每日成本"""
        date = datetime.fromtimestamp(record.timestamp).strftime("%Y-%m-%d")
        
        if date not in self.daily_costs:
            self.daily_costs[date] = 0.0
        
        if not record.from_cache:
            self.daily_costs[date] += record.cost
    
    def _check_budget(self):
        """检查预算"""
        if self.budget_limit <= 0:
            return
        
        # 计算本月至今成本
        today = datetime.now().strftime("%Y-%m-%d")
        month_prefix = today[:7]  # YYYY-MM
        
        month_cost = sum(
            cost for date, cost in self.daily_costs.items()
            if date.startswith(month_prefix)
        )
        
        # 检查是否超出预算
        if month_cost > self.budget_limit:
            alert = {
                "timestamp": time.time(),
                "type": "budget_exceeded",
                "month": month_prefix,
                "cost": month_cost,
                "limit": self.budget_limit,
                "percentage": month_cost / self.budget_limit * 100
            }
            
            # 避免重复告警
            if not any(
                a["month"] == month_prefix 
                for a in self.budget_alerts
            ):
                self.budget_alerts.append(alert)
                logger.warning(
                    f"预算超支告警：{month_prefix} 月成本 {month_cost:.2f} 元 "
                    f"(预算：{self.budget_limit:.2f} 元，{alert['percentage']:.1f}%)"
                )
    
    def get_model_stats(self, model_name: Optional[str] = None) -> Dict:
        """
        获取模型统计
        
        Args:
            model_name: 模型名称，None 表示所有模型
            
        Returns:
            统计字典
        """
        if model_name:
            key = next(
                (k for k in self.model_stats.keys() if model_name in k),
                None
            )
            if key and key in self.model_stats:
                return self.model_stats[key].to_dict()
            return {}
        
        return {
            key: stats.to_dict()
            for key, stats in self.model_stats.items()
        }
    
    def get_cost_summary(
        self,
        time_range: TimeRange = TimeRange.DAILY
    ) -> Dict:
        """
        获取成本摘要
        
        Args:
            time_range: 时间范围
            
        Returns:
            成本摘要字典
        """
        now = datetime.now()
        
        # 筛选时间范围内的记录
        if time_range == TimeRange.DAILY:
            start_time = now.replace(hour=0, minute=0, second=0, microsecond=0)
        elif time_range == TimeRange.WEEKLY:
            start_time = now - timedelta(days=7)
        else:  # MONTHLY
            start_time = now.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
        
        start_timestamp = start_time.timestamp()
        filtered = [r for r in self.records if r.timestamp >= start_timestamp]
        
        # 计算总计
        total_cost = sum(r.cost for r in filtered if not r.from_cache)
        total_cache_saved = sum(r.cache_saved for r in filtered)
        total_requests = len(filtered)
        cache_hits = sum(1 for r in filtered if r.from_cache)
        
        # 按任务类型分组
        by_task_type: Dict[str, Dict] = {}
        for record in filtered:
            if record.task_type not in by_task_type:
                by_task_type[record.task_type] = {
                    "total_cost": 0.0,
                    "total_requests": 0,
                    "cache_hits": 0,
                    "cache_saved": 0.0,
                }
            if not record.from_cache:
                by_task_type[record.task_type]["total_cost"] += record.cost
            by_task_type[record.task_type]["total_requests"] += 1
            if record.from_cache:
                by_task_type[record.task_type]["cache_hits"] += 1
                by_task_type[record.task_type]["cache_saved"] += record.cache_saved
        
        return {
            "time_range": time_range.value,
            "start_date": start_time.strftime("%Y-%m-%d %H:%M:%S"),
            "end_date": now.strftime("%Y-%m-%d %H:%M:%S"),
            "total_cost": f"{total_cost:.4f}",
            "total_requests": total_requests,
            "cache_hits": cache_hits,
            "cache_hit_rate": f"{cache_hits / max(1, total_requests):.2%}",
            "total_cache_saved": f"{total_cache_saved:.4f}",
            "effective_cost": f"{total_cost - total_cache_saved:.4f}",
            "by_task_type": {
                k: {
                    "total_cost": f"{v['total_cost']:.4f}",
                    "total_requests": v["total_requests"],
                    "cache_hits": v["cache_hits"],
                    "cache_hit_rate": f"{v['cache_hits'] / max(1, v['total_requests']):.2%}",
                    "cache_saved": f"{v['cache_saved']:.4f}",
                }
                for k, v in by_task_type.items()
            },
            "model_stats": self.get_model_stats(),
        }
    
    def get_daily_costs(self, days: int = 7) -> List[Dict]:
        """
        获取每日成本
        
        Args:
            days: 天数
            
        Returns:
            每日成本列表
        """
        result = []
        for i in range(days):
            date = (datetime.now() - timedelta(days=i)).strftime("%Y-%m-%d")
            cost = self.daily_costs.get(date, 0.0)
            result.append({
                "date": date,
                "cost": f"{cost:.4f}",
            })
        return result
    
    def generate_recommendations(self) -> List[OptimizationRecommendation]:
        """生成优化建议"""
        recommendations = []
        
        # 1. 高成本模型优化建议
        for key, stats in self.model_stats.items():
            if stats.total_cost > 1.0:  # 成本超过 1 元
                # 检查是否有更便宜的替代
                if stats.task_type == "nlp":
                    recommendations.append(OptimizationRecommendation(
                        priority=2,
                        category="cost",
                        title=f"优化 {stats.model_name} 使用",
                        description=f"{stats.model_name} 成本 {stats.total_cost:.2f} 元，考虑使用更经济的模型",
                        potential_savings=stats.total_cost * 0.5,  # 预估节省 50%
                        action="切换到 Qwen-Turbo 或 DeepSeek-Chat"
                    ))
                elif stats.task_type == "ocr":
                    recommendations.append(OptimizationRecommendation(
                        priority=2,
                        category="cost",
                        title=f"优化 {stats.model_name} OCR 使用",
                        description=f"{stats.model_name} 成本 {stats.total_cost:.2f} 元，非关键场景使用经济模型",
                        potential_savings=stats.total_cost * 0.6,
                        action="切换到 Qwen-VL-Plus"
                    ))
        
        # 2. 缓存优化建议
        total_cache_hit_rate = sum(
            s.cache_hits for s in self.model_stats.values()
        ) / max(1, sum(s.total_requests for s in self.model_stats.values()))
        
        if total_cache_hit_rate < 0.3:  # 命中率低于 30%
            recommendations.append(OptimizationRecommendation(
                priority=1,
                category="cache",
                title="提高缓存命中率",
                description=f"当前缓存命中率 {total_cache_hit_rate:.1%}，目标 50%+",
                potential_savings=0.5,  # 预估每月节省
                action="增加缓存 TTL 或扩大缓存范围"
            ))
        
        # 3. 高延迟模型优化
        for key, stats in self.model_stats.items():
            if stats.avg_latency_ms > 1000:  # 平均延迟超过 1 秒
                recommendations.append(OptimizationRecommendation(
                    priority=3,
                    category="model",
                    title=f"优化 {stats.model_name} 延迟",
                    description=f"{stats.model_name} 平均延迟 {stats.avg_latency_ms:.0f}ms，考虑更快的模型",
                    potential_savings=0.2,
                    action="切换到 turbo 或 fast 版本模型"
                ))
        
        # 4. 预算告警
        if self.budget_alerts:
            latest_alert = self.budget_alerts[-1]
            recommendations.append(OptimizationRecommendation(
                priority=1,
                category="cost",
                title="预算超支告警",
                description=f"{latest_alert['month']} 月成本已超预算 {latest_alert['percentage']:.1f}%",
                potential_savings=latest_alert['cost'] * 0.3,
                action="立即优化模型选择或降低调用频率"
            ))
        
        # 按优先级排序
        recommendations.sort(key=lambda x: x.priority)
        
        return recommendations
    
    def set_budget_limit(self, limit: float):
        """设置预算限制"""
        self.budget_limit = limit
        logger.info(f"预算限制设置为：{limit:.2f} 元/月")
    
    def get_budget_status(self) -> Dict:
        """获取预算状态"""
        if self.budget_limit <= 0:
            return {"enabled": False}
        
        today = datetime.now().strftime("%Y-%m-%d")
        month_prefix = today[:7]
        
        month_cost = sum(
            cost for date, cost in self.daily_costs.items()
            if date.startswith(month_prefix)
        )
        
        remaining = self.budget_limit - month_cost
        percentage = month_cost / self.budget_limit * 100
        
        return {
            "enabled": True,
            "limit": f"{self.budget_limit:.2f}",
            "spent": f"{month_cost:.2f}",
            "remaining": f"{remaining:.2f}",
            "percentage": f"{percentage:.1f}%",
            "alerts": len(self.budget_alerts),
        }
    
    def export_report(self, time_range: TimeRange = TimeRange.MONTHLY) -> Dict:
        """导出成本报告"""
        summary = self.get_cost_summary(time_range)
        recommendations = self.generate_recommendations()
        budget_status = self.get_budget_status()
        
        return {
            "report_type": "cost_analysis",
            "generated_at": datetime.now().isoformat(),
            "summary": summary,
            "daily_costs": self.get_daily_costs(30),
            "model_stats": self.get_model_stats(),
            "recommendations": [r.to_dict() for r in recommendations],
            "budget_status": budget_status,
        }


# 单例
_cost_analytics: Optional[CostAnalyticsService] = None


def get_cost_analytics() -> CostAnalyticsService:
    """获取成本分析服务"""
    global _cost_analytics
    if _cost_analytics is None:
        _cost_analytics = CostAnalyticsService()
    return _cost_analytics
