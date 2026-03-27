"""
OCR 识别 API 接口
"""
import uuid
from datetime import datetime
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Query
from loguru import logger
from app.models.ocr import (
    OCRReceiptResponse, OCRScreenshotResponse, 
    OCRHistoryItem, OCRHistoryResponse, OCRType, OCRStatus
)
from app.services.qwen import recognize_receipt, recognize_screenshot, OCRException
from app.utils.minio import upload_image, init_bucket
from app.utils.redis import cache_ocr_result, get_cached_ocr_result
from app.core.config import settings

router = APIRouter()

# 模拟 OCR 记录存储（实际应该用数据库）
ocr_records = {}


@router.on_event("startup")
async def startup():
    """初始化 MinIO 存储桶"""
    init_bucket()


@router.post("/receipt", response_model=OCRReceiptResponse, summary="小票 OCR 识别")
async def ocr_receipt(
    image: UploadFile = File(..., description="小票图片文件"),
    book_id: int = Form(None, description="账本 ID（可选）")
):
    """
    小票 OCR 识别
    
    上传购物小票图片，自动提取商户、金额、日期、商品明细等信息
    """
    try:
        # 生成 OCR 记录 ID
        ocr_id = str(uuid.uuid4())
        user_id = 1  # TODO: 从 token 中获取用户 ID
        
        # 上传图片到 MinIO
        image_url = await upload_image(image, user_id, folder="ocr/receipt")
        
        # 检查缓存
        cached_result = await get_cached_ocr_result(ocr_id)
        if cached_result:
            logger.info(f"OCR 缓存命中：{ocr_id}")
            return OCRReceiptResponse(**cached_result)
        
        # 调用 Qwen-VL 进行 OCR 识别
        logger.info(f"开始小票 OCR 识别：{ocr_id}")
        result = await recognize_receipt(image_url)
        
        # 创建 OCR 记录
        record = {
            "ocr_id": ocr_id,
            "user_id": user_id,
            "book_id": book_id,
            "ocr_type": OCRType.RECEIPT.value,
            "status": OCRStatus.COMPLETED.value,
            "image_url": image_url,
            "result": result.model_dump(),
            "confidence": result.confidence,
            "created_at": datetime.now(),
            "completed_at": datetime.now()
        }
        ocr_records[ocr_id] = record
        
        # 缓存结果
        response_data = {
            "ocr_id": ocr_id,
            "merchant": result.merchant,
            "total_amount": result.total_amount,
            "transaction_date": result.transaction_date,
            "transaction_time": result.transaction_time,
            "items": [item.model_dump() for item in result.items],
            "confidence": result.confidence,
            "raw_text": result.raw_text
        }
        await cache_ocr_result(ocr_id, response_data)
        
        logger.info(f"小票 OCR 识别成功：{ocr_id}, 商户：{result.merchant}, 金额：{result.total_amount}")
        
        return OCRReceiptResponse(**response_data)
        
    except OCRException as e:
        logger.error(f"OCR 识别失败：{e}")
        raise HTTPException(status_code=400, detail=str(e))
    except ValueError as e:
        logger.error(f"参数验证失败：{e}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"OCR 识别异常：{e}")
        raise HTTPException(status_code=500, detail=f"OCR 识别失败：{str(e)}")


@router.post("/screenshot", response_model=OCRScreenshotResponse, summary="截图 OCR 识别")
async def ocr_screenshot(
    image: UploadFile = File(..., description="截图图片文件"),
    book_id: int = Form(None, description="账本 ID（可选）")
):
    """
    截图 OCR 识别
    
    上传记账相关截图（支付宝/微信账单、电商订单等），自动提取交易信息
    """
    try:
        # 生成 OCR 记录 ID
        ocr_id = str(uuid.uuid4())
        user_id = 1  # TODO: 从 token 中获取用户 ID
        
        # 上传图片到 MinIO
        image_url = await upload_image(image, user_id, folder="ocr/screenshot")
        
        # 检查缓存
        cached_result = await get_cached_ocr_result(ocr_id)
        if cached_result:
            logger.info(f"OCR 缓存命中：{ocr_id}")
            return OCRScreenshotResponse(**cached_result)
        
        # 调用 Qwen-VL 进行 OCR 识别
        logger.info(f"开始截图 OCR 识别：{ocr_id}")
        result = await recognize_screenshot(image_url)
        
        # 创建 OCR 记录
        record = {
            "ocr_id": ocr_id,
            "user_id": user_id,
            "book_id": book_id,
            "ocr_type": OCRType.SCREENSHOT.value,
            "status": OCRStatus.COMPLETED.value,
            "image_url": image_url,
            "result": result.model_dump(),
            "confidence": result.confidence,
            "created_at": datetime.now(),
            "completed_at": datetime.now()
        }
        ocr_records[ocr_id] = record
        
        # 缓存结果
        response_data = {
            "ocr_id": ocr_id,
            "type": "screenshot",
            "source_type": result.source_type.value,
            "transaction_date": result.transaction_date,
            "transaction_time": result.transaction_time,
            "amount": result.amount,
            "counterparty": result.counterparty,
            "description": result.description,
            "confidence": result.confidence,
            "raw_text": result.raw_text
        }
        await cache_ocr_result(ocr_id, response_data)
        
        logger.info(f"截图 OCR 识别成功：{ocr_id}, 来源：{result.source_type.value}, 金额：{result.amount}")
        
        return OCRScreenshotResponse(**response_data)
        
    except OCRException as e:
        logger.error(f"OCR 识别失败：{e}")
        raise HTTPException(status_code=400, detail=str(e))
    except ValueError as e:
        logger.error(f"参数验证失败：{e}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"OCR 识别异常：{e}")
        raise HTTPException(status_code=500, detail=f"OCR 识别失败：{str(e)}")


@router.get("/history", response_model=OCRHistoryResponse, summary="OCR 历史记录")
async def get_ocr_history(
    ocr_type: str = Query(None, description="识别类型：receipt/screenshot"),
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(20, ge=1, le=50, description="每页数量")
):
    """
    获取 OCR 识别历史记录
    
    支持按类型过滤和分页查询
    """
    try:
        # 过滤记录
        filtered_records = list(ocr_records.values())
        if ocr_type:
            filtered_records = [r for r in filtered_records if r["ocr_type"] == ocr_type]
        
        # 排序（按创建时间倒序）
        filtered_records.sort(key=lambda x: x["created_at"], reverse=True)
        
        # 分页
        total = len(filtered_records)
        start = (page - 1) * size
        end = start + size
        page_records = filtered_records[start:end]
        
        # 转换为响应格式
        history_list = []
        for record in page_records:
            result = record.get("result", {})
            history_item = OCRHistoryItem(
                ocr_id=record["ocr_id"],
                ocr_type=record["ocr_type"],
                status=record["status"],
                amount=result.get("total_amount") or result.get("amount"),
                merchant=result.get("merchant"),
                source_type=result.get("source_type"),
                created_at=record["created_at"]
            )
            history_list.append(history_item)
        
        return OCRHistoryResponse(
            list=history_list,
            total=total,
            page=page,
            size=size
        )
        
    except Exception as e:
        logger.error(f"获取 OCR 历史记录失败：{e}")
        raise HTTPException(status_code=500, detail=f"获取历史记录失败：{str(e)}")


@router.get("/results/{ocr_id}", summary="OCR 结果查询")
async def get_ocr_result(ocr_id: str):
    """
    根据 ID 查询 OCR 识别结果
    """
    try:
        # 先从缓存获取
        cached_result = await get_cached_ocr_result(ocr_id)
        if cached_result:
            return {"code": 200, "data": cached_result}
        
        # 从存储中获取
        record = ocr_records.get(ocr_id)
        if not record:
            raise HTTPException(status_code=404, detail="OCR 记录不存在")
        
        return {"code": 200, "data": record.get("result")}
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"查询 OCR 结果失败：{ocr_id}, error: {e}")
        raise HTTPException(status_code=500, detail=f"查询失败：{str(e)}")
