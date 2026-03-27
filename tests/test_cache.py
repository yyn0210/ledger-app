"""
AI 服务缓存层单元测试
测试 Redis 缓存、缓存策略、缓存失效
"""
import pytest
import time
import json
from unittest.mock import AsyncMock, patch, MagicMock

from app.services.cache import (
    CacheService, CacheEntry, CacheStats, CacheStrategy,
    get_cache_service, cache_result
)


class TestCacheEntry:
    """缓存条目测试"""
    
    def test_create_entry(self):
        """测试创建缓存条目"""
        entry = CacheEntry(
            key="test:key",
            value={"result": "test"},
            created_at=time.time(),
            ttl=3600,
            task_type="nlp"
        )
        
        assert entry.key == "test:key"
        assert entry.value == {"result": "test"}
        assert entry.ttl == 3600
        assert entry.hit_count == 0
    
    def test_is_expired(self):
        """测试过期检查"""
        # 未过期
        entry = CacheEntry(
            key="test:key",
            value={"result": "test"},
            created_at=time.time(),
            ttl=3600
        )
        assert entry.is_expired() is False
        
        # 已过期
        entry.created_at = time.time() - 7200  # 2 小时前
        assert entry.is_expired() is True
    
    def test_to_dict(self):
        """测试转换为字典"""
        entry = CacheEntry(
            key="test:key",
            value={"result": "test"},
            created_at=1000.0,
            ttl=3600,
            hit_count=5,
            task_type="ocr",
            model_used="qwen-vl-max",
            cost_saved=0.02
        )
        
        data = entry.to_dict()
        assert data["key"] == "test:key"
        assert data["value"] == {"result": "test"}
        assert data["created_at"] == 1000.0
        assert data["ttl"] == 3600
        assert data["hit_count"] == 5
        assert data["task_type"] == "ocr"
        assert data["model_used"] == "qwen-vl-max"
        assert data["cost_saved"] == 0.02
    
    def test_from_dict(self):
        """测试从字典创建"""
        data = {
            "key": "test:key",
            "value": {"result": "test"},
            "created_at": 1000.0,
            "ttl": 3600,
            "hit_count": 5,
            "task_type": "ocr",
            "model_used": "qwen-vl-max",
            "cost_saved": 0.02,
        }
        
        entry = CacheEntry.from_dict(data)
        assert entry.key == "test:key"
        assert entry.value == {"result": "test"}
        assert entry.hit_count == 5


class TestCacheStats:
    """缓存统计测试"""
    
    def test_create_stats(self):
        """测试创建统计"""
        stats = CacheStats(task_type="nlp")
        assert stats.task_type == "nlp"
        assert stats.hits == 0
        assert stats.misses == 0
    
    def test_hit_rate(self):
        """测试命中率计算"""
        stats = CacheStats(task_type="nlp")
        stats.hits = 80
        stats.misses = 20
        
        assert stats.hit_rate == 0.8
    
    def test_hit_rate_no_requests(self):
        """测试无请求时的命中率"""
        stats = CacheStats(task_type="nlp")
        assert stats.hit_rate == 0.0
    
    def test_total_requests(self):
        """测试总请求数"""
        stats = CacheStats(task_type="nlp")
        stats.hits = 80
        stats.misses = 20
        
        assert stats.total_requests == 100
    
    def test_to_dict(self):
        """测试转换为字典"""
        stats = CacheStats(task_type="ocr")
        stats.hits = 90
        stats.misses = 10
        stats.sets = 100
        stats.total_cost_saved = 1.5
        
        data = stats.to_dict()
        assert data["task_type"] == "ocr"
        assert data["hits"] == 90
        assert data["misses"] == 10
        assert data["hit_rate"] == "90.00%"
        assert data["total_requests"] == 100


