"""
本地模型推理服务 - 支持 CPU/GPU 自动检测、离线推理
"""
import time
import json
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from loguru import logger

from app.core.gateway_config import ModelConfig, ModelProvider


class DeviceType(str, Enum):
    """设备类型"""
    CPU = "cpu"
    GPU = "gpu"
    MPS = "mps"  # Apple Silicon


@dataclass
class LocalModelInfo:
    """本地模型信息"""
    model_name: str
    model_path: str
    device: DeviceType
    memory_mb: int
    max_context_length: int
    loaded: bool = False
    load_time_ms: float = 0.0
    
    def to_dict(self) -> Dict:
        return {
            "model_name": self.model_name,
            "model_path": self.model_path,
            "device": self.device.value,
            "memory_mb": self.memory_mb,
            "max_context_length": self.max_context_length,
            "loaded": self.loaded,
            "load_time_ms": self.load_time_ms,
        }


@dataclass
class InferenceResult:
    """推理结果"""
    text: str
    usage: Dict[str, int]  # prompt_tokens, completion_tokens, total_tokens
    latency_ms: float
    model_name: str
    device: str
    success: bool
    error: Optional[str] = None
    
    def to_dict(self) -> Dict:
        return {
            "text": self.text,
            "usage": self.usage,
            "latency_ms": self.latency_ms,
            "model_name": self.model_name,
            "device": self.device,
            "success": self.success,
            "error": self.error,
        }


