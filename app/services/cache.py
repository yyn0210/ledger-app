"""
AI 服务缓存层 - Redis 缓存、缓存策略、缓存失效
"""
import hashlib
import json
import time
from typing import Any, Dict, Optional, List
from dataclasses import dataclass, field
from enum import Enum
from loguru import logger
import redis.asyncio as redis

from app.core.config import settings


class CacheStrategy(str, Enum):
    """缓存策略"""
    CACHE_FIRST = "cache_first"              # 优先缓存
    NETWORK_FIRST = "network_first"          # 优先网络
    CACHE_ONLY = "cache_only"                # 仅缓存 (测试用)


class CacheOperation(str, Enum):
    """缓存操作类型"""
    GET = "get"
    SET = "set"
    DELETE = "delete"
    INVALIDATE = "invalidate"


@dataclass
class CacheEntry:
    """缓存条目"""
    key: str
    value: Any
    created_at: float
    ttl: int
    hit_count: int = 0
    task_type: str = ""
    model_used: str = ""
    cost_saved: float = 0.0
    
    def is_expired(self) -> bool:
        """检查是否过期"""
        return time.time() > (self.created_at + self.ttl)
    
    def to_dict(self) -> Dict:
        """转换为字典"""
        return {
            "key": self.key,
            "value": self.value,
            "created_at": self.created_at,
            "ttl": self.ttl,
            "hit_count": self.hit_count,
            "task_type": self.task_type,
            "model_used": self.model_used,
            "cost_saved": self.cost_saved,
        }
    
    @classmethod
    def from_dict(cls, data: Dict) -> "CacheEntry":
        """从字典创建"""
        return cls(**data)


@dataclass
class CacheStats:
    """缓存统计"""
    task_type: str
    hits: int = 0
    misses: int = 0
    sets: int = 0
    deletes: int = 0
    total_size_bytes: int = 0
    total_cost_saved: float = 0.0
    
    @property
    def hit_rate(self) -> float:
        """命中率"""
        total = self.hits + self.misses
        if total == 0:
            return 0.0
        return self.hits / total
    
    @property
    def total_requests(self) -> int:
        """总请求数"""
        return self.hits + self.misses
    
    def to_dict(self) -> Dict:
        """转换为字典"""
        return {
            "task_type": self.task_type,
            "hits": self.hits,
            "misses": self.misses,
            "sets": self.sets,
            "deletes": self.deletes,
            "total_size_bytes": self.total_size_bytes,
            "total_cost_saved": self.total_cost_saved,
            "hit_rate": f"{self.hit_rate:.2%}",
            "total_requests": self.total_requests,
        }


