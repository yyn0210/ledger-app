"""
OCR 数据模型
"""
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
from enum import Enum


class OCRStatus(str, Enum):
    """OCR 任务状态"""
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class OCRType(str, Enum):
    """OCR 识别类型"""
    RECEIPT = "receipt"
    SCREENSHOT = "screenshot"


class OCRItem(BaseModel):
    """商品明细"""
    name: str = Field(..., description="商品名称")
    quantity: int = Field(default=1, description="数量")
    price: float = Field(..., description="单价")
    amount: float = Field(..., description="金额")


class OCRReceiptResult(BaseModel):
    """小票 OCR 识别结果"""
    merchant: str = Field(..., description="商户名称")
    transaction_date: str = Field(..., pattern=r'^\d{4}-\d{2}-\d{2}$', description="交易日期")
    transaction_time: Optional[str] = Field(None, pattern=r'^\d{2}:\d{2}:\d{2}$', description="交易时间")
    total_amount: float = Field(..., gt=0, description="总金额")
    items: List[OCRItem] = Field(default_factory=list, description="商品明细")
    confidence: float = Field(default=1.0, ge=0, le=1, description="置信度")
    raw_text: str = Field(default="", description="原始识别文本")


class OCRScreenshotResult(BaseModel):
    """截图 OCR 识别结果"""
    source_type: str = Field(..., description="截图来源类型（alipay/wechat/taobao 等）")
    source_name: Optional[str] = Field(None, description="来源显示名称")
    transaction_date: str = Field(..., pattern=r'^\d{4}-\d{2}-\d{2}$', description="交易日期")
    transaction_time: Optional[str] = Field(None, pattern=r'^\d{2}:\d{2}:\d{2}$', description="交易时间")
    amount: float = Field(..., description="交易金额")
    counterparty: Optional[str] = Field(None, description="交易对方")
    description: Optional[str] = Field(None, description="交易描述")
    confidence: float = Field(default=1.0, ge=0, le=1, description="置信度")
    raw_text: str = Field(default="", description="原始识别文本")


class OCRResult(BaseModel):
    """OCR 识别结果通用模型"""
    ocr_id: str = Field(..., description="OCR 任务 ID")
    type: OCRType = Field(..., description="识别类型")
    status: OCRStatus = Field(..., description="任务状态")
    image_url: str = Field(..., description="图片 URL")
    result: Optional[OCRReceiptResult] = Field(None, description="识别结果")
    error_message: Optional[str] = Field(None, description="错误信息")
    created_at: datetime = Field(default_factory=datetime.utcnow, description="创建时间")
    completed_at: Optional[datetime] = Field(None, description="完成时间")


class OCRHistoryItem(BaseModel):
    """OCR 历史记录项"""
    ocr_id: str = Field(..., description="OCR 任务 ID")
    type: OCRType = Field(..., description="识别类型")
    merchant: Optional[str] = Field(None, description="商户名称")
    amount: Optional[float] = Field(None, description="金额")
    status: OCRStatus = Field(..., description="任务状态")
    created_at: datetime = Field(..., description="创建时间")


class OCRHistoryResponse(BaseModel):
    """OCR 历史记录响应"""
    list: List[OCRHistoryItem] = Field(..., description="历史记录列表")
    total: int = Field(..., description="总数")
    page: int = Field(..., description="当前页码")
    size: int = Field(..., description="每页数量")


class OCRUploadRequest(BaseModel):
    """OCR 上传请求"""
    book_id: Optional[int] = Field(None, description="账本 ID")
