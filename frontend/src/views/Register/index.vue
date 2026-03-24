<template>
  <div class="register-container">
    <div class="register-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <n-card class="register-card" :bordered="false">
      <div class="register-header">
        <div class="logo">
          <n-icon :component="Wallet" size="48" color="#3385ff" />
        </div>
        <h1 class="title">创建账号</h1>
        <p class="subtitle">加入我们，开始智能记账</p>
      </div>

      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
        size="large"
      >
        <n-form-item path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名（4-20 位字母或数字）"
            :clearable="true"
          >
            <template #prefix>
              <n-icon :component="Person" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="email">
          <n-input
            v-model:value="formData.email"
            placeholder="请输入邮箱地址"
            :clearable="true"
          >
            <template #prefix>
              <n-icon :component="Mail" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="phone">
          <n-input
            v-model:value="formData.phone"
            placeholder="请输入手机号（可选）"
            :clearable="true"
          >
            <template #prefix>
              <n-icon :component="Call" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码（6-20 位）"
          >
            <template #prefix>
              <n-icon :component="LockClosed" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="confirmPassword">
          <n-input
            v-model:value="formData.confirmPassword"
            type="password"
            show-password-on="click"
            placeholder="请确认密码"
          >
            <template #prefix>
              <n-icon :component="LockClosed" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="agree">
          <n-checkbox v-model:checked="formData.agree">
            我已阅读并同意
            <n-button text type="primary" @click="showAgreement = true">
              《用户协议》
            </n-button>
            和
            <n-button text type="primary" @click="showPrivacy = true">
              《隐私政策》
            </n-button>
          </n-checkbox>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            block
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </n-button>
        </n-form-item>

        <div class="register-footer">
          已有账号？
          <n-button text type="primary" @click="handleLogin">
            立即登录
          </n-button>
        </div>
      </n-form>
    </n-card>

    <!-- 用户协议弹窗 -->
    <n-modal v-model:show="showAgreement" preset="dialog" title="用户协议" :show-icon="false">
      <div class="agreement-content">
        <p>欢迎使用智能记账服务！</p>
        <p>在使用我们的服务前，请您仔细阅读并理解本协议内容...</p>
        <p>（协议内容开发中）</p>
      </div>
      <template #action>
        <n-button type="primary" @click="showAgreement = false">
          我知道了
        </n-button>
      </template>
    </n-modal>

    <!-- 隐私政策弹窗 -->
    <n-modal v-model:show="showPrivacy" preset="dialog" title="隐私政策" :show-icon="false">
      <div class="agreement-content">
        <p>我们非常重视您的隐私保护！</p>
        <p>本隐私政策说明我们如何收集、使用和保护您的个人信息...</p>
        <p>（隐私政策内容开发中）</p>
      </div>
      <template #action>
        <n-button type="primary" @click="showPrivacy = false">
          我知道了
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Person, Mail, Call, LockClosed, Wallet } from '@vicons/ionicons5'
import { register } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)
const showAgreement = ref(false)
const showPrivacy = ref(false)

const formData = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const validateUsername = (rule, value) => {
  if (!value) {
    return new Error('请输入用户名')
  }
  if (value.length < 4 || value.length > 20) {
    return new Error('用户名长度为 4-20 位')
  }
  if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    return new Error('用户名只能包含字母、数字和下划线')
  }
  return true
}

const validateEmail = (rule, value) => {
  if (!value) {
    return new Error('请输入邮箱')
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    return new Error('请输入有效的邮箱地址')
  }
  return true
}

const validatePhone = (rule, value) => {
  if (!value) return true // 可选字段
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    return new Error('请输入有效的手机号')
  }
  return true
}

const validatePassword = (rule, value) => {
  if (!value) {
    return new Error('请输入密码')
  }
  if (value.length < 6 || value.length > 20) {
    return new Error('密码长度为 6-20 位')
  }
  return true
}

const validateConfirmPassword = (rule, value) => {
  if (!value) {
    return new Error('请确认密码')
  }
  if (value !== formData.password) {
    return new Error('两次输入的密码不一致')
  }
  return true
}

const formRules = {
  username: { validator: validateUsername, trigger: 'blur' },
  email: { validator: validateEmail, trigger: 'blur' },
  phone: { validator: validatePhone, trigger: 'blur' },
  password: { validator: validatePassword, trigger: 'blur' },
  confirmPassword: { validator: validateConfirmPassword, trigger: 'blur' },
  agree: {
    validator: (rule, value) => {
      if (!value) {
        return new Error('请同意用户协议和隐私政策')
      }
      return true
    },
    trigger: 'change'
  }
}

const handleRegister = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    const registerData = {
      username: formData.username,
      email: formData.email,
      phone: formData.phone || undefined,
      password: formData.password
    }

    const res = await register(registerData)
    
    message.success('注册成功！请登录')
    
    // 注册成功后跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
    // 错误信息已在 axios 拦截器中处理
  } finally {
    loading.value = false
  }
}

const handleLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-background {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: -5s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(30px, 30px) rotate(180deg);
  }
}

.register-card {
  position: relative;
  z-index: 1;
  width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.agreement-content {
  max-height: 300px;
  overflow-y: auto;
  line-height: 1.8;
  color: #666;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

:deep(.n-input) {
  border-radius: 8px;
}

:deep(.n-button--primary) {
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

:deep(.n-button--primary:hover) {
  opacity: 0.9;
}

:deep(.n-form-item-feedback-wrapper) {
  padding-top: 4px;
}
</style>