class LocalModelService:
    """本地模型服务 - 管理本地模型的加载和推理"""
    
    # 支持的本地模型配置
    SUPPORTED_MODELS = {
        "qwen2.5-0.5b": {
            "huggingface_id": "Qwen/Qwen2.5-0.5B-Instruct",
            "memory_mb": 512,
            "max_context": 32768,
            "recommended_device": DeviceType.CPU,
            "description": "超轻量模型，适合 CPU 推理"
        },
        "qwen2.5-1.5b": {
            "huggingface_id": "Qwen/Qwen2.5-1.5B-Instruct",
            "memory_mb": 1536,
            "max_context": 32768,
            "recommended_device": DeviceType.GPU,
            "description": "轻量模型，适合 GPU 推理"
        },
        "phi-3-mini": {
            "huggingface_id": "microsoft/Phi-3-mini-4k-instruct",
            "memory_mb": 2048,
            "max_context": 4096,
            "recommended_device": DeviceType.GPU,
            "description": "微软小模型，性能优秀"
        },
    }
    
    def __init__(self, model_dir: str = "./models"):
        """
        初始化本地模型服务
        
        Args:
            model_dir: 模型存储目录
        """
        self.model_dir = Path(model_dir)
        self.loaded_models: Dict[str, LocalModelInfo] = {}
        self.device = self._detect_device()
        logger.info(f"LocalModelService 初始化完成 - 设备：{self.device.value}, 模型目录：{self.model_dir}")
    
    def _detect_device(self) -> DeviceType:
        """自动检测设备类型"""
        try:
            # 尝试导入 torch 检测 GPU
            import torch
            
            if torch.cuda.is_available():
                logger.info("检测到 NVIDIA GPU")
                return DeviceType.GPU
            elif torch.backends.mps.is_available():
                logger.info("检测到 Apple Silicon MPS")
                return DeviceType.MPS
            else:
                logger.info("未检测到 GPU，使用 CPU")
                return DeviceType.CPU
                
        except ImportError:
            logger.warning("PyTorch 未安装，使用 CPU")
            return DeviceType.CPU
    
    def get_available_models(self) -> List[Dict]:
        """获取可用模型列表"""
        return [
            {
                "model_name": name,
                **info,
                "huggingface_id": info["huggingface_id"],
                "recommended_device": info["recommended_device"].value,
            }
            for name, info in self.SUPPORTED_MODELS.items()
        ]
    
    def download_model(self, model_name: str) -> bool:
        """
        下载模型
        
        Args:
            model_name: 模型名称
            
        Returns:
            是否成功
        """
        if model_name not in self.SUPPORTED_MODELS:
            logger.error(f"不支持的模型：{model_name}")
            return False
        
        model_info = self.SUPPORTED_MODELS[model_name]
        model_path = self.model_dir / model_name
        
        if model_path.exists():
            logger.info(f"模型已存在：{model_path}")
            return True
        
        try:
            from transformers import AutoModelForCausalLM, AutoTokenizer
            
            logger.info(f"开始下载模型：{model_info['huggingface_id']}")
            
            # 下载 tokenizer
            tokenizer = AutoTokenizer.from_pretrained(
                model_info["huggingface_id"],
                cache_dir=str(model_path)
            )
            
            # 下载模型
            model = AutoModelForCausalLM.from_pretrained(
                model_info["huggingface_id"],
                cache_dir=str(model_path),
                torch_dtype="auto",
                device_map="auto"
            )
            
            logger.info(f"模型下载完成：{model_path}")
            return True
            
        except Exception as e:
            logger.error(f"模型下载失败：{e}")
            return False
    
    def load_model(self, model_name: str) -> bool:
        """
        加载模型到内存
        
        Args:
            model_name: 模型名称
            
        Returns:
            是否成功
        """
        if model_name in self.loaded_models and self.loaded_models[model_name].loaded:
            logger.info(f"模型已加载：{model_name}")
            return True
        
        if model_name not in self.SUPPORTED_MODELS:
            logger.error(f"不支持的模型：{model_name}")
            return False
        
        model_path = self.model_dir / model_name
        
        if not model_path.exists():
            logger.info(f"模型未下载，开始下载：{model_name}")
            if not self.download_model(model_name):
                return False
        
        try:
            start_time = time.time()
            
            from transformers import AutoModelForCausalLM, AutoTokenizer
            
            # 选择设备
            model_info = self.SUPPORTED_MODELS[model_name]
            device = model_info["recommended_device"]
            
            # 如果用户设备不如推荐设备，降级
            if device == DeviceType.GPU and self.device == DeviceType.CPU:
                logger.warning(f"推荐 GPU 但只有 CPU，性能会较慢")
            
            # 加载 tokenizer
            tokenizer = AutoTokenizer.from_pretrained(
                str(model_path),
                local_files_only=True
            )
            
            # 加载模型
            model = AutoModelForCausalLM.from_pretrained(
                str(model_path),
                local_files_only=True,
                torch_dtype="auto",
                device_map="auto"
            )
            
            load_time_ms = (time.time() - start_time) * 1000
            
            # 存储模型信息
            self.loaded_models[model_name] = LocalModelInfo(
                model_name=model_name,
                model_path=str(model_path),
                device=self.device,
                memory_mb=model_info["memory_mb"],
                max_context_length=model_info["max_context"],
                loaded=True,
                load_time_ms=load_time_ms
            )
            
            logger.info(f"模型加载成功：{model_name} (耗时 {load_time_ms:.0f}ms)")
            return True
            
        except Exception as e:
            logger.error(f"模型加载失败：{e}")
            return False
    
    def unload_model(self, model_name: str) -> bool:
        """卸载模型释放内存"""
        if model_name not in self.loaded_models:
            return False
        
        try:
            import torch
            import gc
            
            # 删除模型
            del self.loaded_models[model_name]
            
            # 清理 GPU 内存
            if self.device == DeviceType.GPU:
                torch.cuda.empty_cache()
            
            gc.collect()
            
            logger.info(f"模型已卸载：{model_name}")
            return True
            
        except Exception as e:
            logger.error(f"模型卸载失败：{e}")
            return False
    
    async def infer(
        self,
        model_name: str,
        prompt: str,
        max_tokens: int = 512,
        temperature: float = 0.7,
        top_p: float = 0.9
    ) -> InferenceResult:
        """
        本地推理
        
        Args:
            model_name: 模型名称
            prompt: 输入提示
            max_tokens: 最大生成 token 数
            temperature: 温度参数
            top_p: Top-p 采样参数
            
        Returns:
            推理结果
        """
        # 确保模型已加载
        if model_name not in self.loaded_models or not self.loaded_models[model_name].loaded:
            if not self.load_model(model_name):
                return InferenceResult(
                    text="",
                    usage={},
                    latency_ms=0,
                    model_name=model_name,
                    device=self.device.value,
                    success=False,
                    error="模型加载失败"
                )
        
        try:
            start_time = time.time()
            
            model_info = self.loaded_models[model_name]
            
            from transformers import AutoModelForCausalLM, AutoTokenizer
            
            # 重新加载模型（简化实现，实际应该缓存）
            tokenizer = AutoTokenizer.from_pretrained(
                model_info.model_path,
                local_files_only=True
            )
            model = AutoModelForCausalLM.from_pretrained(
                model_info.model_path,
                local_files_only=True,
                torch_dtype="auto",
                device_map="auto"
            )
            
            # Tokenize 输入
            inputs = tokenizer(prompt, return_tensors="pt").to(model.device)
            input_length = inputs.input_ids.shape[1]
            
            # 生成
            with torch.no_grad():
                outputs = model.generate(
                    **inputs,
                    max_new_tokens=max_tokens,
                    temperature=temperature,
                    top_p=top_p,
                    pad_token_id=tokenizer.eos_token_id
                )
            
            # 解码输出
            generated_ids = outputs[0][input_length:]
            text = tokenizer.decode(generated_ids, skip_special_tokens=True)
            
            latency_ms = (time.time() - start_time) * 1000
            
            return InferenceResult(
                text=text,
                usage={
                    "prompt_tokens": input_length,
                    "completion_tokens": len(generated_ids),
                    "total_tokens": input_length + len(generated_ids)
                },
                latency_ms=latency_ms,
                model_name=model_name,
                device=self.device.value,
                success=True
            )
            
        except Exception as e:
            logger.error(f"本地推理失败：{e}")
            return InferenceResult(
                text="",
                usage={},
                latency_ms=0,
                model_name=model_name,
                device=self.device.value,
                success=False,
                error=str(e)
            )
    
    def get_status(self) -> Dict:
        """获取服务状态"""
        return {
            "device": self.device.value,
            "model_dir": str(self.model_dir),
            "loaded_models": {
                name: info.to_dict()
                for name, info in self.loaded_models.items()
            },
            "available_models": self.get_available_models(),
        }
    
    def get_benchmark(self, model_name: str, prompt: str = "你好") -> Dict:
        """
        模型性能基准测试
        
        Args:
            model_name: 模型名称
            prompt: 测试提示
            
        Returns:
            基准测试结果
        """
        if not self.load_model(model_name):
            return {"error": "模型加载失败"}
        
        results = []
        
        # 多次测试取平均
        for i in range(3):
            result = self.infer(model_name, prompt, max_tokens=100)
            if result.success:
                results.append({
                    "latency_ms": result.latency_ms,
                    "tokens_per_second": result.usage.get("total_tokens", 0) / (result.latency_ms / 1000)
                })
        
        if not results:
            return {"error": "推理失败"}
        
        avg_latency = sum(r["latency_ms"] for r in results) / len(results)
        avg_tps = sum(r["tokens_per_second"] for r in results) / len(results)
        
        return {
            "model_name": model_name,
            "device": self.device.value,
            "avg_latency_ms": avg_latency,
            "avg_tokens_per_second": avg_tps,
            "runs": len(results),
        }


# 单例
_local_model_service: Optional[LocalModelService] = None


def get_local_model_service(model_dir: str = "./models") -> LocalModelService:
    """获取本地模型服务"""
    global _local_model_service
    if _local_model_service is None:
        _local_model_service = LocalModelService(model_dir)
    return _local_model_service
