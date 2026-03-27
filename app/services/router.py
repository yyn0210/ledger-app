"""
智能路由策略 - 成本优化、质量分级、模型降级
"""
import time
from typing import Dict, List, Optional, Any, Callable
from dataclasses import dataclass, field
from enum import Enum
from loguru import logger
import random

from app.core.gateway_config import (
    ModelConfig, ModelProvider, QualityTier,
    DEFAULT_MODEL_CONFIGS, gateway_config
)


class RoutingStrategy(str, Enum):
    """路由策略"""
    COST_OPTIMIZED = "cost_optimized"      # 成本优先
    QUALITY_FIRST = "quality_first"        # 质量优先
    BALANCED = "balanced"                  # 平衡模式
    LATENCY_OPTIMIZED = "latency_optimized"  # 延迟优先


class TaskPriority(str, Enum):
    """任务优先级"""
    CRITICAL = "critical"    # 关键任务 (用最好模型)
    HIGH = "high"           # 高优先级
    NORMAL = "normal"       # 普通任务
    LOW = "low"             # 低优先级 (用经济模型)


@dataclass
class RoutingContext:
    """路由上下文"""
    task_type: str  # ocr/stt/nlp
    priority: TaskPriority = TaskPriority.NORMAL
    strategy: RoutingStrategy = RoutingStrategy.BALANCED
    budget_limit: float = 0.0  # 预算限制 (元)，0 表示无限制
    required_capabilities: List[str] = field(default_factory=list)
    min_quality_tier: QualityTier = QualityTier.ECONOMY
    max_latency_ms: float = 0.0  # 最大可接受延迟，0 表示无限制
    input_tokens: int = 0  # 预估输入 token 数
    output_tokens: int = 0  # 预估输出 token 数


@dataclass
class ModelScore:
    """模型评分"""
    model: ModelConfig
    score: float
    cost_estimate: float = 0.0
    latency_estimate: float = 0.0
    reasons: List[str] = field(default_factory=list)


