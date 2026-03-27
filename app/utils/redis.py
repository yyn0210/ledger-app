"""
Redis 缓存工具
"""
import json
from typing import Optional, Any
from loguru import logger
from app.core.config import settings

# 全局 Redis 连接池（在 main.py 中初始化）
redis_pool = None


def set_redis_pool(pool):
    """设置 Redis 连接池"""
    global redis_pool
    redis_pool = pool


async def cache_set(key: str, value: Any, expire_seconds: int = 3600):
    """
    缓存数据
    
    Args:
        key: 缓存键
        value: 缓存值（自动序列化为 JSON）
        expire_seconds: 过期时间（秒）
    """
    try:
        if redis_pool is None:
            logger.warning("Redis 连接池未初始化")
            return
        
        await redis_pool.setex(key, expire_seconds, json.dumps(value, ensure_ascii=False))
        logger.debug(f"缓存设置成功：{key}")
    except Exception as e:
        logger.error(f"缓存设置失败：{key}, error: {e}")


async def cache_get(key: str) -> Optional[Any]:
    """
    获取缓存数据
    
    Args:
        key: 缓存键
        
    Returns:
        缓存值，如果不存在则返回 None
    """
    try:
        if redis_pool is None:
            logger.warning("Redis 连接池未初始化")
            return None
        
        cached = await redis_pool.get(key)
        if cached:
            logger.debug(f"缓存命中：{key}")
            return json.loads(cached)
        logger.debug(f"缓存未命中：{key}")
        return None
    except Exception as e:
        logger.error(f"缓存获取失败：{key}, error: {e}")
        return None


async def cache_delete(key: str):
    """
    删除缓存
    
    Args:
        key: 缓存键
    """
    try:
        if redis_pool is None:
            logger.warning("Redis 连接池未初始化")
            return
        
        await redis_pool.delete(key)
        logger.debug(f"缓存删除成功：{key}")
    except Exception as e:
        logger.error(f"缓存删除失败：{key}, error: {e}")


# OCR 缓存辅助函数
async def cache_ocr_result(ocr_id: str, result: dict, expire_seconds: int = None):
    """缓存 OCR 识别结果"""
    if expire_seconds is None:
        expire_seconds = settings.OCR_CACHE_EXPIRE
    key = f"ocr:result:{ocr_id}"
    await cache_set(key, result, expire_seconds)


async def get_cached_ocr_result(ocr_id: str) -> Optional[dict]:
    """获取缓存的 OCR 结果"""
    key = f"ocr:result:{ocr_id}"
    return await cache_get(key)


# STT 缓存辅助函数
async def cache_stt_result(stt_id: str, result: dict, expire_seconds: int = None):
    """缓存 STT 识别结果"""
    if expire_seconds is None:
        expire_seconds = settings.STT_CACHE_EXPIRE
    key = f"stt:result:{stt_id}"
    await cache_set(key, result, expire_seconds)


async def get_cached_stt_result(stt_id: str) -> Optional[dict]:
    """获取缓存的 STT 结果"""
    key = f"stt:result:{stt_id}"
    return await cache_get(key)


# NLP 缓存辅助函数
async def cache_nlp_result(nlp_id: str, result: dict, expire_seconds: int = None):
    """缓存 NLP 识别结果"""
    if expire_seconds is None:
        expire_seconds = settings.NLP_CACHE_EXPIRE
    key = f"nlp:result:{nlp_id}"
    await cache_set(key, result, expire_seconds)


async def get_cached_nlp_result(nlp_id: str) -> Optional[dict]:
    """获取缓存的 NLP 结果"""
    key = f"nlp:result:{nlp_id}"
    return await cache_get(key)
