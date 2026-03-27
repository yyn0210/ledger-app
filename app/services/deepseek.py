"""
DeepSeek 服务
提供语义理解和文本生成能力
"""
import httpx
from typing import Optional, Dict, List
from loguru import logger

from app.core.config import settings


class DeepSeekService:
    """DeepSeek 服务"""
    
    def __init__(self):
        self.api_key = settings.DEEPSEEK_API_KEY
        self.api_url = settings.DEEPSEEK_API_URL
        self.model = settings.DEEPSEEK_MODEL
    
    async def chat(self, messages: List[Dict], temperature: float = 0.7) -> Optional[str]:
        """
        对话聊天
        
        Args:
            messages: 消息列表
            temperature: 温度参数
            
        Returns:
            回复内容
        """
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{self.api_url}/chat/completions",
                    headers={
                        "Authorization": f"Bearer {self.api_key}",
                        "Content-Type": "application/json"
                    },
                    json={
                        "model": self.model,
                        "messages": messages,
                        "temperature": temperature
                    },
                    timeout=30.0
                )
                
                if response.status_code == 200:
                    data = response.json()
                    return data["choices"][0]["message"]["content"]
                else:
                    logger.error(f"DeepSeek chat failed: {response.status_code} - {response.text}")
                    return None
                    
        except Exception as e:
            logger.error(f"DeepSeek chat exception: {e}")
            return None
    
    async def classify_transaction(self, title: str, amount: float, 
                                    merchant: Optional[str] = None,
                                    categories: List[Dict] = None) -> Optional[Dict]:
        """
        交易智能分类
        
        Args:
            title: 交易标题
            amount: 金额
            merchant: 商户
            categories: 可选分类列表
            
        Returns:
            分类结果
        """
        prompt = f"""请根据以下交易信息，推荐最合适的分类：

交易标题：{title}
交易金额：{amount}
商户：{merchant or '未知'}

请返回：
1. 推荐的分类 ID
2. 分类名称
3. 置信度（0-100）

请以 JSON 格式返回：{{"category_id": 1, "category_name": "餐饮", "confidence": 95}}"""

        messages = [
            {"role": "system", "content": "你是一个智能记账助手，擅长根据交易信息推荐合适的分类。"},
            {"role": "user", "content": prompt}
        ]
        
        result = await self.chat(messages)
        
        if result:
            try:
                # 尝试解析 JSON
                import json
                return json.loads(result)
            except:
                logger.warning(f"Failed to parse classification result: {result}")
                return None
        
        return None
    
    async def extract_receipt_info(self, ocr_text: str) -> Optional[Dict]:
        """
        从小票 OCR 结果中提取关键信息
        
        Args:
            ocr_text: OCR 识别的文字
            
        Returns:
            提取的信息
        """
        prompt = f"""请从以下小票 OCR 识别结果中提取关键信息：

{ocr_text}

请提取：
1. 商户名称
2. 交易日期
3. 总金额
4. 商品明细（如有）

请以 JSON 格式返回。"""

        messages = [
            {"role": "system", "content": "你是一个信息提取专家，擅长从文本中提取结构化信息。"},
            {"role": "user", "content": prompt}
        ]
        
        result = await self.chat(messages)
        
        if result:
            try:
                import json
                return json.loads(result)
            except:
                logger.warning(f"Failed to parse receipt info: {result}")
                return None
        
        return None
    
    async def parse_transaction(self, text: str, categories: Optional[List[Dict]] = None) -> Optional[Dict]:
        """
        解析交易语义（自然语言转结构化数据）
        
        Args:
            text: 自然语言描述（如'今天打车花了 50 块'）
            categories: 可选分类列表
            
        Returns:
            结构化的交易信息
        """
        categories_str = ""
        if categories:
            categories_str = f"\n可选分类列表：{categories}"
        
        prompt = f"""请解析以下交易描述，提取关键信息：

"{text}"

请提取：
1. 金额（数字）
2. 分类（如餐饮、交通、购物等）
3. 商户（如有）
4. 日期（如有，格式 YYYY-MM-DD）
5. 账户（如现金、信用卡、支付宝等，如有）
6. 备注
7. 消费意图（necessary/optional/impulse/investment）

请以 JSON 格式返回，格式如下：
{{
  "amount": 50.0,
  "category": "交通",
  "merchant": "滴滴出行",
  "date": "2026-03-24",
  "account": "支付宝",
  "note": "打车",
  "intent": "necessary"
}}

如果某些信息无法识别，请设为 null。{categories_str}"""

        messages = [
            {"role": "system", "content": "你是一个智能记账助手，擅长从自然语言描述中提取交易信息。"},
            {"role": "user", "content": prompt}
        ]
        
        result = await self.chat(messages)
        
        if result:
            try:
                import json
                # 清理 markdown 格式
                result = result.replace("```json", "").replace("```", "").strip()
                return json.loads(result)
            except:
                logger.warning(f"Failed to parse transaction: {result}")
                return None
        
        return None
    
    async def analyze_consumption_intent(self, text: str, amount: float, category: str) -> Optional[Dict]:
        """
        分析消费意图
        
        Args:
            text: 交易描述
            amount: 金额
            category: 分类
            
        Returns:
            意图分析结果
        """
        prompt = f"""请分析以下消费的意图类型：

交易描述：{text}
金额：{amount}
分类：{category}

消费意图类型：
- necessary: 必要消费（生活必需品、日常开销）
- optional: 可选消费（改善生活质量、非必需）
- impulse: 冲动消费（临时起意、可延迟）
- investment: 投资性消费（教育、健康、技能提升）

请返回：
{{
  "intent": "necessary",
  "reason": "分析理由",
  "suggestions": ["建议 1", "建议 2"]
}}"""

        messages = [
            {"role": "system", "content": "你是一个理财顾问，擅长分析消费行为和提供理财建议。"},
            {"role": "user", "content": prompt}
        ]
        
        result = await self.chat(messages)
        
        if result:
            try:
                import json
                result = result.replace("```json", "").replace("```", "").strip()
                return json.loads(result)
            except:
                logger.warning(f"Failed to analyze intent: {result}")
                return None
        
        return None


# 全局 DeepSeek 服务实例
deepseek_service = DeepSeekService()
