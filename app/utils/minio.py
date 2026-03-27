"""
MinIO 文件存储工具
"""
import io
import uuid
from datetime import timedelta
from typing import Optional
from fastapi import UploadFile
from loguru import logger
from minio import Minio
from app.core.config import settings

# 创建 MinIO 客户端
minio_client = Minio(
    settings.MINIO_ENDPOINT,
    access_key=settings.MINIO_ACCESS_KEY,
    secret_key=settings.MINIO_SECRET_KEY,
    secure=settings.MINIO_SECURE
)


def init_bucket():
    """初始化存储桶"""
    try:
        if not minio_client.bucket_exists(settings.MINIO_BUCKET):
            minio_client.make_bucket(settings.MINIO_BUCKET)
            logger.info(f"创建 MinIO 存储桶：{settings.MINIO_BUCKET}")
        else:
            logger.info(f"MinIO 存储桶已存在：{settings.MINIO_BUCKET}")
    except Exception as e:
        logger.error(f"初始化 MinIO 存储桶失败：{e}")
        raise


async def upload_image(file: UploadFile, user_id: int, folder: str = "ocr") -> str:
    """
    上传图片到 MinIO
    
    Args:
        file: 上传的文件
        user_id: 用户 ID
        folder: 文件夹名称
        
    Returns:
        str: 文件访问 URL
    """
    try:
        # 读取文件内容
        content = await file.read()
        
        # 验证文件大小
        if len(content) > settings.MAX_IMAGE_SIZE:
            raise ValueError(f"图片文件过大，最大支持 {settings.MAX_IMAGE_SIZE // 1024 // 1024}MB")
        
        # 验证文件类型
        if file.content_type not in settings.ALLOWED_IMAGE_TYPES:
            raise ValueError(f"不支持的图片格式：{file.content_type}")
        
        # 生成文件名
        file_extension = file.filename.split('.')[-1] if '.' in file.filename else 'jpg'
        object_name = f"{folder}/{user_id}/{uuid.uuid4()}.{file_extension}"
        
        # 上传到 MinIO
        minio_client.put_object(
            bucket_name=settings.MINIO_BUCKET,
            object_name=object_name,
            data=io.BytesIO(content),
            length=len(content),
            content_type=file.content_type
        )
        
        logger.info(f"图片上传成功：{object_name}")
        
        # 返回预签名 URL（7 天有效期）
        url = minio_client.presigned_get_object(
            bucket_name=settings.MINIO_BUCKET,
            object_name=object_name,
            expires=timedelta(days=7)
        )
        
        return url
        
    except Exception as e:
        logger.error(f"图片上传失败：{e}")
        raise


async def upload_audio(file: UploadFile, user_id: int, folder: str = "stt") -> str:
    """
    上传音频到 MinIO
    
    Args:
        file: 上传的文件
        user_id: 用户 ID
        folder: 文件夹名称
        
    Returns:
        str: 文件访问 URL
    """
    try:
        # 读取文件内容
        content = await file.read()
        
        # 验证文件大小
        if len(content) > settings.MAX_AUDIO_SIZE:
            raise ValueError(f"音频文件过大，最大支持 {settings.MAX_AUDIO_SIZE // 1024 // 1024}MB")
        
        # 验证文件类型
        if file.content_type not in settings.ALLOWED_AUDIO_TYPES:
            raise ValueError(f"不支持的音频格式：{file.content_type}")
        
        # 生成文件名
        file_extension_map = {
            "audio/mpeg": "mp3",
            "audio/wav": "wav",
            "audio/mp4": "m4a",
            "audio/amr": "amr",
            "audio/x-m4a": "m4a"
        }
        file_extension = file_extension_map.get(file.content_type, 'mp3')
        object_name = f"{folder}/{user_id}/{uuid.uuid4()}.{file_extension}"
        
        # 上传到 MinIO
        minio_client.put_object(
            bucket_name=settings.MINIO_BUCKET,
            object_name=object_name,
            data=io.BytesIO(content),
            length=len(content),
            content_type=file.content_type
        )
        
        logger.info(f"音频上传成功：{object_name}")
        
        # 返回预签名 URL（7 天有效期）
        url = minio_client.presigned_get_object(
            bucket_name=settings.MINIO_BUCKET,
            object_name=object_name,
            expires=timedelta(days=7)
        )
        
        return url
        
    except Exception as e:
        logger.error(f"音频上传失败：{e}")
        raise


def get_file_url(object_name: str, expires_days: int = 7) -> str:
    """
    获取文件访问 URL
    
    Args:
        object_name: 对象名称
        expires_days: URL 有效期（天）
        
    Returns:
        str: 预签名 URL
    """
    url = minio_client.presigned_get_object(
        bucket_name=settings.MINIO_BUCKET,
        object_name=object_name,
        expires=timedelta(days=expires_days)
    )
    return url


def delete_file(object_name: str):
    """
    删除文件
    
    Args:
        object_name: 对象名称
    """
    try:
        minio_client.remove_object(settings.MINIO_BUCKET, object_name)
        logger.info(f"文件删除成功：{object_name}")
    except Exception as e:
        logger.error(f"文件删除失败：{e}")
