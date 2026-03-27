"""
Prompt 模板管理 API 接口
"""
from fastapi import APIRouter, HTTPException, Body, Query
from loguru import logger
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

router = APIRouter()


class PromptTemplate(BaseModel):
    """Prompt 模板模型"""
    id: str
    name: str
    code: str
    content: str
    version: str
    variables: List[str]
    description: Optional[str] = None
    created_at: datetime
    updated_at: datetime


# 模拟 Prompt 模板存储
prompt_templates = {
    "receipt_ocr": PromptTemplate(
        id="1",
        name="小票 OCR 识别",
        code="RECEIPT_OCR",
        content="""请识别这张购物小票，提取以下信息：
1. 商户名称
2. 交易日期和时间
3. 总金额
4. 商品明细（名称、数量、单价、金额）

请以 JSON 格式返回。""",
        version="1.0.0",
        variables=["image_url"],
        description="用于购物小票 OCR 识别的 Prompt 模板",
        created_at=datetime.now(),
        updated_at=datetime.now()
    ),
    "screenshot_ocr": PromptTemplate(
        id="2",
        name="截图 OCR 识别",
        code="SCREENSHOT_OCR",
        content="""请识别这张截图，提取以下信息：
1. 截图来源类型
2. 交易日期和时间
3. 交易金额
4. 交易对方
5. 交易描述

请以 JSON 格式返回。""",
        version="1.0.0",
        variables=["image_url"],
        description="用于记账截图 OCR 识别的 Prompt 模板",
        created_at=datetime.now(),
        updated_at=datetime.now()
    ),
    "nlp_parse": PromptTemplate(
        id="3",
        name="语义理解解析",
        code="NLP_PARSE",
        content="""请解析这段自然语言，提取交易相关信息：
- 金额
- 分类
- 商户
- 时间
- 备注

请以 JSON 格式返回。""",
        version="1.0.0",
        variables=["text"],
        description="用于语义理解解析的 Prompt 模板",
        created_at=datetime.now(),
        updated_at=datetime.now()
    )
}


@router.get("/", summary="获取 Prompt 模板列表")
async def get_prompts():
    """获取所有 Prompt 模板列表"""
    try:
        templates = list(prompt_templates.values())
        return {"code": 200, "data": [t.model_dump() for t in templates]}
    except Exception as e:
        logger.error(f"获取 Prompt 列表失败：{e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{code}", summary="获取指定 Prompt 模板")
async def get_prompt(code: str):
    """根据 code 获取 Prompt 模板"""
    try:
        template = prompt_templates.get(code)
        if not template:
            raise HTTPException(status_code=404, detail="Prompt 模板不存在")
        return {"code": 200, "data": template.model_dump()}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取 Prompt 失败：{code}, error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/", summary="创建/更新 Prompt 模板")
async def save_prompt(template: PromptTemplate = Body(...)):
    """创建或更新 Prompt 模板"""
    try:
        prompt_templates[template.code] = template
        logger.info(f"Prompt 模板已保存：{template.code}")
        return {"code": 200, "message": "保存成功", "data": template.model_dump()}
    except Exception as e:
        logger.error(f"保存 Prompt 失败：{e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/{code}", summary="删除 Prompt 模板")
async def delete_prompt(code: str):
    """删除 Prompt 模板"""
    try:
        if code not in prompt_templates:
            raise HTTPException(status_code=404, detail="Prompt 模板不存在")
        del prompt_templates[code]
        logger.info(f"Prompt 模板已删除：{code}")
        return {"code": 200, "message": "删除成功"}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除 Prompt 失败：{code}, error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
