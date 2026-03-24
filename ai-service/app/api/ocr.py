"""
OCR API 路由
"""
import uuid
import json
from fastapi import APIRouter, HTTPException, UploadFile, File, Form, Query
from fastapi.responses import JSONResponse
from typing import Optional, List
from datetime import datetime
from loguru import logger

from app.services.qwen import qwen_service
from app.services.deepseek import deepseek_service
from app.utils.redis import redis_client
from app.utils.minio import minio_client
from app.enums.screenshot import ScreenshotSourceType, detect_screenshot_source, SOURCE_KEYWORDS
from app.models.ocr import (
    OCRStatus, OCRType, OCRReceiptResult, OCRItem, OCRScreenshotResult,
    OCRResult, OCRHistoryItem, OCRHistoryResponse
)

router = APIRouter(prefix="/api/ocr", tags=["OCR 识别"])


@router.post("/receipt")
async def ocr_receipt(
    image: UploadFile = File(..., description="小票图片文件（支持 jpg/png/webp）"),
    book_id: Optional[int] = Form(None, description="账本 ID（可选）")
):
    """
    小票 OCR 识别
    
    - **image**: 小票图片文件（必填，支持 jpg/png/webp）
    - **book_id**: 账本 ID（可选，用于后续自动创建交易）
    
    返回提取的商户、金额、日期、商品明细等信息。
    """
    # 验证图片格式
    allowed_formats = ["image/jpeg", "image/png", "image/webp"]
    if image.content_type not in allowed_formats:
        raise HTTPException(status_code=400, detail=f"不支持的图片格式。支持：{', '.join(allowed_formats)}")
    
    # 验证图片大小（最大 10MB）
    MAX_SIZE = 10 * 1024 * 1024
    content = await image.read()
    if len(content) > MAX_SIZE:
        raise HTTPException(status_code=400, detail="图片大小不能超过 10MB")
    
    ocr_id = str(uuid.uuid4())
    user_id = 1  # TODO: 从 token 获取用户 ID
    
    try:
        # 上传图片到 MinIO
        file_extension = image.filename.split(".")[-1] if image.filename else "jpg"
        image_url = minio_client.upload_image(content, user_id, file_extension)
        public_url = f"http://localhost:9000/{image_url}"
        
        # 调用 OCR 服务
        ocr_result = qwen_service.ocr_receipt(public_url)
        
        if not ocr_result or not ocr_result.get("success"):
            result = OCRResult(
                ocr_id=ocr_id,
                type=OCRType.RECEIPT,
                status=OCRStatus.FAILED,
                image_url=public_url,
                error_message=ocr_result.get("error", "OCR 识别失败") if ocr_result else "OCR 识别失败",
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
        else:
            receipt_result = OCRReceiptResult(
                merchant=ocr_result.get("merchant", ""),
                transaction_date=ocr_result.get("transaction_date", ""),
                transaction_time=ocr_result.get("transaction_time"),
                total_amount=ocr_result.get("total_amount", 0),
                items=[OCRItem(**item) for item in ocr_result.get("items", [])],
                confidence=ocr_result.get("confidence", 1.0),
                raw_text=ocr_result.get("raw_text", "")
            )
            
            result = OCRResult(
                ocr_id=ocr_id,
                type=OCRType.RECEIPT,
                status=OCRStatus.COMPLETED,
                image_url=public_url,
                result=receipt_result,
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
            
            # 缓存结果 24 小时
            cache_key = f"ocr:result:{ocr_id}"
            redis_client.set(cache_key, result.dict(), ttl=86400)
        
        # 保存到历史记录
        history_key = f"ocr:history:{user_id}"
        history_item = {
            "ocr_id": ocr_id,
            "type": "receipt",
            "merchant": ocr_result.get("merchant") if ocr_result else None,
            "amount": ocr_result.get("total_amount") if ocr_result else None,
            "status": result.status.value,
            "created_at": result.created_at.isoformat()
        }
        redis_client.client.lpush(history_key, json.dumps(history_item))
        redis_client.client.ltrim(history_key, 0, 99)
        
        return JSONResponse(content={"code": 200, "data": result.dict()})
        
    except Exception as e:
        logger.error(f"OCR receipt error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/results/{ocr_id}")
async def get_ocr_result(ocr_id: str):
    """查询 OCR 识别结果"""
    cache_key = f"ocr:result:{ocr_id}"
    cached = redis_client.get(cache_key)
    
    if cached:
        return JSONResponse(content={"code": 200, "data": cached})
    
    raise HTTPException(status_code=404, detail="OCR 结果不存在或已过期")


@router.get("/history")
async def get_ocr_history(
    type: Optional[str] = Query(None, description="识别类型（receipt/screenshot）"),
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(20, ge=1, le=50, description="每页数量")
):
    """获取 OCR 识别历史记录"""
    user_id = 1  # TODO: 从 token 获取用户 ID
    history_key = f"ocr:history:{user_id}"
    
    history_data = redis_client.client.lrange(history_key, 0, -1)
    
    if type:
        history_data = [item for item in history_data if json.loads(item).get("type") == type]
    
    total = len(history_data)
    start = (page - 1) * size
    end = start + size
    page_data = history_data[start:end]
    
    history_list = [OCRHistoryItem(**json.loads(item)) for item in page_data]
    
    return JSONResponse(content={
        "code": 200,
        "data": {
            "list": [item.dict() for item in history_list],
            "total": total,
            "page": page,
            "size": size
        }
    })


@router.post("/screenshot")
async def ocr_screenshot(
    image: UploadFile = File(..., description="截图文件（支持 jpg/png/webp）"),
    book_id: Optional[int] = Form(None, description="账本 ID（可选）")
):
    """
    截图 OCR 识别
    
    - **image**: 截图文件（必填，支持 jpg/png/webp）
    - **book_id**: 账本 ID（可选，用于后续自动创建交易）
    
    自动识别截图来源（支付宝/微信/淘宝等），提取交易信息。
    """
    # 验证图片格式
    allowed_formats = ["image/jpeg", "image/png", "image/webp"]
    if image.content_type not in allowed_formats:
        raise HTTPException(status_code=400, detail=f"不支持的图片格式。支持：{', '.join(allowed_formats)}")
    
    # 验证图片大小（最大 10MB）
    MAX_SIZE = 10 * 1024 * 1024
    content = await image.read()
    if len(content) > MAX_SIZE:
        raise HTTPException(status_code=400, detail="图片大小不能超过 10MB")
    
    ocr_id = str(uuid.uuid4())
    user_id = 1  # TODO: 从 token 获取用户 ID
    
    try:
        # 上传图片到 MinIO
        file_extension = image.filename.split(".")[-1] if image.filename else "png"
        image_url = minio_client.upload_image(content, user_id, file_extension)
        public_url = f"http://localhost:9000/{image_url}"
        
        # 调用 OCR 服务
        ocr_result = qwen_service.ocr_screenshot(public_url)
        
        if not ocr_result or not ocr_result.get("success"):
            result = OCRResult(
                ocr_id=ocr_id,
                type=OCRType.SCREENSHOT,
                status=OCRStatus.FAILED,
                image_url=public_url,
                error_message=ocr_result.get("error", "截图识别失败") if ocr_result else "截图识别失败",
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
        else:
            # 自动检测截图来源
            raw_text = ocr_result.get("raw_text", "")
            source_type = ocr_result.get("source_type", "unknown")
            
            # 如果 OCR 未返回 source_type，从 raw_text 检测
            if not source_type or source_type == "unknown":
                detected = detect_screenshot_source(raw_text)
                source_type = detected.value
            
            # 获取显示名称
            source_name = ScreenshotSourceType.get_display_name(source_type)
            
            screenshot_result = OCRScreenshotResult(
                source_type=source_type,
                source_name=source_name,
                transaction_date=ocr_result.get("transaction_date", ""),
                transaction_time=ocr_result.get("transaction_time"),
                amount=ocr_result.get("amount", 0),
                counterparty=ocr_result.get("counterparty"),
                description=ocr_result.get("description"),
                confidence=ocr_result.get("confidence", 1.0),
                raw_text=raw_text
            )
            
            result = OCRResult(
                ocr_id=ocr_id,
                type=OCRType.SCREENSHOT,
                status=OCRStatus.COMPLETED,
                image_url=public_url,
                result=screenshot_result,
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
            
            # 缓存结果 24 小时
            cache_key = f"ocr:result:{ocr_id}"
            redis_client.set(cache_key, result.dict(), ttl=86400)
        
        # 保存到历史记录
        history_key = f"ocr:history:{user_id}"
        history_item = {
            "ocr_id": ocr_id,
            "type": "screenshot",
            "source_type": ocr_result.get("source_type") if ocr_result else None,
            "amount": ocr_result.get("amount") if ocr_result else None,
            "status": result.status.value,
            "created_at": result.created_at.isoformat()
        }
        redis_client.client.lpush(history_key, json.dumps(history_item))
        redis_client.client.ltrim(history_key, 0, 99)
        
        return JSONResponse(content={"code": 200, "data": result.dict()})
        
    except Exception as e:
        logger.error(f"OCR screenshot error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
