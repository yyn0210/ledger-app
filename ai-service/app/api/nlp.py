"""
NLP API 路由
"""
from fastapi import APIRouter, HTTPException, Query
from fastapi.responses import JSONResponse
from typing import Optional, List
from datetime import datetime
from loguru import logger

from app.services.deepseek import deepseek_service
from app.services.qwen import qwen_service
from app.core.prompts import prompt_manager
from app.utils.redis import redis_client
from app.models.nlp import (
    NLPParseRequest, NLPExtractedInfo, NLPAnalyzeRequest,
    NLPAnalyzeResponse, ConsumptionIntent
)

router = APIRouter(prefix="/api/nlp", tags=["语义理解"])


@router.post("/parse")
async def parse_transaction(request: NLPParseRequest):
    """
    交易语义解析
    
    - **text**: 自然语言描述（如'今天打车花了 50 块'）
    - **book_id**: 账本 ID（可选）
    
    从自然语言中提取金额、分类、商户等信息。
    """
    try:
        # 检查缓存
        cache_key = f"nlp:parse:{hash(request.text)}"
        cached = redis_client.get(cache_key)
        if cached:
            logger.info(f"NLP parse cache hit: {cache_key}")
            return JSONResponse(content={"code": 200, "data": cached})
        
        # 调用 DeepSeek 进行语义解析
        result = await deepseek_service.parse_transaction(request.text)
        
        if not result:
            raise HTTPException(status_code=500, detail="语义解析失败")
        
        # 构建响应
        extracted = NLPExtractedInfo(
            amount=result.get("amount"),
            category=result.get("category"),
            category_id=result.get("category_id"),
            merchant=result.get("merchant"),
            account=result.get("account"),
            date=result.get("date"),
            note=result.get("note"),
            intent=result.get("intent", "unknown"),
            confidence=result.get("confidence", 0.9)
        )
        
        response_data = extracted.dict()
        
        # 缓存结果 1 小时
        redis_client.set(cache_key, response_data, ttl=3600)
        
        return JSONResponse(content={"code": 200, "data": response_data})
        
    except Exception as e:
        logger.error(f"NLP parse error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/extract")
async def extract_info(text: str = Query(..., description="要提取信息的文本")):
    """
    关键信息提取
    
    - **text**: 要提取信息的文本
    
    从文本中提取商户、金额、日期等关键信息。
    """
    try:
        # 检查缓存
        cache_key = f"nlp:extract:{hash(text)}"
        cached = redis_client.get(cache_key)
        if cached:
            return JSONResponse(content={"code": 200, "data": cached})
        
        # 使用 DeepSeek 提取信息
        prompt = f"""请从以下文本中提取关键信息：

"{text}"

请提取：
1. 商户名称
2. 金额
3. 日期
4. 商品/服务描述

请以 JSON 格式返回。"""

        result = await deepseek_service.chat([
            {"role": "system", "content": "你是一个信息提取专家。"},
            {"role": "user", "content": prompt}
        ])
        
        if not result:
            raise HTTPException(status_code=500, detail="信息提取失败")
        
        import json
        try:
            result = result.replace("```json", "").replace("```", "").strip()
            extracted = json.loads(result)
        except:
            extracted = {"raw_result": result}
        
        # 缓存
        redis_client.set(cache_key, extracted, ttl=3600)
        
        return JSONResponse(content={"code": 200, "data": extracted})
        
    except Exception as e:
        logger.error(f"NLP extract error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/analyze")
async def analyze_text(request: NLPAnalyzeRequest):
    """
    语义分析
    
    - **text**: 自然语言描述
    - **history_transactions**: 历史交易记录（可选，用于个性化推荐）
    
    分析消费意图并提供建议。
    """
    try:
        # 解析交易
        parse_result = await deepseek_service.parse_transaction(request.text)
        
        if not parse_result:
            raise HTTPException(status_code=500, detail="语义分析失败")
        
        # 分析消费意图
        intent_analysis = None
        if parse_result.get("amount") and parse_result.get("category"):
            intent_analysis = await deepseek_service.analyze_consumption_intent(
                request.text,
                parse_result.get("amount", 0),
                parse_result.get("category", "")
            )
        
        # 构建响应
        extracted = NLPExtractedInfo(
            amount=parse_result.get("amount"),
            category=parse_result.get("category"),
            merchant=parse_result.get("merchant"),
            date=parse_result.get("date"),
            note=parse_result.get("note"),
            intent=parse_result.get("intent", "unknown"),
            confidence=parse_result.get("confidence", 0.9)
        )
        
        response = NLPAnalyzeResponse(
            text=request.text,
            extracted=extracted,
            intent_analysis=intent_analysis,
            suggestions=intent_analysis.get("suggestions") if intent_analysis else None
        )
        
        return JSONResponse(content={"code": 200, "data": response.dict()})
        
    except Exception as e:
        logger.error(f"NLP analyze error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/prompts")
async def list_prompts():
    """
    获取 Prompt 模板列表
    """
    templates = prompt_manager.list_templates()
    return JSONResponse(content={
        "code": 200,
        "data": {"templates": templates}
    })


@router.post("/prompts/{name}/render")
async def render_prompt(name: str, **kwargs):
    """
    渲染 Prompt 模板
    
    - **name**: 模板名称
    - **kwargs**: 模板参数
    """
    try:
        rendered = prompt_manager.render(name, **kwargs)
        return JSONResponse(content={
            "code": 200,
            "data": {"prompt": rendered}
        })
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
