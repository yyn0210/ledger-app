"""
本地模型服务单元测试
测试 CPU/GPU 检测、模型加载、离线推理
"""
import pytest
from unittest.mock import patch, MagicMock, AsyncMock
import sys

from app.core.gateway_config import ModelProvider
from app.services.local_model import (
    LocalModelService, LocalModelInfo, InferenceResult,
    DeviceType, get_local_model_service
)


class TestDeviceType:
    """设备类型枚举测试"""
    
    def test_device_type_values(self):
        """测试设备类型值"""
        assert DeviceType.CPU.value == "cpu"
        assert DeviceType.GPU.value == "gpu"
        assert DeviceType.MPS.value == "mps"


class TestLocalModelInfo:
    """本地模型信息测试"""
    
    def test_create_model_info(self):
        """测试创建模型信息"""
        info = LocalModelInfo(
            model_name="qwen2.5-0.5b",
            model_path="./models/qwen2.5-0.5b",
            device=DeviceType.CPU,
            memory_mb=512,
            max_context_length=32768
        )
        
        assert info.model_name == "qwen2.5-0.5b"
        assert info.memory_mb == 512
        assert info.loaded is False
    
    def test_to_dict(self):
        """测试转换为字典"""
        info = LocalModelInfo(
            model_name="qwen2.5-1.5b",
            model_path="./models/qwen2.5-1.5b",
            device=DeviceType.GPU,
            memory_mb=1536,
            max_context_length=32768,
            loaded=True,
            load_time_ms=1500.0
        )
        
        data = info.to_dict()
        assert data["model_name"] == "qwen2.5-1.5b"
        assert data["device"] == "gpu"
        assert data["loaded"] is True
        assert data["load_time_ms"] == 1500.0


class TestInferenceResult:
    """推理结果测试"""
    
    def test_create_success_result(self):
        """测试创建成功结果"""
        result = InferenceResult(
            text="Hello!",
            usage={"prompt_tokens": 10, "completion_tokens": 5, "total_tokens": 15},
            latency_ms=150.0,
            model_name="qwen2.5-0.5b",
            device="cpu",
            success=True
        )
        
        assert result.success is True
        assert result.text == "Hello!"
        assert result.error is None
    
    def test_create_error_result(self):
        """测试创建失败结果"""
        result = InferenceResult(
            text="",
            usage={},
            latency_ms=0,
            model_name="qwen2.5-0.5b",
            device="cpu",
            success=False,
            error="Model loading failed"
        )
        
        assert result.success is False
        assert result.error == "Model loading failed"
    
    def test_to_dict(self):
        """测试转换为字典"""
        result = InferenceResult(
            text="Test response",
            usage={"total_tokens": 20},
            latency_ms=200.0,
            model_name="phi-3-mini",
            device="gpu",
            success=True
        )
        
        data = result.to_dict()
        assert data["text"] == "Test response"
        assert data["success"] is True
        assert data["latency_ms"] == 200.0