class TestCacheService:
    """缓存服务测试"""
    
    @pytest.fixture
    def mock_redis(self):
        """模拟 Redis"""
        redis_mock = AsyncMock()
        redis_mock.get = AsyncMock(return_value=None)
        redis_mock.setex = AsyncMock()
        redis_mock.delete = AsyncMock()
        redis_mock.scan_iter = MagicMock(return_value=AsyncMock())
        return redis_mock
    
    @pytest.fixture
    def cache_service(self, mock_redis):
        """创建缓存服务"""
        return CacheService(redis_pool=mock_redis)
    
    def test_generate_key(self, cache_service):
        """测试缓存键生成"""
        key = cache_service._generate_key(
            task_type="ocr",
            input_data={"image_url": "http://example.com/image.jpg"},
            model_name="qwen-vl-max"
        )
        
        assert key.startswith("ledger:ai:ocr:")
        assert "qwen-vl-max" in key or "qwen_vl_max" in key
        assert len(key) > 20
    
    def test_generate_key_consistent(self, cache_service):
        """测试缓存键一致性"""
        input_data = {"text": "hello world"}
        
        key1 = cache_service._generate_key("nlp", input_data)
        key2 = cache_service._generate_key("nlp", input_data)
        
        assert key1 == key2
    
    @pytest.mark.asyncio
    async def test_cache_miss(self, cache_service, mock_redis):
        """测试缓存未命中"""
        mock_redis.get.return_value = None
        
        entry = await cache_service.get("test:key", "nlp")
        
        assert entry is None
        assert cache_service.stats["nlp"].misses == 1
    
    @pytest.mark.asyncio
    async def test_cache_hit(self, cache_service, mock_redis):
        """测试缓存命中"""
        entry_data = {
            "key": "test:key",
            "value": {"result": "cached"},
            "created_at": time.time(),
            "ttl": 3600,
            "hit_count": 0,
            "task_type": "nlp",
            "model_used": "qwen-turbo",
            "cost_saved": 0.0,
        }
        mock_redis.get.return_value = json.dumps(entry_data)
        
        entry = await cache_service.get("test:key", "nlp")
        
        assert entry is not None
        assert entry.value == {"result": "cached"}
        assert cache_service.stats["nlp"].hits == 1
    
    @pytest.mark.asyncio
    async def test_cache_expired(self, cache_service, mock_redis):
        """测试缓存过期"""
        entry_data = {
            "key": "test:key",
            "value": {"result": "cached"},
            "created_at": time.time() - 7200,  # 2 小时前
            "ttl": 3600,
            "hit_count": 0,
            "task_type": "nlp",
            "model_used": "qwen-turbo",
            "cost_saved": 0.0,
        }
        mock_redis.get.return_value = json.dumps(entry_data)
        
        entry = await cache_service.get("test:key", "nlp")
        
        assert entry is None  # 过期返回 None
        assert cache_service.stats["nlp"].misses == 1
    
    @pytest.mark.asyncio
    async def test_cache_set(self, cache_service, mock_redis):
        """测试设置缓存"""
        success = await cache_service.set(
            key="test:key",
            value={"result": "new"},
            task_type="ocr",
            ttl=7200,
            model_used="qwen-vl-max",
            cost_saved=0.02
        )
        
        assert success is True
        assert mock_redis.setex.called
        assert cache_service.stats["ocr"].sets == 1
    
    @pytest.mark.asyncio
    async def test_cache_delete(self, cache_service, mock_redis):
        """测试删除缓存"""
        success = await cache_service.delete("test:key", "nlp")
        
        assert success is True
        assert mock_redis.delete.called
        assert cache_service.stats["nlp"].deletes == 1
    
    @pytest.mark.asyncio
    async def test_invalidate_pattern(self, cache_service, mock_redis):
        """测试批量删除"""
        # 模拟 scan_iter
        async def async_iter():
            yield "key1"
            yield "key2"
            yield "key3"
        
        mock_redis.scan_iter.return_value = async_iter()
        
        count = await cache_service.invalidate_pattern("ledger:ai:ocr:*", "ocr")
        
        assert count == 3
    
    @pytest.mark.asyncio
    async def test_invalidate_all(self, cache_service, mock_redis):
        """测试删除所有缓存"""
        async def async_iter():
            yield "key1"
            yield "key2"
        
        mock_redis.scan_iter.return_value = async_iter()
        
        count = await cache_service.invalidate_all("nlp")
        
        assert count == 2
    
    def test_get_stats(self, cache_service):
        """测试获取统计"""
        cache_service.stats["nlp"].hits = 80
        cache_service.stats["nlp"].misses = 20
        
        stats = cache_service.get_stats("nlp")
        
        assert stats["task_type"] == "nlp"
        assert stats["hits"] == 80
        assert stats["misses"] == 20
        assert stats["hit_rate"] == "80.00%"
    
    def test_get_summary(self, cache_service):
        """测试获取摘要"""
        cache_service.stats["nlp"].hits = 80
        cache_service.stats["nlp"].misses = 20
        cache_service.stats["ocr"].hits = 90
        cache_service.stats["ocr"].misses = 10
        
        summary = cache_service.get_summary()
        
        assert summary["total_requests"] == 200
        assert summary["total_hits"] == 170
        assert summary["overall_hit_rate"] == "85.00%"
        assert "by_task_type" in summary
    
    @pytest.mark.asyncio
    async def test_cache_disabled(self, cache_service):
        """测试缓存禁用"""
        with patch('app.services.cache.settings') as mock_settings:
            mock_settings.CACHE_ENABLED = False
            
            entry = await cache_service.get("test:key", "nlp")
            assert entry is None
            
            success = await cache_service.set("test:key", {"result": "test"}, "nlp")
            assert success is False