class IntelligentRouter:
    """智能路由器 - 根据成本、质量、延迟等因素选择最优模型"""
    
    def __init__(self, task_type: str):
        """
        初始化路由器
        
        Args:
            task_type: 任务类型 (ocr/stt/nlp)
        """
        self.task_type = task_type
        self.models = DEFAULT_MODEL_CONFIGS.get(task_type, [])
        self.model_stats: Dict[str, Dict] = {}  # 模型历史统计
        self._init_stats()
        logger.info(f"IntelligentRouter 初始化完成 - 任务类型：{task_type}, 模型数：{len(self.models)}")
    
    def _init_stats(self):
        """初始化模型统计"""
        for model in self.models:
            key = self._model_key(model)
            self.model_stats[key] = {
                "total_requests": 0,
                "successful_requests": 0,
                "failed_requests": 0,
                "total_cost": 0.0,
                "avg_latency_ms": 0.0,
                "last_failure_time": 0.0,
                "consecutive_failures": 0,
            }
    
    def _model_key(self, model: ModelConfig) -> str:
        """生成模型键"""
        return f"{model.provider.value}:{model.model_name}"
    
    def select_model(self, context: RoutingContext) -> Optional[ModelConfig]:
        """
        根据路由上下文选择最优模型
        
        Args:
            context: 路由上下文
            
        Returns:
            最优模型配置
        """
        # 过滤可用模型
        available = self._filter_available_models(context)
        if not available:
            logger.error(f"没有可用的模型 (task_type={self.task_type})")
            return None
        
        if len(available) == 1:
            return available[0]
        
        # 根据策略评分
        scored = self._score_models(available, context)
        
        # 选择得分最高的模型
        best = max(scored, key=lambda x: x.score)
        logger.info(f"智能路由选择：{self._model_key(best.model)} (得分：{best.score:.2f}, 策略：{context.strategy.value})")
        
        return best.model
    
    def _filter_available_models(self, context: RoutingContext) -> List[ModelConfig]:
        """过滤可用模型"""
        available = []
        for model in self.models:
            # 检查启用状态
            if not model.enabled:
                continue
            
            # 检查质量等级
            if self._get_tier_priority(model.quality_tier) < self._get_tier_priority(context.min_quality_tier):
                continue
            
            # 检查必需能力
            if context.required_capabilities:
                if not any(cap in model.capabilities for cap in context.required_capabilities):
                    continue
            
            # 检查熔断状态
            key = self._model_key(model)
            stats = self.model_stats.get(key, {})
            if stats.get("consecutive_failures", 0) >= 5:
                # 连续失败 5 次，暂时禁用
                if time.time() - stats.get("last_failure_time", 0) < 300:  # 5 分钟内
                    continue
            
            available.append(model)
        
        return available
    
    def _score_models(self, models: List[ModelConfig], context: RoutingContext) -> List[ModelScore]:
        """对模型进行评分"""
        scored = []
        for model in models:
            score = self._calculate_score(model, context)
            scored.append(score)
        return scored
    
    def _calculate_score(self, model: ModelConfig, context: RoutingContext) -> ModelScore:
        """计算单个模型得分"""
        key = self._model_key(model)
        stats = self.model_stats.get(key, {})
        
        # 基础得分 (100 分)
        base_score = 100.0
        reasons = []
        
        # 1. 成本评分 (权重 40%)
        cost_estimate = self._estimate_cost(model, context)
        cost_score = self._calculate_cost_score(cost_estimate, context)
        reasons.append(f"成本：{cost_score:.1f}/40")
        
        # 2. 质量评分 (权重 30%)
        quality_score = self._calculate_quality_score(model, context)
        reasons.append(f"质量：{quality_score:.1f}/30")
        
        # 3. 延迟评分 (权重 20%)
        latency_score = self._calculate_latency_score(model, stats, context)
        reasons.append(f"延迟：{latency_score:.1f}/20")
        
        # 4. 可靠性评分 (权重 10%)
        reliability_score = self._calculate_reliability_score(stats)
        reasons.append(f"可靠性：{reliability_score:.1f}/10")
        
        # 根据策略调整权重
        if context.strategy == RoutingStrategy.COST_OPTIMIZED:
            final_score = cost_score * 1.0 + quality_score * 0.5 + latency_score * 0.3 + reliability_score * 0.2
        elif context.strategy == RoutingStrategy.QUALITY_FIRST:
            final_score = quality_score * 1.0 + reliability_score * 0.8 + latency_score * 0.5 + cost_score * 0.3
        elif context.strategy == RoutingStrategy.LATENCY_OPTIMIZED:
            final_score = latency_score * 1.0 + reliability_score * 0.7 + quality_score * 0.5 + cost_score * 0.3
        else:  # BALANCED
            final_score = cost_score + quality_score + latency_score + reliability_score
        
        return ModelScore(
            model=model,
            score=final_score,
            cost_estimate=cost_estimate,
            latency_estimate=stats.get("avg_latency_ms", 0),
            reasons=reasons
        )
    
    def _estimate_cost(self, model: ModelConfig, context: RoutingContext) -> float:
        """预估成本"""
        input_cost = (context.input_tokens / 1000) * model.cost_per_1k_input
        output_cost = (context.output_tokens / 1000) * model.cost_per_1k_output
        return input_cost + output_cost
    
    def _calculate_cost_score(self, cost: float, context: RoutingContext) -> float:
        """计算成本得分 (40 分制)"""
        if context.budget_limit > 0 and cost > context.budget_limit:
            return 0.0  # 超出预算
        
        # 找到最便宜的模型成本作为基准
        min_cost = min(
            self._estimate_cost(m, context) 
            for m in self.models if m.enabled
        )
        
        if min_cost == 0:
            return 40.0
        
        # 成本越低得分越高
        ratio = min_cost / max(cost, 0.001)
        return min(40.0, ratio * 40.0)
    
    def _calculate_quality_score(self, model: ModelConfig, context: RoutingContext) -> float:
        """计算质量得分 (30 分制)"""
        tier_priority = {
            QualityTier.PREMIUM: 30.0,
            QualityTier.STANDARD: 20.0,
            QualityTier.ECONOMY: 10.0,
        }
        
        base = tier_priority.get(model.quality_tier, 15.0)
        
        # 优先级匹配加分
        if context.priority == TaskPriority.CRITICAL and model.quality_tier == QualityTier.PREMIUM:
            base += 5.0
        elif context.priority == TaskPriority.LOW and model.quality_tier == QualityTier.ECONOMY:
            base += 5.0  # 低优先级用经济模型也加分
        
        return min(30.0, base)
    
    def _calculate_latency_score(self, model: ModelConfig, stats: Dict, context: RoutingContext) -> float:
        """计算延迟得分 (20 分制)"""
        avg_latency = stats.get("avg_latency_ms", 500)  # 默认 500ms
        
        # 如果有最大延迟要求
        if context.max_latency_ms > 0 and avg_latency > context.max_latency_ms:
            return 0.0
        
        # 延迟越低得分越高
        if avg_latency < 100:
            return 20.0
        elif avg_latency < 300:
            return 15.0
        elif avg_latency < 500:
            return 10.0
        elif avg_latency < 1000:
            return 5.0
        else:
            return 2.0
    
    def _calculate_reliability_score(self, stats: Dict) -> float:
        """计算可靠性得分 (10 分制)"""
        total = stats.get("total_requests", 0)
        success = stats.get("successful_requests", 0)
        consecutive_failures = stats.get("consecutive_failures", 0)
        
        if total == 0:
            return 5.0  # 无历史数据，中等分数
        
        success_rate = success / total
        
        # 成功率基础分
        base = success_rate * 10.0
        
        # 连续失败扣分
        if consecutive_failures > 0:
            base -= consecutive_failures * 2.0
        
        return max(0.0, min(10.0, base))
    
    def _get_tier_priority(self, tier: QualityTier) -> int:
        """获取质量等级优先级"""
        return {
            QualityTier.PREMIUM: 3,
            QualityTier.STANDARD: 2,
            QualityTier.ECONOMY: 1,
        }.get(tier, 0)
    
    def record_success(self, model: ModelConfig, latency_ms: float, tokens_used: int = 0):
        """记录成功请求"""
        key = self._model_key(model)
        stats = self.model_stats.setdefault(key, {
            "total_requests": 0,
            "successful_requests": 0,
            "failed_requests": 0,
            "total_cost": 0.0,
            "avg_latency_ms": 0.0,
            "last_failure_time": 0.0,
            "consecutive_failures": 0,
        })
        
        stats["total_requests"] += 1
        stats["successful_requests"] += 1
        stats["consecutive_failures"] = 0
        
        # 更新平均延迟
        n = stats["successful_requests"]
        old_avg = stats["avg_latency_ms"]
        stats["avg_latency_ms"] = old_avg + (latency_ms - old_avg) / n
        
        # 更新成本
        cost = self._estimate_cost(model, RoutingContext(
            task_type=self.task_type,
            input_tokens=tokens_used,
            output_tokens=0
        ))
        stats["total_cost"] += cost
    
    def record_failure(self, model: ModelConfig):
        """记录失败请求"""
        key = self._model_key(model)
        stats = self.model_stats.setdefault(key, {
            "total_requests": 0,
            "successful_requests": 0,
            "failed_requests": 0,
            "total_cost": 0.0,
            "avg_latency_ms": 0.0,
            "last_failure_time": 0.0,
            "consecutive_failures": 0,
        })
        
        stats["total_requests"] += 1
        stats["failed_requests"] += 1
        stats["consecutive_failures"] += 1
        stats["last_failure_time"] = time.time()
    
    def get_cost_report(self) -> Dict:
        """获取成本报告"""
        report = {
            "task_type": self.task_type,
            "total_cost": 0.0,
            "models": {}
        }
        
        for key, stats in self.model_stats.items():
            model_cost = stats.get("total_cost", 0.0)
            report["models"][key] = {
                "total_cost": model_cost,
                "total_requests": stats.get("total_requests", 0),
                "avg_cost_per_request": model_cost / max(1, stats.get("total_requests", 1))
            }
            report["total_cost"] += model_cost
        
        return report
    
    def degrade_model(self, current_model: ModelConfig) -> Optional[ModelConfig]:
        """
        模型降级 - 当当前模型失败时，选择更低成本/质量的备选
        
        Args:
            current_model: 当前失败的模型
            
        Returns:
            降级后的备选模型
        """
        tier_order = [QualityTier.PREMIUM, QualityTier.STANDARD, QualityTier.ECONOMY]
        current_tier_idx = tier_order.index(current_model.quality_tier)
        
        # 尝试更低等级的模型
        for tier_idx in range(current_tier_idx + 1, len(tier_order)):
            target_tier = tier_order[tier_idx]
            for model in self.models:
                if model.quality_tier == target_tier and model.enabled:
                    logger.info(f"模型降级：{self._model_key(current_model)} → {self._model_key(model)}")
                    return model
        
        # 如果没有更低等级，尝试同等级其他模型
        for model in self.models:
            if model.quality_tier == current_model.quality_tier and model.enabled:
                if model.model_name != current_model.model_name:
                    return model
        
        return None


