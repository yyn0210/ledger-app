"""
Prompt 模板管理（向后兼容，使用新的 prompt_manager）
"""
from app.services.prompt_manager import prompt_manager, PromptTemplate

# 向后兼容：导出旧的接口
__all__ = ["prompt_manager", "PromptTemplate"]
