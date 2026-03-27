"""
语音 STT API 接口
"""
import uuid
from datetime import datetime
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, Query
from loguru import logger
from app.utils.minio import upload_audio, init_bucket
from app.utils.redis import cache_stt_result, get_cached_stt_result
from app.services.iflytek import recognize_speech, STTException
from app.core.config import settings

router = APIRouter()


@router.post("/", summary="语音 STT 识别")
async def stt_recognize(
    audio: UploadFile = File(..., description="语音文件"),
    language: str = Form("zh_cn", description="语言：zh_cn/zh_tw/en_us"),
    dialect: str = Form(None, description="方言：cantonese/sichuan/none")
):
    """
    语音转文字（STT）识别
    
    支持上传语音文件进行识别，用于语音记账场景
    """
    try:
        # 生成 STT 记录 ID
        stt_id = str(uuid.uuid4())
        user_id = 1  # TODO: 从 token 中获取用户 ID
        
        # 上传音频到 MinIO
        audio_url = await upload_audio(audio, user_id, folder="stt")
        
        # 检查缓存
        cached_result = await get_cached_stt_result(stt_id)
        if cached_result:
            logger.info(f"STT 缓存命中：{stt_id}")
            return {"code": 200, "data": cached_result}
        
        # 调用讯飞星火进行 STT 识别
        result = await recognize_speech(audio_url, language, dialect)
        
        # 缓存结果
        response_data = {
            "stt_id": stt_id,
            "status": "completed",
            "audio_url": audio_url,
            "text": result["text"],
            "duration": result["duration"],
            "confidence": result["confidence"],
            "language": language,
            "created_at": datetime.now(),
            "completed_at": datetime.now()
        }
        await cache_stt_result(stt_id, response_data)
        
        logger.info(f"STT 识别成功：{stt_id}, 时长：{result['duration']}s")
        
        return {"code": 200, "data": response_data}
        
    except ValueError as e:
        logger.error(f"参数验证失败：{e}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"STT 识别异常：{e}")
        raise HTTPException(status_code=500, detail=f"STT 识别失败：{str(e)}")


@router.get("/results/{stt_id}", summary="STT 结果查询")
async def get_stt_result(stt_id: str):
    """根据 ID 查询 STT 识别结果"""
    try:
        cached_result = await get_cached_stt_result(stt_id)
        if cached_result:
            return {"code": 200, "data": cached_result}
        raise HTTPException(status_code=404, detail="STT 记录不存在")
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"查询 STT 结果失败：{stt_id}, error: {e}")
        raise HTTPException(status_code=500, detail=f"查询失败：{str(e)}")


@router.get("/history", summary="STT 历史记录")
async def get_stt_history(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(20, ge=1, le=50, description="每页数量")
):
    """获取 STT 识别历史记录"""
    # TODO: 实现历史记录查询
    return {"code": 200, "data": {"list": [], "total": 0, "page": page, "size": size}}
