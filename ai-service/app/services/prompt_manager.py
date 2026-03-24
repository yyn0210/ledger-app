"""
Prompt 模板管理
"""
import json
from typing import Dict, List, Optional
from datetime import datetime
from loguru import logger

from app.utils.redis import redis_client


class PromptTemplate:
    """Prompt 模板类"""
    
    def __init__(
        self,
        name: str,
        template: str,
        description: str = "",
        version: str = "1.0.0",
        variables: List[str] = None,
        category: str = "general"
    ):
        self.name = name
        self.template = template
        self.description = description
        self.version = version
        self.variables = variables or []
        self.category = category
        self.created_at = datetime.utcnow()
        self.updated_at = datetime.utcnow()
    
    def render(self, **kwargs) -> str:
        """渲染模板"""
        try:
            return self.template.format(**kwargs)
        except KeyError as e:
            logger.warning(f"Missing variable: {e}")
            raise ValueError(f"缺少变量：{e}")
    
    def to_dict(self) -> dict:
        """转换为字典"""
        return {
            "name": self.name,
            "template": self.template,
            "description": self.description,
            "version": self.version,
            "variables": self.variables,
            "category": self.category,
            "created_at": self.created_at.isoformat(),
            "updated_at": self.updated_at.isoformat()
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> "PromptTemplate":
        """从字典创建"""
        template = cls(
            name=data["name"],
            template=data["template"],
            description=data.get("description", ""),
            version=data.get("version", "1.0.0"),
            variables=data.get("variables", []),
            category=data.get("category", "general")
        )
        if "created_at" in data:
            template.created_at = datetime.fromisoformat(data["created_at"])
        if "updated_at" in data:
            template.updated_at = datetime.fromisoformat(data["updated_at"])
        return template


class PromptManager:
    """Prompt 管理器"""
    
    def __init__(self):
        self.templates: Dict[str, PromptTemplate] = {}
        self._init_builtin_templates()
    
    def _init_builtin_templates(self):
        """初始化内置模板"""
        
        # OCR 相关
        self.add_template(PromptTemplate(
            name="receipt_ocr",
            template="""请识别这张购物小票，提取以下信息：
1. 商户名称
2. 交易日期和时间
3. 总金额
4. 商品明细（名称、数量、单价、金额）

请以 JSON 格式返回，格式如下：
{{
  "merchant": "商户名称",
  "transaction_date": "YYYY-MM-DD",
  "transaction_time": "HH:MM:SS",
  "total_amount": 123.45,
  "items": [
    {{"name": "商品名", "quantity": 1, "price": 10.00, "amount": 10.00}}
  ],
  "raw_text": "原始识别文本"
}}

如果某些信息无法识别，请用 null 或空字符串表示。""",
            description="小票 OCR 识别模板",
            category="ocr",
            variables=["merchant", "transaction_date", "total_amount", "items"]
        ))
        
        self.add_template(PromptTemplate(
            name="screenshot_ocr",
            template="""请识别这张截图，提取以下信息：
1. 截图来源类型（支付宝/微信/淘宝/京东/拼多多/美团/银行转账/其他）
2. 交易日期和时间
3. 交易金额
4. 交易对方（商户/个人）
5. 交易描述/备注

请以 JSON 格式返回，格式如下：
{{
  "source_type": "alipay",
  "transaction_date": "YYYY-MM-DD",
  "transaction_time": "HH:MM:SS",
  "amount": 123.45,
  "counterparty": "商户名称",
  "description": "交易描述",
  "raw_text": "原始识别文本"
}}

如果无法识别某些字段，请设为 null。""",
            description="截图 OCR 识别模板",
            category="ocr",
            variables=["source_type", "transaction_date", "amount", "counterparty"]
        ))
        
        # NLP 相关
        self.add_template(PromptTemplate(
            name="transaction_parse",
            template="""请解析以下交易描述，提取关键信息：

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

如果某些信息无法识别，请设为 null。""",
            description="交易语义解析模板",
            category="nlp",
            variables=["text"]
        ))
        
        self.add_template(PromptTemplate(
            name="intent_analysis",
            template="""请分析以下消费的意图类型：

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
}}""",
            description="消费意图分析模板",
            category="nlp",
            variables=["text", "amount", "category"]
        ))
        
        self.add_template(PromptTemplate(
            name="category_recommend",
            template="""请根据以下信息推荐合适的分类：

商户：{merchant}
金额：{amount}
备注：{note}

可选分类列表：{categories}

请返回：
{{
  "category_id": 1,
  "category_name": "餐饮",
  "confidence": 0.95,
  "reason": "推荐理由"
}}""",
            description="分类推荐模板",
            category="nlp",
            variables=["merchant", "amount", "note", "categories"]
        ))
        
        # STT 相关
        self.add_template(PromptTemplate(
            name="stt_result_cleanup",
            template="""请整理以下语音识别结果，修正可能的错误：

识别结果：{raw_text}

请：
1. 修正错别字
2. 添加标点符号
3. 提取关键信息（金额、商户、日期等）

请以 JSON 格式返回。""",
            description="STT 结果整理模板",
            category="stt",
            variables=["raw_text"]
        ))
        
        # 通用
        self.add_template(PromptTemplate(
            name="info_extraction",
            template="""请从以下文本中提取关键信息：

"{text}"

请提取：
1. 商户名称
2. 金额
3. 日期
4. 商品/服务描述

请以 JSON 格式返回。""",
            description="通用信息提取模板",
            category="general",
            variables=["text"]
        ))
    
    def add_template(self, template: PromptTemplate):
        """添加/更新模板"""
        self.templates[template.name] = template
        
        # 缓存到 Redis
        self._cache_template(template)
        logger.info(f"Added template: {template.name} v{template.version}")
    
    def get_template(self, name: str) -> Optional[PromptTemplate]:
        """获取模板"""
        # 先查缓存
        cached = self._get_cached_template(name)
        if cached:
            return cached
        
        # 查内存
        return self.templates.get(name)
    
    def list_templates(self, category: Optional[str] = None) -> List[Dict]:
        """列出模板"""
        templates = self.templates.values()
        
        if category:
            templates = [t for t in templates if t.category == category]
        
        return [t.to_dict() for t in templates]
    
    def render(self, name: str, **kwargs) -> str:
        """渲染模板"""
        template = self.get_template(name)
        if not template:
            raise ValueError(f"模板不存在：{name}")
        return template.render(**kwargs)
    
    def delete_template(self, name: str) -> bool:
        """删除模板"""
        if name in self.templates:
            del self.templates[name]
            # 清除缓存
            redis_client.delete(f"prompt:template:{name}")
            logger.info(f"Deleted template: {name}")
            return True
        return False
    
    def _cache_template(self, template: PromptTemplate, ttl: int = 3600):
        """缓存模板到 Redis"""
        key = f"prompt:template:{template.name}"
        redis_client.set(key, template.to_dict(), ttl=ttl)
    
    def _get_cached_template(self, name: str) -> Optional[PromptTemplate]:
        """从缓存获取模板"""
        key = f"prompt:template:{name}"
        cached = redis_client.get(key)
        if cached:
            return PromptTemplate.from_dict(cached)
        return None


# 全局 Prompt 管理器实例
prompt_manager = PromptManager()
