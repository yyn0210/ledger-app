"""
OCR 数据模型
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
from enum import Enum


class OCRType(str, Enum):
    RECEIPT = "receipt"
    SCREENSHOT = "screenshot"


class OCRStatus(str, Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class OCRItem(BaseModel):
    """商品明细项"""
    name: str = Field(..., description="商品名称")
    quantity: int = Field(default=1, description="数量")
    price: float = Field(..., description="单价")
    amount: float = Field(..., description="金额")


class OCRReceiptResult(BaseModel):
    """小票 OCR 识别结果"""
    merchant: str = Field(..., description="商户名称")
    transaction_date: str = Field(..., pattern=r'^\d{4}-\d{2}-\d{2}$', description="交易日期")
    transaction_time: str = Field(..., pattern=r'^\d{2}:\d{2}:\d{2}$', description="交易时间")
    total_amount: float = Field(..., gt=0, description="总金额")
    items: List[OCRItem] = Field(default_factory=list, description="商品明细")
    confidence: float = Field(default=1.0, ge=0, le=1, description="置信度")
    raw_text: str = Field(default="", description="原始识别文本")


class ScreenshotSourceType(str, Enum):
    """截图来源类型"""
    ALIPAY = "alipay"
    WECHAT = "wechat"
    TAOMAO = "taobao"
    JD = "jd"
    PDD = "pdd"
    MEITUAN = "meituan"
    BANK_TRANSFER = "bank"
    UNKNOWN = "unknown"


class OCRScreenshotResult(BaseModel):
    """截图 OCR 识别结果"""
    source_type: ScreenshotSourceType = Field(..., description="截图来源类型")
    transaction_date: str = Field(..., pattern=r'^\d{4}-\d{2}-\d{2}$', description="交易日期")
    transaction_time: str = Field(..., pattern=r'^\d{2}:\d{2}:\d{2}$', description="交易时间")
    amount: float = Field(..., gt=0, description="交易金额")
    counterparty: Optional[str] = Field(None, description="交易对方")
    description: Optional[str] = Field(None, description="交易描述")
    confidence: float = Field(default=1.0, ge=0, le=1, description="置信度")
    raw_text: str = Field(default="", description="原始识别文本")


class OCRRecord(BaseModel):
    """OCR 识别记录"""
    ocr_id: str
    user_id: int
    book_id: Optional[int] = None
    ocr_type: OCRType
    status: OCRStatus
    image_url: str
    result: Optional[dict] = None
    error_message: Optional[str] = None
    confidence: Optional[float] = None
    created_at: datetime
    completed_at: Optional[datetime] = None


# API 请求/响应模型
class OCRReceiptResponse(BaseModel):
    """小票 OCR 响应"""
    ocr_id: str
    merchant: str
    total_amount: float
    transaction_date: str
    transaction_time: str
    items: List[OCRItem]
    confidence: float
    raw_text: str


class OCRScreenshotResponse(BaseModel):
    """截图 OCR 响应"""
    ocr_id: str
    type: str = "screenshot"
    source_type: str
    transaction_date: str
    transaction_time: str
    amount: float
    counterparty: Optional[str]
    description: Optional[str]
    confidence: float
    raw_text: str


class OCRHistoryItem(BaseModel):
    """OCR 历史记录项"""
    ocr_id: str
    ocr_type: str
    status: str
    amount: Optional[float] = None
    merchant: Optional[str] = None
    source_type: Optional[str] = None
    created_at: datetime


class OCRHistoryResponse(BaseModel):
    """OCR 历史记录响应"""
    list: List[OCRHistoryItem]
    total: int
    page: int
    size: int
