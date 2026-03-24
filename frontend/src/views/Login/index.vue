<template>
  <div class="login-container">
<<<<<<< HEAD
    <n-card class="login-card" title="智能记账">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </n-button>
        </n-form-item>
=======
    <div class="login-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <n-card class="login-card" :bordered="false">
      <div class="login-header">
        <div class="logo">
          <n-icon :component="Wallet" size="48" color="#3385ff" />
        </div>
        <h1 class="title">智能记账</h1>
        <p class="subtitle">让理财更简单</p>
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
            placeholder="请输入用户名/手机号/邮箱"
            :clearable="true"
          >
            <template #prefix>
              <n-icon :component="Person" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
          >
            <template #prefix>
              <n-icon :component="LockClosed" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <div class="form-options">
            <n-checkbox v-model:checked="rememberMe">
              记住我
            </n-checkbox>
            <n-button text type="primary" @click="handleForgotPassword">
              忘记密码？
            </n-button>
          </div>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            block
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </n-button>
        </n-form-item>

        <n-divider>
          <span class="divider-text">其他登录方式</span>
        </n-divider>

        <n-space justify="center" :size="24">
          <n-button circle @click="handleWechatLogin">
            <template #icon>
              <n-icon :component="LogoWechat" size="24" />
            </template>
          </n-button>
          <n-button circle @click="handleAlipayLogin">
            <template #icon>
              <n-icon :component="LogoAlipay" size="24" />
            </template>
          </n-button>
        </n-space>

        <div class="login-footer">
          还没有账号？
          <n-button text type="primary" @click="handleRegister">
            立即注册
          </n-button>
        </div>
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
<<<<<<< HEAD
=======
import { Person, LockClosed, Wallet, LogoWechat, LogoAlipay } from '@vicons/ionicons5'
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
<<<<<<< HEAD

const formData = reactive({ username: '', password: '' })

const formRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
=======
const rememberMe = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const formRules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
<<<<<<< HEAD
    const data = await login(formData)
    userStore.setToken(data.token)
    userStore.setUserInfo(data.user)
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
=======

    const data = await login(formData)
    userStore.setToken(data.token)
    userStore.setUserInfo(data.user)

    message.success('登录成功')
    
    // 如果选择记住我，设置长期 token
    if (rememberMe.value) {
      localStorage.setItem('token_expire', '30d')
    }
    
    // 跳转到首页或之前访问的页面
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
    // 错误信息已在 axios 拦截器中处理
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
  } finally {
    loading.value = false
  }
}
<<<<<<< HEAD
=======

const handleRegister = () => {
  router.push('/register')
}

const handleForgotPassword = () => {
  message.info('找回密码功能开发中')
}

const handleWechatLogin = () => {
  message.info('微信登录功能开发中')
}

const handleAlipayLogin = () => {
  message.info('支付宝登录功能开发中')
}
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
</script>

<style scoped>
.login-container {
<<<<<<< HEAD
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background-color: #f5f7f9;
}
.login-card {
  width: 400px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
=======
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-background {
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

.login-card {
  position: relative;
  z-index: 1;
  width: 440px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
}

.login-header {
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.divider-text {
  font-size: 12px;
  color: #999;
}

.login-footer {
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}
</style>