class CacheService:
    """缓存服务 - 统一管理 AI 服务缓存"""
    
    # 任务类型默认 TTL (秒)
    DEFAULT_TTL = {
        "ocr": 86400,    # 24 小时 - OCR 结果通常不变
        "stt": 86400,    # 24 小时 - 语音转写结果不变
        "nlp": 3600,     # 1 小时 - NLP 结果可能变化
    }
    
    # 缓存键前缀
    KEY_PREFIX = "ledger:ai"
    
    def __init__(self, redis_pool: Optional[redis.Redis] = None):
        """
        初始化缓存服务
        
        Args:
            redis_pool: Redis 连接池
        """
        self.redis = redis_pool
        self.stats: Dict[str, CacheStats] = {}
        self._init_stats()
        logger.info(f"CacheService 初始化完成 - 策略：{settings.CACHE_STRATEGY}")
    
    def _init_stats(self):
        """初始化统计"""
        for task_type in self.DEFAULT_TTL.keys():
            self.stats[task_type] = CacheStats(task_type=task_type)
    
    def _generate_key(self, task_type: str, input_data: Dict, model_name: str = "") -> str:
        """
        生成缓存键
        
        Args:
            task_type: 任务类型 (ocr/stt/nlp)
            input_data: 输入数据
            model_name: 模型名称 (可选)
            
        Returns:
            缓存键字符串
        """
        # 序列化输入数据
        input_str = json.dumps(input_data, sort_keys=True)
        
        # 生成哈希
        input_hash = hashlib.md5(input_str.encode()).hexdigest()[:16]
        
        # 构建键：ledger:ai:{task_type}:{model}:{input_hash}
        key_parts = [self.KEY_PREFIX, task_type]
        if model_name:
            key_parts.append(model_name.replace(":", "_"))
        key_parts.append(input_hash)
        
        return ":".join(key_parts)
    
    async def get(self, key: str, task_type: str = "nlp") -> Optional[CacheEntry]:
        """
        获取缓存
        
        Args:
            key: 缓存键
            task_type: 任务类型
            
        Returns:
            缓存条目，如果不存在或已过期返回 None
        """
        if not settings.CACHE_ENABLED:
            return None
        
        try:
            if self.redis is None:
                logger.debug("Redis 未连接，跳过缓存读取")
                self.stats[task_type].misses += 1
                return None
            
            data = await self.redis.get(key)
            if data is None:
                logger.debug(f"缓存未命中：{key}")
                self.stats[task_type].misses += 1
                return None
            
            # 反序列化
            entry_data = json.loads(data)
            entry = CacheEntry.from_dict(entry_data)
            
            # 检查过期
            if entry.is_expired():
                logger.debug(f"缓存已过期：{key}")
                await self.delete(key, task_type)
                self.stats[task_type].misses += 1
                return None
            
            # 更新命中计数
            entry.hit_count += 1
            self.stats[task_type].hits += 1
            
            logger.debug(f"缓存命中：{key} (hit_count={entry.hit_count})")
            return entry
            
        except Exception as e:
            logger.error(f"缓存读取失败：{key}, 错误：{e}")
            self.stats[task_type].misses += 1
            return None
    
    async def set(
        self,
        key: str,
        value: Any,
        task_type: str = "nlp",
        ttl: Optional[int] = None,
        model_used: str = "",
        cost_saved: float = 0.0
    ) -> bool:
        """
        设置缓存
        
        Args:
            key: 缓存键
            value: 缓存值
            task_type: 任务类型
            ttl: 过期时间 (秒)，None 使用默认值
            model_used: 使用的模型
            cost_saved: 节省的成本
            
        Returns:
            是否成功
        """
        if not settings.CACHE_ENABLED:
            return False
        
        try:
            # 使用默认 TTL
            if ttl is None:
                ttl = self.DEFAULT_TTL.get(task_type, settings.CACHE_DEFAULT_TTL)
            
            # 创建缓存条目
            entry = CacheEntry(
                key=key,
                value=value,
                created_at=time.time(),
                ttl=ttl,
                task_type=task_type,
                model_used=model_used,
                cost_saved=cost_saved,
            )
            
            if self.redis is None:
                logger.debug(f"Redis 未连接，跳过缓存写入：{key}")
                self.stats[task_type].sets += 1
                return False
            
            # 序列化并存储
            await self.redis.setex(
                key,
                ttl,
                json.dumps(entry.to_dict())
            )
            
            self.stats[task_type].sets += 1
            self.stats[task_type].total_size_bytes += len(json.dumps(entry.to_dict()))
            self.stats[task_type].total_cost_saved += cost_saved
            
            logger.debug(f"缓存写入成功：{key} (ttl={ttl}s)")
            return True
            
        except Exception as e:
            logger.error(f"缓存写入失败：{key}, 错误：{e}")
            return False
    
    async def delete(self, key: str, task_type: str = "nlp") -> bool:
        """
        删除缓存
        
        Args:
            key: 缓存键
            task_type: 任务类型
            
        Returns:
            是否成功
        """
        if not settings.CACHE_ENABLED:
            return False
        
        try:
            if self.redis is None:
                return False
            
            await self.redis.delete(key)
            self.stats[task_type].deletes += 1
            logger.debug(f"缓存删除成功：{key}")
            return True
            
        except Exception as e:
            logger.error(f"缓存删除失败：{key}, 错误：{e}")
            return False
    
    async def invalidate_pattern(self, pattern: str, task_type: str = "nlp") -> int:
        """
        批量删除匹配模式的缓存
        
        Args:
            pattern: 匹配模式 (支持通配符 *)
            task_type: 任务类型
            
        Returns:
            删除的缓存数量
        """
        if not settings.CACHE_ENABLED:
            return 0
        
        try:
            if self.redis is None:
                return 0
            
            # 扫描匹配的键
            deleted_count = 0
            async for key in self.redis.scan_iter(match=pattern):
                await self.redis.delete(key)
                deleted_count += 1
            
            logger.info(f"批量删除缓存：{pattern}, 删除 {deleted_count} 个")
            return deleted_count
            
        except Exception as e:
            logger.error(f"批量删除缓存失败：{pattern}, 错误：{e}")
            return 0
    
    async def invalidate_by_input(
        self,
        task_type: str,
        input_hash: str,
        all_models: bool = True
    ) -> int:
        """
        根据输入哈希删除缓存
        
        Args:
            task_type: 任务类型
            input_hash: 输入哈希
            all_models: 是否删除所有模型的缓存
            
        Returns:
            删除的缓存数量
        """
        if all_models:
            pattern = f"{self.KEY_PREFIX}:{task_type}:*:{input_hash}"
        else:
            pattern = f"{self.KEY_PREFIX}:{task_type}:*:{input_hash}"
        
        return await self.invalidate_pattern(pattern, task_type)
    
    async def invalidate_all(self, task_type: Optional[str] = None) -> int:
        """
        删除所有缓存
        
        Args:
            task_type: 任务类型，None 表示删除所有类型
            
        Returns:
            删除的缓存数量
        """
        if not settings.CACHE_ENABLED:
            return 0
        
        try:
            if self.redis is None:
                return 0
            
            if task_type:
                pattern = f"{self.KEY_PREFIX}:{task_type}:*"
            else:
                pattern = f"{self.KEY_PREFIX}:*"
            
            return await self.invalidate_pattern(pattern, task_type or "nlp")
            
        except Exception as e:
            logger.error(f"删除所有缓存失败，错误：{e}")
            return 0
    
    def get_stats(self, task_type: Optional[str] = None) -> Dict:
        """
        获取缓存统计
        
        Args:
            task_type: 任务类型，None 表示所有类型
            
        Returns:
            统计字典
        """
        if task_type:
            if task_type in self.stats:
                return self.stats[task_type].to_dict()
            return {}
        
        # 返回所有类型的统计
        return {
            task_type: stats.to_dict()
            for task_type, stats in self.stats.items()
        }
    
    def get_summary(self) -> Dict:
        """获取缓存摘要"""
        total_hits = sum(s.hits for s in self.stats.values())
        total_misses = sum(s.misses for s in self.stats.values())
        total_requests = total_hits + total_misses
        overall_hit_rate = total_hits / max(1, total_requests)
        total_cost_saved = sum(s.total_cost_saved for s in self.stats.values())
        
        return {
            "enabled": settings.CACHE_ENABLED,
            "strategy": settings.CACHE_STRATEGY,
            "total_requests": total_requests,
            "total_hits": total_hits,
            "total_misses": total_misses,
            "overall_hit_rate": f"{overall_hit_rate:.2%}",
            "total_cost_saved": f"{total_cost_saved:.4f} 元",
            "by_task_type": self.get_stats(),
        }
    
    async def close(self):
        """关闭连接"""
        if self.redis:
            await self.redis.close()
            logger.info("缓存服务已关闭")


# 缓存服务单例
_cache_service: Optional[CacheService] = None


def get_cache_service(redis_pool: Optional[redis.Redis] = None) -> CacheService:
    """获取或创建缓存服务"""
    global _cache_service
    if _cache_service is None:
        _cache_service = CacheService(redis_pool)
    return _cache_service


# 便捷装饰器
def cache_result(task_type: str = "nlp", ttl: Optional[int] = None):
    """
    缓存结果装饰器
    
    Args:
        task_type: 任务类型
        ttl: 过期时间
        
    Usage:
        @cache_result(task_type="ocr", ttl=86400)
        async def recognize_receipt(image_url: str):
            ...
    """
    def decorator(func):
        async def wrapper(*args, **kwargs):
            cache = get_cache_service()
            
            # 生成缓存键 (简单实现，实际应该根据参数生成)
            key = cache._generate_key(
                task_type,
                {"args": str(args), "kwargs": str(kwargs)}
            )
            
            # 尝试从缓存获取
            entry = await cache.get(key, task_type)
            if entry:
                return entry.value
            
            # 执行函数
            result = await func(*args, **kwargs)
            
            # 写入缓存
            await cache.set(key, result, task_type, ttl)
            
            return result
        return wrapper
    return decorator
