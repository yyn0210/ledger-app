"""
NLP 数据模型
"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from enum import Enum


class ConsumptionIntent(str, Enum):
    """消费意图类型"""
    NECESSARY = "necessary"      # 必要消费
    OPTIONAL = "optional"        # 可选消费
    IMPULSE = "impulse"          # 冲动消费
    INVESTMENT = "investment"    # 投资性消费
    UNKNOWN = "unknown"          # 未知


class NLPParseRequest(BaseModel):
    """NLP 解析请求"""
    text: str = Field(..., description="自然语言描述（如'今天打车花了 50 块'）")
    book_id: Optional[int] = Field(None, description="账本 ID（可选）")


class NLPExtractedInfo(BaseModel):
    """NLP 提取的信息"""
    amount: Optional[float] = Field(None, description="金额")
    category: Optional[str] = Field(None, description="推荐分类")
    category_id: Optional[int] = Field(None, description="分类 ID")
    merchant: Optional[str] = Field(None, description="商户")
    account: Optional[str] = Field(None, description="推荐账户")
    date: Optional[str] = Field(None, description="日期")
    note: Optional[str] = Field(None, description="备注")
    intent: Optional[str] = Field(None, description="消费意图")
    confidence: float = Field(default=0.9, ge=0, le=1, description="置信度")


class NLPAnalyzeRequest(BaseModel):
    """NLP 分析请求"""
    text: str = Field(..., description="自然语言描述")
    history_transactions: Optional[List[dict]] = Field(None, description="历史交易记录（用于个性化推荐）")


class NLPAnalyzeResponse(BaseModel):
    """NLP 分析响应"""
    text: str = Field(..., description="原始文本")
    extracted: NLPExtractedInfo = Field(..., description="提取的信息")
    intent_analysis: Optional[dict] = Field(None, description="意图分析")
    suggestions: Optional[List[str]] = Field(None, description="建议")