class TestCacheServiceNoRedis:
    """无 Redis 连接时的缓存服务测试"""
    
    @pytest.fixture
    def cache_service(self):
        """创建无 Redis 连接的缓存服务"""
        return CacheService(redis_pool=None)
    
    @pytest.mark.asyncio
    async def test_get_without_redis(self, cache_service):
        """测试无 Redis 时获取缓存"""
        entry = await cache_service.get("test:key", "nlp")
        assert entry is None
        assert cache_service.stats["nlp"].misses == 1
    
    @pytest.mark.asyncio
    async def test_set_without_redis(self, cache_service):
        """测试无 Redis 时设置缓存"""
        success = await cache_service.set("test:key", {"result": "test"}, "nlp")
        assert success is False
        assert cache_service.stats["nlp"].sets == 1
    
    @pytest.mark.asyncio
    async def test_delete_without_redis(self, cache_service):
        """测试无 Redis 时删除缓存"""
        success = await cache_service.delete("test:key", "nlp")
        assert success is False


class TestCacheSingleton:
    """缓存服务单例测试"""
    
    def test_get_cache_service_returns_same_instance(self):
        """测试获取缓存服务返回相同实例"""
        cache1 = get_cache_service()
        cache2 = get_cache_service()
        assert cache1 is cache2


class TestCacheKeyGeneration:
    """缓存键生成测试"""
    
    @pytest.fixture
    def cache_service(self):
        return CacheService(redis_pool=None)
    
    def test_key_format(self, cache_service):
        """测试键格式"""
        key = cache_service._generate_key(
            task_type="ocr",
            input_data={"url": "test.jpg"}
        )
        
        assert key.startswith("ledger:ai:ocr:")
    
    def test_key_different_task_types(self, cache_service):
        """测试不同任务类型的键"""
        ocr_key = cache_service._generate_key("ocr", {"data": "test"})
        stt_key = cache_service._generate_key("stt", {"data": "test"})
        nlp_key = cache_service._generate_key("nlp", {"data": "test"})
        
        assert "ocr" in ocr_key
        assert "stt" in stt_key
        assert "nlp" in nlp_key
        assert ocr_key != stt_key
        assert stt_key != nlp_key
    
    def test_key_same_input_same_output(self, cache_service):
        """测试相同输入生成相同键"""
        input_data = {"text": "hello", "lang": "zh"}
        
        key1 = cache_service._generate_key("nlp", input_data)
        key2 = cache_service._generate_key("nlp", input_data)
        
        assert key1 == key2
    
    def test_key_different_input_different_output(self, cache_service):
        """测试不同输入生成不同键"""
        key1 = cache_service._generate_key("nlp", {"text": "hello"})
        key2 = cache_service._generate_key("nlp", {"text": "world"})
        
        assert key1 != key2


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
