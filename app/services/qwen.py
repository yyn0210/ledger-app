"""
阿里百炼 Qwen-VL 服务
OCR 识别核心服务
"""
import json
import os
from loguru import logger
from dashscope import MultiModalConversation
from app.core.config import settings
from app.models.ocr import OCRReceiptResult, OCRScreenshotResult, ScreenshotSourceType

# 小票 OCR Prompt 模板
RECEIPT_OCR_PROMPT = """请识别这张购物小票，提取以下信息：
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
  ]
}

如果某些字段无法识别，请设为 null。"""

# 截图 OCR Prompt 模板
SCREENSHOT_OCR_PROMPT = """请识别这张截图，提取以下信息：
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
  "description": "交易描述"
}

如果无法识别某些字段，请设为 null。"""


async def recognize_receipt(image_url: str) -> OCRReceiptResult:
    """
    小票 OCR 识别
    
    Args:
        image_url: 图片 URL
        
    Returns:
        OCRReceiptResult: 识别结果
        
    Raises:
        OCRException: OCR 识别失败
    """
    try:
        messages = [{
            "role": "user",
            "content": [
                {"image": image_url},
                {"text": RECEIPT_OCR_PROMPT}
            ]
        }]
        
        response = MultiModalConversation.call(
            model='qwen-vl-max',
            messages=messages,
            api_key=settings.DASHSCOPE_API_KEY
        )
        
        if response.status_code == 200:
            content = response.output.choices[0].message.content
            # 解析 JSON 结果
            result_data = json.loads(content)
            
            return OCRReceiptResult(
                merchant=result_data.get('merchant', '未知商户'),
                transaction_date=result_data.get('transaction_date', ''),
                transaction_time=result_data.get('transaction_time', ''),
                total_amount=result_data.get('total_amount', 0),
                items=[OCRReceiptResult(**item) if isinstance(item, dict) else item 
                       for item in result_data.get('items', [])],
                confidence=result_data.get('confidence', 1.0),
                raw_text=content
            )
        else:
            logger.error(f"Qwen-VL API 调用失败：{response.message}")
            raise OCRException(f"OCR 识别失败：{response.message}")
            
    except json.JSONDecodeError as e:
        logger.error(f"JSON 解析失败：{e}")
        raise OCRException(f"识别结果解析失败：{str(e)}")
    except Exception as e:
        logger.error(f"OCR 识别异常：{e}")
        raise OCRException(f"OCR 识别异常：{str(e)}")


async def recognize_screenshot(image_url: str) -> OCRScreenshotResult:
    """
    截图 OCR 识别
    
    Args:
        image_url: 图片 URL
        
    Returns:
        OCRScreenshotResult: 识别结果
    """
    try:
        messages = [{
            "role": "user",
            "content": [
                {"image": image_url},
                {"text": SCREENSHOT_OCR_PROMPT}
            ]
        }]
        
        response = MultiModalConversation.call(
            model='qwen-vl-max',
            messages=messages,
            api_key=settings.DASHSCOPE_API_KEY
        )
        
        if response.status_code == 200:
            content = response.output.choices[0].message.content
            result_data = json.loads(content)
            
            # 自动检测截图来源
            source_type = detect_screenshot_source(content)
            
            return OCRScreenshotResult(
                source_type=source_type,
                transaction_date=result_data.get('transaction_date', ''),
                transaction_time=result_data.get('transaction_time', ''),
                amount=result_data.get('amount', 0),
                counterparty=result_data.get('counterparty'),
                description=result_data.get('description'),
                confidence=result_data.get('confidence', 1.0),
                raw_text=content
            )
        else:
            logger.error(f"Qwen-VL API 调用失败：{response.message}")
            raise OCRException(f"OCR 识别失败：{response.message}")
            
    except json.JSONDecodeError as e:
        logger.error(f"JSON 解析失败：{e}")
        raise OCRException(f"识别结果解析失败：{str(e)}")
    except Exception as e:
        logger.error(f"OCR 识别异常：{e}")
        raise OCRException(f"OCR 识别异常：{str(e)}")


def detect_screenshot_source(text: str) -> ScreenshotSourceType:
    """
    根据 OCR 识别的文本自动判断截图来源
    
    Args:
        text: OCR 识别的原始文本
        
    Returns:
        ScreenshotSourceType: 截图来源类型
    """
    text_lower = text.lower()
    
    if any(kw in text_lower for kw in ['支付宝', 'alipay', '支']):
        return ScreenshotSourceType.ALIPAY
    elif any(kw in text_lower for kw in ['微信', 'wechat', '微']):
        return ScreenshotSourceType.WECHAT
    elif any(kw in text_lower for kw in ['淘宝', 'taobao', '天猫']):
        return ScreenshotSourceType.TAOMAO
    elif any(kw in text_lower for kw in ['京东', 'jd.com']):
        return ScreenshotSourceType.JD
    elif any(kw in text_lower for kw in ['拼多多', 'pdd']):
        return ScreenshotSourceType.PDD
    elif any(kw in text_lower for kw in ['美团', 'meituan']):
        return ScreenshotSourceType.MEITUAN
    elif any(kw in text_lower for kw in ['银行', '转账', 'bank']):
        return ScreenshotSourceType.BANK_TRANSFER
    else:
        return ScreenshotSourceType.UNKNOWN


class OCRException(Exception):
    """OCR 识别异常"""
    pass
