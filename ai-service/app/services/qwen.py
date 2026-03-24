"""
阿里百炼 DashScope 服务
提供 OCR 和文本生成能力
"""
import dashscope
from dashscope import ImageSynthesis, Generation
from typing import Optional, Dict, List
from loguru import logger

from app.core.config import settings


class QwenService:
    """通义千问服务"""
    
    def __init__(self):
        dashscope.api_key = settings.DASHSCOPE_API_KEY
    
    def ocr_receipt(self, image_url: str) -> Optional[Dict]:
        """
        小票 OCR 识别 - 提取结构化信息
        
        Args:
            image_url: 图片 URL
            
        Returns:
            OCR 识别结果（商户/日期/金额/商品明细）
        """
        try:
            response = dashscope.MultiModalConversation.call(
                model=settings.DASHSCOPE_QWEN_VL_MODEL,
                messages=[{
                    "role": "user",
                    "content": [
                        {"image": image_url},
                        {"text": """请识别这张购物小票，提取以下信息：
1. 商户名称
2. 交易日期和时间
3. 总金额
4. 商品明细（名称、数量、单价、金额）

请以 JSON 格式返回，格式如下：
{
  "merchant": "商户名称",
  "transaction_date": "YYYY-MM-DD",
  "transaction_time": "HH:MM:SS",
  "total_amount": 123.45,
  "items": [
    {"name": "商品名", "quantity": 1, "price": 10.00, "amount": 10.00}
  ],
  "raw_text": "原始识别文本"
}

如果某些信息无法识别，请用 null 或空字符串表示。"""}
                    ]
                }]
            )
            
            if response.status_code == 200:
                import json
                content = response.output.choices[0].message.content
                try:
                    content = content.replace("```json", "").replace("```", "").strip()
                    result = json.loads(content)
                    result["success"] = True
                    return result
                except json.JSONDecodeError as e:
                    logger.warning(f"Failed to parse OCR JSON: {e}")
                    return {"success": False, "error": "JSON 解析失败", "raw_text": content}
            else:
                logger.error(f"OCR failed: {response.code} - {response.message}")
                return None
        except Exception as e:
            logger.error(f"OCR exception: {e}")
            return None
    
    def ocr_screenshot(self, image_url: str) -> Optional[Dict]:
        """
        截图 OCR 识别 - 提取交易信息
        
        Args:
            image_url: 截图 URL
            
        Returns:
            OCR 识别结果（来源/日期/金额/对方/描述）
        """
        try:
            response = dashscope.MultiModalConversation.call(
                model=settings.DASHSCOPE_QWEN_VL_MODEL,
                messages=[{
                    "role": "user",
                    "content": [
                        {"image": image_url},
                        {"text": """请识别这张截图，提取以下信息：
1. 截图来源类型（支付宝/微信/淘宝/京东/拼多多/美团/银行转账/其他）
2. 交易日期和时间
3. 交易金额
4. 交易对方（商户/个人）
5. 交易描述/备注

请以 JSON 格式返回，格式如下：
{
  "source_type": "alipay",
  "transaction_date": "YYYY-MM-DD",
  "transaction_time": "HH:MM:SS",
  "amount": 123.45,
  "counterparty": "商户名称",
  "description": "交易描述",
  "raw_text": "原始识别文本"
}

如果无法识别某些字段，请设为 null。"""}
                    ]
                }]
            )
            
            if response.status_code == 200:
                import json
                content = response.output.choices[0].message.content
                try:
                    content = content.replace("```json", "").replace("```", "").strip()
                    result = json.loads(content)
                    result["success"] = True
                    return result
                except json.JSONDecodeError as e:
                    logger.warning(f"Failed to parse screenshot OCR JSON: {e}")
                    return {"success": False, "error": "JSON 解析失败", "raw_text": content}
            else:
                logger.error(f"Screenshot OCR failed: {response.code} - {response.message}")
                return None
        except Exception as e:
            logger.error(f"Screenshot OCR exception: {e}")
            return None
    
    def text_completion(self, prompt: str, system_prompt: Optional[str] = None) -> Optional[str]:
        """
        文本生成
        
        Args:
            prompt: 用户输入
            system_prompt: 系统提示词
            
        Returns:
            生成的文本
        """
        try:
            messages = []
            if system_prompt:
                messages.append({"role": "system", "content": system_prompt})
            messages.append({"role": "user", "content": prompt})
            
            response = Generation.call(
                model=settings.DASHSCOPE_QWEN_MODEL,
                messages=messages
            )
            
            if response.status_code == 200:
                return response.output.choices[0].message.content
            else:
                logger.error(f"Text completion failed: {response.code} - {response.message}")
                return None
        except Exception as e:
            logger.error(f"Text completion exception: {e}")
            return None


# 全局 Qwen 服务实例
qwen_service = QwenService()