class TestLocalModelService:
    """本地模型服务测试"""
    
    @pytest.fixture
    def service(self):
        """创建本地模型服务"""
        return LocalModelService(model_dir="./test_models")
    
    def test_service_initialization(self, service):
        """测试服务初始化"""
        assert service.model_dir.name == "test_models"
        assert service.device in [DeviceType.CPU, DeviceType.GPU, DeviceType.MPS]
        assert len(service.loaded_models) == 0
    
    def test_get_available_models(self, service):
        """测试获取可用模型"""
        models = service.get_available_models()
        
        assert len(models) > 0
        assert any(m["model_name"] == "qwen2.5-0.5b" for m in models)
        assert any(m["model_name"] == "phi-3-mini" for m in models)
    
    def test_supported_models_config(self, service):
        """测试支持的模型配置"""
        assert "qwen2.5-0.5b" in service.SUPPORTED_MODELS
        assert "qwen2.5-1.5b" in service.SUPPORTED_MODELS
        assert "phi-3-mini" in service.SUPPORTED_MODELS
        
        # 验证配置完整性
        for model_name, config in service.SUPPORTED_MODELS.items():
            assert "huggingface_id" in config
            assert "memory_mb" in config
            assert "max_context" in config
            assert "recommended_device" in config
    
    @patch('app.services.local_model.torch')
    def test_detect_device_cuda(self, mock_torch, service):
        """测试 GPU 检测 (CUDA)"""
        mock_torch.cuda.is_available.return_value = True
        mock_torch.backends.mps.is_available.return_value = False
        
        device = service._detect_device()
        assert device == DeviceType.GPU
    
    @patch('app.services.local_model.torch')
    def test_detect_device_mps(self, mock_torch, service):
        """测试 GPU 检测 (MPS)"""
        mock_torch.cuda.is_available.return_value = False
        mock_torch.backends.mps.is_available.return_value = True
        
        device = service._detect_device()
        assert device == DeviceType.MPS
    
    @patch('app.services.local_model.torch')
    def test_detect_device_cpu(self, mock_torch, service):
        """测试 CPU 检测"""
        mock_torch.cuda.is_available.return_value = False
        mock_torch.backends.mps.is_available.return_value = False
        
        device = service._detect_device()
        assert device == DeviceType.CPU
    
    @patch('app.services.local_model.Path.exists')
    def test_download_model_exists(self, mock_exists, service):
        """测试模型下载 (已存在)"""
        mock_exists.return_value = True
        
        result = service.download_model("qwen2.5-0.5b")
        assert result is True
    
    def test_download_model_not_supported(self, service):
        """测试下载不支持的模型"""
        result = service.download_model("invalid-model")
        assert result is False
    
    def test_load_model_not_supported(self, service):
        """测试加载不支持的模型"""
        result = service.load_model("invalid-model")
        assert result is False
    
    def test_get_status(self, service):
        """测试获取服务状态"""
        status = service.get_status()
        
        assert "device" in status
        assert "model_dir" in status
        assert "loaded_models" in status
        assert "available_models" in status
        assert status["device"] == service.device.value
    
    @pytest.mark.asyncio
    async def test_infer_model_not_loaded(self, service):
        """测试推理 (模型未加载)"""
        # 不实际加载模型，应该返回错误
        result = await service.infer("qwen2.5-0.5b", "test prompt")
        
        # 由于没有实际模型文件，应该失败
        assert result.success is False
        assert result.error is not None
    
    def test_get_benchmark_no_model(self, service):
        """测试基准测试 (无模型)"""
        benchmark = service.get_benchmark("qwen2.5-0.5b")
        
        # 由于没有实际模型，应该返回错误
        assert "error" in benchmark or benchmark.get("avg_latency_ms", 0) == 0


class TestLocalModelServiceSingleton:
    """本地模型服务单例测试"""
    
    def test_get_local_model_service_returns_same_instance(self):
        """测试获取服务返回相同实例"""
        service1 = get_local_model_service()
        service2 = get_local_model_service()
        assert service1 is service2
    
    def test_get_local_model_service_custom_dir(self):
        """测试自定义目录"""
        service = get_local_model_service("./custom_models")
        assert str(service.model_dir).endswith("custom_models")


class TestLocalModelIntegration:
    """本地模型集成测试"""
    
    @pytest.fixture
    def service(self):
        return LocalModelService(model_dir="./test_models")
    
    def test_model_config_completeness(self, service):
        """测试模型配置完整性"""
        for model_name, config in service.SUPPORTED_MODELS.items():
            # 验证必需字段
            assert config["huggingface_id"], f"{model_name} 缺少 huggingface_id"
            assert config["memory_mb"] > 0, f"{model_name} memory_mb 应该 > 0"
            assert config["max_context"] > 0, f"{model_name} max_context 应该 > 0"
            
            # 验证设备推荐合理性
            if config["memory_mb"] <= 1024:
                # 小模型可以 CPU
                assert config["recommended_device"] in [DeviceType.CPU, DeviceType.GPU]
            else:
                # 大模型推荐 GPU
                assert config["recommended_device"] in [DeviceType.GPU, DeviceType.MPS]
    
    def test_model_memory_ordering(self, service):
        """测试模型内存排序"""
        models = service.get_available_models()
        memory_sizes = [m["memory_mb"] for m in models]
        
        # 验证内存大小合理
        assert min(memory_sizes) >= 512  # 最小 512MB
        assert max(memory_sizes) <= 4096  # 最大 4GB


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
