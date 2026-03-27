"""
推荐数据模型
"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


class RecommendRequest(BaseModel):
    """分类推荐请求"""
    merchant: Optional[str] = Field(None, description="商户名称")
    amount: Optional[float] = Field(None, description="金额")
    note: Optional[str] = Field(None, description="备注/描述")
    book_id: Optional[int] = Field(None, description="账本 ID")


class RecommendCategory(BaseModel):
    """推荐的分类"""
    category_id: int = Field(..., description="分类 ID")
    category_name: str = Field(..., description="分类名称")
    confidence: float = Field(..., ge=0, le=1, description="置信度")
    reason: str = Field(..., description="推荐理由")
    match_count: int = Field(default=0, description="匹配的历史交易数")


class RecommendResponse(BaseModel):
    """分类推荐响应"""
    merchant: Optional[str] = Field(None, description="商户名称")
    amount: Optional[float] = Field(None, description="金额")
    note: Optional[str] = Field(None, description="备注")
    recommendations: List[RecommendCategory] = Field(..., description="推荐分类列表")
    top_recommendation: Optional[RecommendCategory] = Field(None, description="最佳推荐")


class RecommendHistoryItem(BaseModel):
    """推荐历史记录项"""
    merchant: Optional[str]
    amount: Optional[float]
    recommended_category: str
    selected_category: Optional[str]
    created_at: datetime


class RecommendFeedbackRequest(BaseModel):
    """推荐反馈请求"""
    merchant: Optional[str]
    amount: Optional[float]
    recommended_category: str
    actual_category: str = Field(..., description="实际选择的分类")
