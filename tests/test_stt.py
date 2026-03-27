"""
STT API 单元测试
"""
import pytest
import json
from unittest.mock import Mock, patch, MagicMock
from datetime import datetime

from app.models.stt import STTStatus, STTLanguage, STTDialect, STTResult, STTHistoryItem


class TestSTTResult:
    """STT 结果测试"""
    
    def test_valid_stt_result(self):
        """测试有效的 STT 结果"""
        result = STTResult(
            stt_id="test-uuid-123",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/ledger-app/stt/1/test.mp3",
            text="今天中午在公司附近的面馆吃了碗牛肉面，花了三十五块钱",
            duration=5.3,
            confidence=0.95,
            language="zh_cn",
            dialect=None
        )
        
        assert result.stt_id == "test-uuid-123"
        assert result.status == STTStatus.COMPLETED
        assert result.text == "今天中午在公司附近的面馆吃了碗牛肉面，花了三十五块钱"
        assert result.duration == 5.3
        assert result.confidence == 0.95
        assert result.language == "zh_cn"
    
    def test_stt_result_with_dialect(self):
        """测试带方言的 STT 结果"""
        result = STTResult(
            stt_id="test-uuid-456",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/ledger-app/stt/1/test.mp3",
            text="今日中午食咗碗牛肉面",
            duration=3.2,
            confidence=0.90,
            language="zh_cn",
            dialect="cantonese"
        )
        
        assert result.dialect == "cantonese"
        assert result.confidence == 0.90
    
    def test_stt_failed_result(self):
        """测试失败的 STT 结果"""
        result = STTResult(
            stt_id="test-uuid-789",
            status=STTStatus.FAILED,
            audio_url="http://localhost:9000/ledger-app/stt/1/test.mp3",
            text="",
            error_message="语音识别失败",
            language="zh_cn"
        )
        
        assert result.status == STTStatus.FAILED
        assert result.error_message == "语音识别失败"
        assert result.text == ""
    
    def test_stt_result_empty_text(self):
        """测试空文本的 STT 结果"""
        result = STTResult(
            stt_id="test-uuid-000",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/ledger-app/stt/1/test.mp3",
            text="",
            duration=2.0,
            confidence=0.5,
            language="zh_cn"
        )
        
        assert result.text == ""
        assert result.duration == 2.0


class TestSTTStatus:
    """STT 状态枚举测试"""
    
    def test_status_values(self):
        """测试状态值"""
        assert STTStatus.PENDING.value == "pending"
        assert STTStatus.PROCESSING.value == "processing"
        assert STTStatus.COMPLETED.value == "completed"
        assert STTStatus.FAILED.value == "failed"


class TestSTTLanguage:
    """STT 语言枚举测试"""
    
    def test_language_values(self):
        """测试语言值"""
        assert STTLanguage.ZH_CN.value == "zh_cn"
        assert STTLanguage.ZH_TW.value == "zh_tw"
        assert STTLanguage.EN_US.value == "en_us"


class TestSTTDialect:
    """STT 方言枚举测试"""
    
    def test_dialect_values(self):
        """测试方言值"""
        assert STTDialect.NONE.value == "none"
        assert STTDialect.CANTONESE.value == "cantonese"
        assert STTDialect.SICHUAN.value == "sichuan"


class TestSTTHistoryItem:
    """STT 历史记录项测试"""
    
    def test_history_item(self):
        """测试历史记录项"""
        item = STTHistoryItem(
            stt_id="test-uuid-history",
            text="今天中午吃了牛肉面",
            duration=5.3,
            status=STTStatus.COMPLETED,
            created_at=datetime.utcnow()
        )
        
        assert item.stt_id == "test-uuid-history"
        assert item.text == "今天中午吃了牛肉面"
        assert item.duration == 5.3
        assert item.status == STTStatus.COMPLETED


class TestSTTResultValidation:
    """STT 结果验证测试"""
    
    def test_confidence_range_valid(self):
        """测试置信度范围（有效）"""
        result = STTResult(
            stt_id="test-uuid-1",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="test",
            confidence=0.95,
            language="zh_cn"
        )
        assert result.confidence == 0.95
    
    def test_confidence_zero(self):
        """测试置信度为 0"""
        result = STTResult(
            stt_id="test-uuid-2",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="test",
            confidence=0.0,
            language="zh_cn"
        )
        assert result.confidence == 0.0
    
    def test_confidence_one(self):
        """测试置信度为 1"""
        result = STTResult(
            stt_id="test-uuid-3",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="test",
            confidence=1.0,
            language="zh_cn"
        )
        assert result.confidence == 1.0
    
    def test_duration_positive(self):
        """测试时长为正数"""
        result = STTResult(
            stt_id="test-uuid-4",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="test",
            duration=10.5,
            language="zh_cn"
        )
        assert result.duration == 10.5
    
    def test_duration_zero(self):
        """测试时长为 0"""
        result = STTResult(
            stt_id="test-uuid-5",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="test",
            duration=0.0,
            language="zh_cn"
        )
        assert result.duration == 0.0


class TestSTTResultDict:
    """STT 结果字典转换测试"""
    
    def test_to_dict(self):
        """测试转换为字典"""
        result = STTResult(
            stt_id="test-uuid-dict",
            status=STTStatus.COMPLETED,
            audio_url="http://localhost:9000/test.mp3",
            text="测试文本",
            duration=3.5,
            confidence=0.88,
            language="zh_cn",
            dialect="cantonese"
        )
        
        result_dict = result.dict()
        
        assert result_dict["stt_id"] == "test-uuid-dict"
        assert result_dict["status"] == "completed"
        assert result_dict["text"] == "测试文本"
        assert result_dict["duration"] == 3.5
        assert result_dict["confidence"] == 0.88
        assert result_dict["language"] == "zh_cn"
        assert result_dict["dialect"] == "cantonese"
    
    def test_to_dict_with_error(self):
        """测试带错误的字典转换"""
        result = STTResult(
            stt_id="test-uuid-error",
            status=STTStatus.FAILED,
            audio_url="http://localhost:9000/test.mp3",
            text="",
            error_message="音频格式不支持",
            language="zh_cn"
        )
        
        result_dict = result.dict()
        
        assert result_dict["status"] == "failed"
        assert result_dict["error_message"] == "音频格式不支持"
        assert result_dict["text"] == ""
