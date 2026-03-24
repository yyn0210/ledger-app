"""
STT 数据模型
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from enum import Enum


class STTStatus(str, Enum):
    """STT 任务状态"""
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"


class STTLanguage(str, Enum):
    """支持的语言"""
    ZH_CN = "zh_cn"      # 中文普通话
    ZH_TW = "zh_tw"      # 中文繁体
    EN_US = "en_us"      # 英语


class STTDialect(str, Enum):
    """支持的方言"""
    NONE = "none"           # 无
    CANTONESE = "cantonese" # 粤语
    SICHUAN = "sichuan"     # 四川话


class STTResult(BaseModel):
    """STT 识别结果"""
    stt_id: str = Field(..., description="STT 任务 ID")
    status: STTStatus = Field(..., description="任务状态")
    audio_url: str = Field(..., description="音频 URL")
    text: str = Field(default="", description="识别的文字")
    duration: float = Field(default=0, description="音频时长（秒）")
    confidence: float = Field(default=0, ge=0, le=1, description="置信度")
    language: str = Field(default="zh_cn", description="语言")
    dialect: Optional[str] = Field(None, description="方言")
    error_message: Optional[str] = Field(None, description="错误信息")
    created_at: datetime = Field(default_factory=datetime.utcnow, description="创建时间")
    completed_at: Optional[datetime] = Field(None, description="完成时间")


class STTHistoryItem(BaseModel):
    """STT 历史记录项"""
    stt_id: str = Field(..., description="STT 任务 ID")
    text: str = Field(..., description="识别的文字")
    duration: float = Field(..., description="音频时长")
    status: STTStatus = Field(..., description="任务状态")
    created_at: datetime = Field(..., description="创建时间")


class STTHistoryResponse(BaseModel):
    """STT 历史记录响应"""
    list: list[STTHistoryItem] = Field(..., description="历史记录列表")
    total: int = Field(..., description="总数")
    page: int = Field(..., description="当前页码")
    size: int = Field(..., description="每页数量")
