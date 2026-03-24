"""
智能推荐 API 路由
"""
import json
from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
from typing import Optional, List
from datetime import datetime
from loguru import logger

from app.services.recommender import recommender_service
from app.services.deepseek import deepseek_service
from app.utils.redis import redis_client
from app.models.recommend import (
    RecommendRequest, RecommendResponse, RecommendCategory,
    RecommendHistoryItem, RecommendFeedbackRequest
)

router = APIRouter(prefix="/api/nlp", tags=["智能推荐"])


@router.post("/recommend")
async def recommend_category(request: RecommendRequest):
    """
    分类推荐
    
    - **merchant**: 商户名称（可选）
    - **amount**: 金额（可选）
    - **note**: 备注/描述（可选）
    - **book_id**: 账本 ID（可选）
    
    根据商户名/备注推荐分类，基于历史交易学习。
    """
    try:
        # 检查缓存
        cache_key = f"recommend:{hash(f'{request.merchant}{request.amount}{request.note}')}"
        cached = redis_client.get(cache_key)
        if cached:
            logger.info(f"Recommend cache hit: {cache_key}")
            return JSONResponse(content={"code": 200, "data": cached})
        
        # 获取历史交易记录（简化：从缓存获取，实际应从数据库查询）
        history_key = f"transactions:history:{request.book_id or 1}"
        history_data = redis_client.client.lrange(history_key, 0, 49)  # 最近 50 笔
        history = [json.loads(item) for item in history_data] if history_data else []
        
        # 调用推荐服务
        recommendations = recommender_service.get_recommendations(
            merchant=request.merchant,
            amount=request.amount,
            note=request.note,
            history=history
        )
        
        if not recommendations:
            raise HTTPException(status_code=500, detail="推荐失败")
        
        # 构建响应
        rec_categories = [
            RecommendCategory(
                category_id=i + 1,  # TODO: 实际应从分类表获取
                category_name=rec["category_name"],
                confidence=rec["confidence"],
                reason=rec["reason"],
                match_count=rec.get("match_count", 0)
            )
            for i, rec in enumerate(recommendations)
        ]
        
        response = RecommendResponse(
            merchant=request.merchant,
            amount=request.amount,
            note=request.note,
            recommendations=rec_categories,
            top_recommendation=rec_categories[0] if rec_categories else None
        )
        
        response_data = response.dict()
        
        # 缓存推荐结果 30 分钟
        redis_client.set(cache_key, response_data, ttl=1800)
        
        # 保存推荐历史
        history_item = {
            "merchant": request.merchant,
            "amount": request.amount,
            "recommended_category": rec_categories[0].category_name if rec_categories else None,
            "created_at": datetime.utcnow().isoformat()
        }
        redis_client.client.lpush(f"recommend:history:{request.book_id or 1}", json.dumps(history_item))
        redis_client.client.ltrim(f"recommend:history:{request.book_id or 1}", 0, 99)
        
        return JSONResponse(content={"code": 200, "data": response_data})
        
    except Exception as e:
        logger.error(f"Recommend error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommend/history")
async def get_recommend_history(
    book_id: Optional[int] = None,
    page: int = 1,
    size: int = 20
):
    """
    推荐历史记录
    
    - **book_id**: 账本 ID（可选）
    - **page**: 页码（默认 1）
    - **size**: 每页数量（默认 20）
    """
    history_key = f"recommend:history:{book_id or 1}"
    history_data = redis_client.client.lrange(history_key, 0, -1)
    
    total = len(history_data)
    start = (page - 1) * size
    end = start + size
    page_data = history_data[start:end]
    
    history_list = [json.loads(item) for item in page_data]
    
    return JSONResponse(content={
        "code": 200,
        "data": {
            "list": history_list,
            "total": total,
            "page": page,
            "size": size
        }
    })


@router.post("/recommend/feedback")
async def submit_feedback(request: RecommendFeedbackRequest):
    """
    推荐反馈
    
    - **merchant**: 商户名称
    - **amount**: 金额
    - **recommended_category**: 推荐的分类
    - **actual_category**: 实际选择的分类（用于优化模型）
    
    通过反馈优化推荐算法。
    """
    try:
        # 保存反馈
        feedback = {
            "merchant": request.merchant,
            "amount": request.amount,
            "recommended_category": request.recommended_category,
            "actual_category": request.actual_category,
            "created_at": datetime.utcnow().isoformat()
        }
        
        feedback_key = f"recommend:feedback:{request.merchant or 'unknown'}"
        redis_client.client.lpush(feedback_key, json.dumps(feedback))
        redis_client.client.ltrim(feedback_key, 0, 49)  # 保留最近 50 条反馈
        
        # 如果推荐和实际不一致，记录用于优化
        if request.recommended_category != request.actual_category:
            logger.info(
                f"Feedback mismatch: merchant={request.merchant}, "
                f"recommended={request.recommended_category}, actual={request.actual_category}"
            )
        
        return JSONResponse(content={
            "code": 200,
            "message": "反馈已提交，用于优化推荐算法"
        })
        
    except Exception as e:
        logger.error(f"Feedback error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
