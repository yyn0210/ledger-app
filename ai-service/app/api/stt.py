"""
语音识别 API 路由
"""
import uuid
import json
from fastapi import APIRouter, HTTPException, UploadFile, File, Query, Form
from fastapi.responses import JSONResponse
from typing import Optional
from datetime import datetime
from loguru import logger

from app.services.iflytek import iflytek_service
from app.utils.redis import redis_client
from app.utils.minio import minio_client
from app.models.stt import STTStatus, STTResult, STTHistoryItem, STTHistoryResponse

router = APIRouter(prefix="/api/stt", tags=["语音识别"])

# 支持的音频格式
ALLOWED_AUDIO_TYPES = {
    "audio/mpeg": ".mp3",
    "audio/wav": ".wav",
    "audio/mp4": ".m4a",
    "audio/amr": ".amr",
    "audio/x-m4a": ".m4a"
}

MAX_AUDIO_SIZE = 10 * 1024 * 1024  # 10MB


@router.post("")
async def speech_to_text(
    audio: UploadFile = File(..., description="语音文件（支持 mp3/wav/m4a/amr）"),
    language: str = Form("zh_cn", description="语言（zh_cn/zh_tw/en_us）"),
    dialect: Optional[str] = Form(None, description="方言（none/cantonese/sichuan）"),
    book_id: Optional[int] = Form(None, description="账本 ID（可选）")
):
    """
    语音转文字（STT）
    
    - **audio**: 语音文件（必填，支持 mp3/wav/m4a/amr）
    - **language**: 语言（可选，默认 zh_cn）
    - **dialect**: 方言（可选，默认 none）
    - **book_id**: 账本 ID（可选，用于后续自动创建交易）
    
    返回识别的文字内容。
    """
    # 验证音频格式
    if audio.content_type not in ALLOWED_AUDIO_TYPES:
        raise HTTPException(
            status_code=400,
            detail=f"不支持的音频格式。支持：{', '.join(ALLOWED_AUDIO_TYPES.keys())}"
        )
    
    # 验证音频大小
    content = await audio.read()
    if len(content) > MAX_AUDIO_SIZE:
        raise HTTPException(status_code=400, detail="音频文件大小不能超过 10MB")
    
    stt_id = str(uuid.uuid4())
    user_id = 1  # TODO: 从 token 获取用户 ID
    
    try:
        # 上传音频到 MinIO
        file_extension = ALLOWED_AUDIO_TYPES[audio.content_type]
        audio_url = minio_client.upload_audio(content, user_id, file_extension)
        public_url = f"http://localhost:9000/{audio_url}"
        
        # 调用 STT 服务
        stt_result = await iflytek_service.speech_to_text(content, language, dialect)
        
        if not stt_result or not stt_result.get("success"):
            # STT 识别失败
            result = STTResult(
                stt_id=stt_id,
                status=STTStatus.FAILED,
                audio_url=public_url,
                error_message=stt_result.get("error", "语音识别失败") if stt_result else "语音识别失败",
                language=language,
                dialect=dialect,
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
        else:
            # STT 识别成功
            result = STTResult(
                stt_id=stt_id,
                status=STTStatus.COMPLETED,
                audio_url=public_url,
                text=stt_result.get("text", ""),
                duration=stt_result.get("duration", 0),
                confidence=stt_result.get("confidence", 0.9),
                language=language,
                dialect=dialect,
                created_at=datetime.utcnow(),
                completed_at=datetime.utcnow()
            )
            
            # 缓存结果 24 小时
            cache_key = f"stt:result:{stt_id}"
            redis_client.set(cache_key, result.dict(), ttl=86400)
        
        # 保存到历史记录
        history_key = f"stt:history:{user_id}"
        history_item = {
            "stt_id": stt_id,
            "text": stt_result.get("text", "") if stt_result else "",
            "duration": stt_result.get("duration", 0) if stt_result else 0,
            "status": result.status.value,
            "created_at": result.created_at.isoformat()
        }
        redis_client.client.lpush(history_key, json.dumps(history_item))
        redis_client.client.ltrim(history_key, 0, 99)  # 保留最近 100 条
        
        return JSONResponse(content={
            "code": 200,
            "data": result.dict()
        })
        
    except Exception as e:
        logger.error(f"STT API error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/results/{stt_id}")
async def get_stt_result(stt_id: str):
    """
    查询 STT 识别结果
    
    - **stt_id**: STT 任务 ID
    """
    # 先查缓存
    cache_key = f"stt:result:{stt_id}"
    cached = redis_client.get(cache_key)
    
    if cached:
        logger.info(f"STT cache hit: {cache_key}")
        return JSONResponse(content={
            "code": 200,
            "data": cached
        })
    
    # 缓存未命中，返回 404
    raise HTTPException(status_code=404, detail="STT 结果不存在或已过期")


@router.get("/history")
async def get_stt_history(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(20, ge=1, le=50, description="每页数量")
):
    """
    获取 STT 识别历史记录
    
    - **page**: 页码（默认 1）
    - **size**: 每页数量（默认 20，最大 50）
    """
    user_id = 1  # TODO: 从 token 获取用户 ID
    history_key = f"stt:history:{user_id}"
    
    # 获取所有历史记录
    history_data = redis_client.client.lrange(history_key, 0, -1)
    
    # 分页
    total = len(history_data)
    start = (page - 1) * size
    end = start + size
    page_data = history_data[start:end]
    
    # 解析数据
    history_list = [
        STTHistoryItem(**json.loads(item))
        for item in page_data
    ]
    
    return JSONResponse(content={
        "code": 200,
        "data": {
            "list": [item.dict() for item in history_list],
            "total": total,
            "page": page,
            "size": size
        }
    })