# 路由器单例
_routers: Dict[str, IntelligentRouter] = {}


def get_router(task_type: str) -> IntelligentRouter:
    """获取或创建路由器"""
    if task_type not in _routers:
        _routers[task_type] = IntelligentRouter(task_type)
    return _routers[task_type]


# 便捷函数
async def route_and_execute(
    task_type: str,
    execute_fn: Callable,
    context: Optional[RoutingContext] = None,
    *args,
    **kwargs
) -> Any:
    """
    智能路由并执行
    
    Args:
        task_type: 任务类型
        execute_fn: 执行函数 (接收 model_config 作为第一个参数)
        context: 路由上下文
        *args: 位置参数
        **kwargs: 关键字参数
        
    Returns:
        模型调用结果
    """
    router = get_router(task_type)
    
    if context is None:
        context = RoutingContext(task_type=task_type)
    
    # 选择模型
    model = router.select_model(context)
    if not model:
        raise Exception("没有可用的模型")
    
    try:
        # 执行调用
        result = await execute_fn(model, *args, **kwargs)
        router.record_success(model, 0)  # latency 由上层记录
        return result
    except Exception as e:
        router.record_failure(model)
        
        # 尝试降级
        degraded = router.degrade_model(model)
        if degraded:
            logger.info(f"尝试降级模型：{degraded.model_name}")
            try:
                result = await execute_fn(degraded, *args, **kwargs)
                router.record_success(degraded, 0)
                return result
            except Exception as e2:
                raise e2
        raise e
