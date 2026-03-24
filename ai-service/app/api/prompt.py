"""
Prompt 模板管理 API 路由
"""
from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
from typing import Optional, List
from loguru import logger

from app.services.prompt_manager import prompt_manager, PromptTemplate
from app.utils.redis import redis_client

router = APIRouter(prefix="/api/nlp/prompts", tags=["Prompt 模板管理"])


@router.get("")
async def list_prompts(category: Optional[str] = None):
    """
    获取 Prompt 模板列表
    
    - **category**: 分类过滤（可选，ocr/nlp/stt/general）
    """
    try:
        templates = prompt_manager.list_templates(category)
        return JSONResponse(content={
            "code": 200,
            "data": {
                "templates": templates,
                "total": len(templates),
                "category": category
            }
        })
    except Exception as e:
        logger.error(f"List prompts error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{name}")
async def get_prompt_template(name: str):
    """
    获取模板详情
    
    - **name**: 模板名称
    """
    try:
        template = prompt_manager.get_template(name)
        if not template:
            raise HTTPException(status_code=404, detail=f"模板不存在：{name}")
        
        return JSONResponse(content={
            "code": 200,
            "data": template.to_dict()
        })
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Get template error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/{name}/render")
async def render_prompt_template(name: str, variables: dict):
    """
    渲染模板（变量替换）
    
    - **name**: 模板名称
    - **variables**: 模板变量（JSON 对象）
    
    示例：
    ```json
    {
      "text": "今天打车花了 50 块",
      "amount": 50,
      "category": "交通"
    }
    ```
    """
    try:
        rendered = prompt_manager.render(name, **variables)
        return JSONResponse(content={
            "code": 200,
            "data": {
                "template_name": name,
                "rendered_prompt": rendered
            }
        })
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Render template error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("")
async def create_prompt_template(template_data: dict):
    """
    创建自定义模板
    
    请求体：
    ```json
    {
      "name": "my_custom_template",
      "template": "请分析：{text}",
      "description": "我的自定义模板",
      "category": "general",
      "variables": ["text"]
    }
    ```
    """
    try:
        # 验证必填字段
        required_fields = ["name", "template"]
        for field in required_fields:
            if field not in template_data:
                raise HTTPException(status_code=400, detail=f"缺少必填字段：{field}")
        
        # 检查是否已存在
        existing = prompt_manager.get_template(template_data["name"])
        if existing:
            raise HTTPException(
                status_code=409,
                detail=f"模板已存在：{template_data['name']}，请使用不同名称或先删除"
            )
        
        # 创建模板
        template = PromptTemplate(
            name=template_data["name"],
            template=template_data["template"],
            description=template_data.get("description", ""),
            version=template_data.get("version", "1.0.0"),
            variables=template_data.get("variables", []),
            category=template_data.get("category", "general")
        )
        
        prompt_manager.add_template(template)
        
        return JSONResponse(content={
            "code": 200,
            "message": f"模板创建成功：{template.name}",
            "data": template.to_dict()
        })
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Create template error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.put("/{name}")
async def update_prompt_template(name: str, template_data: dict):
    """
    更新模板（版本升级）
    
    请求体：
    ```json
    {
      "template": "更新后的模板内容",
      "description": "更新描述",
      "version": "1.0.1"
    }
    ```
    """
    try:
        # 获取现有模板
        existing = prompt_manager.get_template(name)
        if not existing:
            raise HTTPException(status_code=404, detail=f"模板不存在：{name}")
        
        # 更新模板
        existing.template = template_data.get("template", existing.template)
        existing.description = template_data.get("description", existing.description)
        existing.version = template_data.get("version", existing.version)
        existing.variables = template_data.get("variables", existing.variables)
        existing.category = template_data.get("category", existing.category)
        existing.updated_at = __import__("datetime").datetime.utcnow()
        
        prompt_manager.add_template(existing)
        
        return JSONResponse(content={
            "code": 200,
            "message": f"模板更新成功：{name} v{existing.version}",
            "data": existing.to_dict()
        })
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Update template error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/{name}")
async def delete_prompt_template(name: str):
    """
    删除模板
    
    - **name**: 模板名称
    """
    try:
        # 不允许删除内置模板（名称不以 my_ 开头）
        if not name.startswith("my_"):
            raise HTTPException(
                status_code=403,
                detail="不允许删除内置模板，请创建自定义模板（名称以 my_ 开头）"
            )
        
        success = prompt_manager.delete_template(name)
        if not success:
            raise HTTPException(status_code=404, detail=f"模板不存在：{name}")
        
        return JSONResponse(content={
            "code": 200,
            "message": f"模板删除成功：{name}"
        })
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Delete template error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
