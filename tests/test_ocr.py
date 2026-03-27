"""
OCR API 单元测试
"""
import pytest
import json
from unittest.mock import Mock, patch, MagicMock
from datetime import datetime

from app.models.ocr import (
    OCRStatus, OCRType, OCRReceiptResult, OCRItem, 
    OCRScreenshotResult, OCRResult, OCRHistoryItem
)
from app.enums.screenshot import ScreenshotSourceType, detect_screenshot_source, SOURCE_KEYWORDS


class TestOCRReceiptResult:
    """小票 OCR 结果测试"""
    
    def test_valid_receipt_result(self):
        """测试有效的小票 OCR 结果"""
        result = OCRReceiptResult(
            merchant="XX 超市",
            transaction_date="2026-03-24",
            transaction_time="14:35:00",
            total_amount=158.50,
            items=[
                OCRItem(name="可口可乐", quantity=2, price=3.50, amount=7.00),
                OCRItem(name="薯片", quantity=1, price=8.50, amount=8.50)
            ],
            confidence=0.95,
            raw_text="XX 超市\n2026-03-24 14:35:00\n..."
        )
        
        assert result.merchant == "XX 超市"
        assert result.total_amount == 158.50
        assert len(result.items) == 2
        assert result.confidence == 0.95
    
    def test_receipt_result_without_time(self):
        """测试没有时间的小票结果"""
        result = OCRReceiptResult(
            merchant="XX 超市",
            transaction_date="2026-03-24",
            total_amount=158.50,
            items=[]
        )
        
        assert result.transaction_time is None
        assert len(result.items) == 0
    
    def test_invalid_date_format(self):
        """测试无效日期格式"""
        with pytest.raises(Exception):
            OCRReceiptResult(
                merchant="XX 超市",
                transaction_date="2026/03/24",  # 错误格式
                total_amount=158.50
            )
    
    def test_negative_amount(self):
        """测试负金额（应该失败）"""
        with pytest.raises(Exception):
            OCRReceiptResult(
                merchant="XX 超市",
                transaction_date="2026-03-24",
                total_amount=-100.00
            )


class TestOCRScreenshotResult:
    """截图 OCR 结果测试"""
    
    def test_valid_screenshot_result_alipay(self):
        """测试支付宝截图结果"""
        result = OCRScreenshotResult(
            source_type="alipay",
            source_name="支付宝",
            transaction_date="2026-03-24",
            transaction_time="14:35:00",
            amount=158.50,
            counterparty="XX 商户",
            description="购物消费",
            confidence=0.92,
            raw_text="支付宝账单\n2026-03-24 14:35:00\n..."
        )
        
        assert result.source_type == "alipay"
        assert result.source_name == "支付宝"
        assert result.amount == 158.50
    
    def test_screenshot_result_without_counterparty(self):
        """测试没有交易对方的截图结果"""
        result = OCRScreenshotResult(
            source_type="wechat",
            transaction_date="2026-03-24",
            amount=100.00,
            counterparty=None,
            description=None
        )
        
        assert result.counterparty is None
        assert result.description is None


class TestScreenshotSourceType:
    """截图来源类型枚举测试"""
    
    def test_get_display_name_alipay(self):
        """测试支付宝显示名称"""
        assert ScreenshotSourceType.get_display_name("alipay") == "支付宝"
    
    def test_get_display_name_wechat(self):
        """测试微信显示名称"""
        assert ScreenshotSourceType.get_display_name("wechat") == "微信"
    
    def test_get_display_name_unknown(self):
        """测试未知来源显示名称"""
        assert ScreenshotSourceType.get_display_name("unknown") == "未知"
    
    def test_get_display_name_invalid(self):
        """测试无效来源显示名称"""
        assert ScreenshotSourceType.get_display_name("invalid") == "未知"


