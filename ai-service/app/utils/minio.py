"""
MinIO 文件存储工具
"""
import io
from typing import Optional
from loguru import logger
from minio import Minio
from minio.error import S3Error

from app.core.config import settings


class MinIOClient:
    """MinIO 客户端"""
    
    def __init__(self):
        self.client = Minio(
            endpoint=getattr(settings, 'MINIO_ENDPOINT', 'localhost:9000'),
            access_key=getattr(settings, 'MINIO_ACCESS_KEY', 'minioadmin'),
            secret_key=getattr(settings, 'MINIO_SECRET_KEY', 'minioadmin'),
            secure=False
        )
        self.bucket_name = getattr(settings, 'MINIO_BUCKET', 'ledger-app')
        self._ensure_bucket_exists()
    
    def _ensure_bucket_exists(self):
        """确保存储桶存在"""
        try:
            if not self.client.bucket_exists(self.bucket_name):
                self.client.make_bucket(self.bucket_name)
                logger.info(f"Created bucket: {self.bucket_name}")
        except S3Error as e:
            logger.error(f"Failed to create bucket: {e}")
    
    def upload_file(self, file_data: bytes, object_name: str, content_type: str = "application/octet-stream") -> str:
        """
        上传文件到 MinIO
        
        Args:
            file_data: 文件数据
            object_name: 对象名称
            content_type: 内容类型
            
        Returns:
            文件 URL
        """
        try:
            self.client.put_object(
                bucket_name=self.bucket_name,
                object_name=object_name,
                data=io.BytesIO(file_data),
                length=len(file_data),
                content_type=content_type
            )
            
            # 返回公开访问 URL（需要配置公开访问或使用预签名 URL）
            return f"/{self.bucket_name}/{object_name}"
            
        except S3Error as e:
            logger.error(f"Upload file error: {e}")
            raise
    
    def upload_image(self, file_data: bytes, user_id: int, file_extension: str) -> str:
        """
        上传图片
        
        Args:
            file_data: 图片数据
            user_id: 用户 ID
            file_extension: 文件扩展名
            
        Returns:
            图片 URL
        """
        import uuid
        object_name = f"ocr/{user_id}/{uuid.uuid4()}.{file_extension}"
        content_type = f"image/{file_extension}"
        
        return self.upload_file(file_data, object_name, content_type)
    
    def upload_audio(self, file_data: bytes, user_id: int, file_extension: str) -> str:
        """
        上传音频
        
        Args:
            file_data: 音频数据
            user_id: 用户 ID
            file_extension: 文件扩展名
            
        Returns:
            音频 URL
        """
        import uuid
        object_name = f"stt/{user_id}/{uuid.uuid4()}{file_extension}"
        content_types = {
            ".mp3": "audio/mpeg",
            ".wav": "audio/wav",
            ".m4a": "audio/mp4",
            ".amr": "audio/amr"
        }
        content_type = content_types.get(file_extension, "audio/mpeg")
        
        return self.upload_file(file_data, object_name, content_type)
    
    def get_presigned_url(self, object_name: str, expires_days: int = 7) -> str:
        """
        获取预签名 URL（临时访问）
        
        Args:
            object_name: 对象名称
            expires_days: 过期天数
            
        Returns:
            预签名 URL
        """
        from datetime import timedelta
        try:
            url = self.client.presigned_get_object(
                bucket_name=self.bucket_name,
                object_name=object_name,
                expires=timedelta(days=expires_days)
            )
            return url
        except S3Error as e:
            logger.error(f"Get presigned URL error: {e}")
            return f"/{self.bucket_name}/{object_name}"
    
    def delete_file(self, object_name: str) -> bool:
        """
        删除文件
        
        Args:
            object_name: 对象名称
            
        Returns:
            是否成功
        """
        try:
            self.client.remove_object(self.bucket_name, object_name)
            return True
        except S3Error as e:
            logger.error(f"Delete file error: {e}")
            return False


# 全局 MinIO 客户端实例
minio_client = MinIOClient()
