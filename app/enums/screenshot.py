"""
截图来源类型枚举
"""
from enum import Enum


class ScreenshotSourceType(str, Enum):
    """截图来源类型"""
    ALIPAY = "alipay"        # 支付宝账单
    WECHAT = "wechat"        # 微信账单
    TAOMAO = "taobao"        # 淘宝订单
    JD = "jd"               # 京东订单
    PDD = "pdd"             # 拼多多订单
    MEITUAN = "meituan"     # 美团订单
    BANK_TRANSFER = "bank"  # 银行转账
    UNKNOWN = "unknown"     # 未知来源
    
    @classmethod
    def get_display_name(cls, source_type: str) -> str:
        """获取显示名称"""
        names = {
            "alipay": "支付宝",
            "wechat": "微信",
            "taobao": "淘宝",
            "jd": "京东",
            "pdd": "拼多多",
            "meituan": "美团",
            "bank": "银行转账",
            "unknown": "未知"
        }
        return names.get(source_type, "未知")


# 来源检测关键词
SOURCE_KEYWORDS = {
    ScreenshotSourceType.ALIPAY: ["支付宝", "alipay", "支"],
    ScreenshotSourceType.WECHAT: ["微信", "wechat", "微", "微信支付"],
    ScreenshotSourceType.TAOMAO: ["淘宝", "taobao", "天猫", "tmall"],
    ScreenshotSourceType.JD: ["京东", "jd.com", "jd "],
    ScreenshotSourceType.PDD: ["拼多多", "pdd", "拼多多"],
    ScreenshotSourceType.MEITUAN: ["美团", "meituan", "美团外卖"],
    ScreenshotSourceType.BANK_TRANSFER: ["银行", "转账", "bank", "转入", "转出"]
}


def detect_screenshot_source(text: str) -> ScreenshotSourceType:
    """
    根据 OCR 识别的文本自动判断截图来源
    
    Args:
        text: OCR 识别的文本
        
    Returns:
        截图来源类型
    """
    if not text:
        return ScreenshotSourceType.UNKNOWN
    
    text_lower = text.lower()
    
    # 按优先级检测（更具体的关键词优先）
    priority_order = [
        ScreenshotSourceType.MEITUAN,  # 美团外卖最具体
        ScreenshotSourceType.BANK_TRANSFER,
        ScreenshotSourceType.TAOMAO,
        ScreenshotSourceType.WECHAT,
        ScreenshotSourceType.ALIPAY,
        ScreenshotSourceType.JD,
        ScreenshotSourceType.PDD,
    ]
    
    for source_type in priority_order:
        keywords = SOURCE_KEYWORDS.get(source_type, [])
        if any(kw in text_lower for kw in keywords):
            return source_type
    
    return ScreenshotSourceType.UNKNOWN