class TestDetectScreenshotSource:
    """截图来源检测测试"""
    
    def test_detect_alipay(self):
        """测试支付宝检测"""
        text = "支付宝账单 2026-03-24 购物消费 ¥158.50"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.ALIPAY
    
    def test_detect_wechat(self):
        """测试微信检测"""
        text = "微信支付 2026-03-24 转账给 XX 商户"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.WECHAT
    
    def test_detect_taobao(self):
        """测试淘宝检测"""
        text = "淘宝订单 2026-03-24 待收货"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.TAOMAO
    
    def test_detect_jd(self):
        """测试京东检测"""
        text = "京东订单 2026-03-24 已完成"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.JD
    
    def test_detect_pdd(self):
        """测试拼多多检测"""
        text = "拼多多订单 2026-03-24 待评价"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.PDD
    
    def test_detect_meituan(self):
        """测试美团检测"""
        text = "美团外卖 2026-03-24 订单已完成"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.MEITUAN
    
    def test_detect_bank_transfer(self):
        """测试银行转账检测"""
        text = "银行转账 2026-03-24 转入 5000 元"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.BANK_TRANSFER
    
    def test_detect_unknown(self):
        """测试未知来源检测"""
        text = "这是一张普通的图片，没有任何账单信息"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.UNKNOWN
    
    def test_detect_empty_text(self):
        """测试空文本"""
        result = detect_screenshot_source("")
        assert result == ScreenshotSourceType.UNKNOWN
    
    def test_detect_case_insensitive(self):
        """测试大小写不敏感"""
        text = "ALIPAY 账单"
        result = detect_screenshot_source(text)
        assert result == ScreenshotSourceType.ALIPAY


class TestOCRResult:
    """OCR 结果通用模型测试"""
    
    def test_completed_receipt_result(self):
        """测试完成的小票 OCR 结果"""
        receipt_result = OCRReceiptResult(
            merchant="XX 超市",
            transaction_date="2026-03-24",
            total_amount=158.50,
            items=[]
        )
        
        result = OCRResult(
            ocr_id="test-uuid-123",
            type=OCRType.RECEIPT,
            status=OCRStatus.COMPLETED,
            image_url="http://localhost:9000/ledger-app/ocr/1/test.jpg",
            result=receipt_result
        )
        
        assert result.ocr_id == "test-uuid-123"
        assert result.type == OCRType.RECEIPT
        assert result.status == OCRStatus.COMPLETED
        assert result.result.merchant == "XX 超市"
    
    def test_failed_result(self):
        """测试失败的 OCR 结果"""
        result = OCRResult(
            ocr_id="test-uuid-456",
            type=OCRType.SCREENSHOT,
            status=OCRStatus.FAILED,
            image_url="http://localhost:9000/ledger-app/ocr/1/test.png",
            error_message="OCR 识别失败"
        )
        
        assert result.status == OCRStatus.FAILED
        assert result.error_message == "OCR 识别失败"
        assert result.result is None


class TestOCRHistoryItem:
    """OCR 历史记录项测试"""
    
    def test_history_item(self):
        """测试历史记录项"""
        item = OCRHistoryItem(
            ocr_id="test-uuid-789",
            type=OCRType.RECEIPT,
            merchant="XX 超市",
            amount=158.50,
            status=OCRStatus.COMPLETED,
            created_at=datetime.utcnow()
        )
        
        assert item.ocr_id == "test-uuid-789"
        assert item.merchant == "XX 超市"
        assert item.amount == 158.50


class TestOCRStatus:
    """OCR 状态枚举测试"""
    
    def test_status_values(self):
        """测试状态值"""
        assert OCRStatus.PENDING.value == "pending"
        assert OCRStatus.PROCESSING.value == "processing"
        assert OCRStatus.COMPLETED.value == "completed"
        assert OCRStatus.FAILED.value == "failed"


class TestOCRType:
    """OCR 类型枚举测试"""
    
    def test_type_values(self):
        """测试类型值"""
        assert OCRType.RECEIPT.value == "receipt"
        assert OCRType.SCREENSHOT.value == "screenshot"
