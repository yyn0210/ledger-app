"""
讯飞星火语音服务
提供语音识别（STT）能力
"""
import base64
import hashlib
import hmac
import json
import websocket
from typing import Optional, Dict, Callable
from datetime import datetime
from loguru import logger
import threading
import time

from app.core.config import settings


class IFlytekService:
    """讯飞星火服务"""
    
    def __init__(self):
        self.app_id = settings.IFLYTEK_APP_ID
        self.api_key = settings.IFLYTEK_API_KEY
        self.api_secret = settings.IFLYTEK_API_SECRET
    
    def _create_signature(self, host: str, date: str) -> str:
        """创建 API 调用签名"""
        signature_origin = f"host: {host}\ndate: {date}\nGET /v1/iat HTTP/1.1"
        
        signature_sha = hmac.new(
            self.api_secret.encode("utf-8"),
            signature_origin.encode("utf-8"),
            digestmod=hashlib.sha256
        ).digest()
        
        signature_sha_base64 = base64.b64encode(signature_sha).decode("utf-8")
        
        authorization_origin = f'api_key="{self.api_key}", algorithm="hmac-sha256", headers="host date request-line", signature="{signature_sha_base64}"'
        authorization = base64.b64encode(authorization_origin.encode("utf-8")).decode("utf-8")
        
        return authorization
    
    async def speech_to_text(
        self,
        audio_data: bytes,
        language: str = "zh_cn",
        dialect: Optional[str] = None
    ) -> Optional[Dict]:
        """
        语音转文字
        
        Args:
            audio_data: 音频数据
            language: 语言（zh_cn/zh_tw/en_us）
            dialect: 方言（none/cantonese/sichuan）
            
        Returns:
            识别的文字和元数据
        """
        try:
            # 讯飞 Websocket URL
            host = "iat-api.xfyun.cn"
            url = f"wss://{host}/v2/iat"
            
            # 生成时间戳
            now = datetime.utcnow()
            date = now.strftime("%a, %d %b %Y %H:%M:%S GMT")
            
            # 创建签名
            authorization = self._create_signature(host, date)
            
            # 构建请求参数
            params = {
                "common": {
                    "app_id": self.app_id
                },
                "business": {
                    "language": language,
                    "domain": "iat",
                    "accent": dialect if dialect else "mandarin",
                    "vad_eos": 3000,
                    "ptt": 0,  # 不带标点
                    "rl": 10000  # 结果级别
                },
                "data": {
                    "status": 2,
                    "format": "audio/L16;rate=16000",
                    "encoding": "raw",
                    "audio": base64.b64encode(audio_data).decode("utf-8")
                }
            }
            
            # 构建完整 URL
            auth_url = f"{url}?authorization={authorization}&date={base64.b64encode(date.encode()).decode()}&host={host}"
            
            # 使用 websocket 连接
            result_text = ""
            result_confidence = 0.9
            
            def on_message(ws, message):
                nonlocal result_text, result_confidence
                try:
                    data = json.loads(message)
                    if data.get("code") == 0:
                        result_data = data.get("data", {})
                        result = result_data.get("result", {})
                        
                        # 提取识别结果
                        ws_result = result.get("ws", [])
                        for ws_item in ws_result:
                            cw_list = ws_item.get("cw", [])
                            for cw_item in cw_list:
                                result_text += cw_item.get("w", "")
                        
                        # 置信度
                        if "confidence" in result:
                            result_confidence = result["confidence"]
                            
                        # 标记完成
                        if result.get("status") == 2:
                            ws.close()
                    else:
                        logger.error(f"iFlytek STT error: {data}")
                except Exception as e:
                    logger.error(f"Parse message error: {e}")
            
            def on_error(ws, error):
                logger.error(f"iFlytek STT websocket error: {error}")
            
            def on_close(ws, code, reason):
                pass
            
            def on_open(ws):
                # 发送数据
                ws.send(json.dumps(params))
            
            # 创建 websocket 连接
            ws = websocket.WebSocketApp(
                auth_url,
                on_open=on_open,
                on_message=on_message,
                on_error=on_error,
                on_close=on_close
            )
            
            # 运行 websocket（阻塞）
            ws.run_forever()
            
            if result_text:
                return {
                    "success": True,
                    "text": result_text,
                    "confidence": result_confidence,
                    "language": language,
                    "dialect": dialect
                }
            else:
                return {
                    "success": False,
                    "error": "未识别到有效语音"
                }
            
        except Exception as e:
            logger.error(f"STT exception: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def speech_to_text_file(
        self,
        file_path: str,
        language: str = "zh_cn",
        dialect: Optional[str] = None
    ) -> Optional[Dict]:
        """
        从文件进行语音转文字
        
        Args:
            file_path: 音频文件路径
            language: 语言
            dialect: 方言
            
        Returns:
            识别的文字
        """
        try:
            with open(file_path, "rb") as f:
                audio_data = f.read()
            
            return await self.speech_to_text(audio_data, language, dialect)
            
        except Exception as e:
            logger.error(f"STT file exception: {e}")
            return None


# 全局讯飞服务实例
iflytek_service = IFlytekService()
