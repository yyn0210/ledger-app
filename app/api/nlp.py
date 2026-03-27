"""
语义理解 NLP API 接口
"""
import uuid
from datetime import datetime
from fastapi import APIRouter, HTTPException, Body
from loguru import logger
from app.utils.redis import cache_nlp_result, get_cached_nlp_result
from app.services.deepseek import parse_with_deepseek, classify_with_deepseek, NLPException

router = APIRouter()


@router.post("/parse", summary="语义理解解析")
async def nlp_parse(
    text: str = Body(..., description="自然语言文本，如'今天打车花了 50 块'"),
    book_id: int = Body(None, description="账本 ID（可选）")
):
    """
    语义理解解析
    
    使用 DeepSeek 解析自然语言，提取金额/分类/账户/备注等信息
    """
    try:
        nlp_id = str(uuid.uuid4())
        
        # 检查缓存
        cached_result = await get_cached_nlp_result(nlp_id)
        if cached_result:
            return {"code": 200, "data": cached_result}
        
        # 调用 DeepSeek 进行语义解析
        result = await parse_with_deepseek(text)
        
        # 缓存结果
        response_data = {
            "nlp_id": nlp_id,
            "text": text,
            "result": result,
            "created_at": datetime.now()
        }
        await cache_nlp_result(nlp_id, response_data)
        
        logger.info(f"NLP 解析成功：{nlp_id}")
        
        return {"code": 200, "data": response_data}
        
    except Exception as e:
        logger.error(f"NLP 解析异常：{e}")
        raise HTTPException(status_code=500, detail=f"NLP 解析失败：{str(e)}")


@router.post("/classify", summary="智能分类推荐")
async def classify_recommend(
    merchant: str = Body(None, description="商户名称"),
    note: str = Body(None, description="备注"),
    history: list = Body(None, description="历史交易记录")
):
    """
    智能分类推荐
    
    根据商户名/备注推荐分类，基于历史交易学习
    """
    try:
        result = await classify_with_deepseek(merchant, history)
        return {"code": 200, "data": result}
        
    except Exception as e:
        logger.error(f"分类推荐异常：{e}")
        raise HTTPException(status_code=500, detail=f"分类推荐失败：{str(e)}")
