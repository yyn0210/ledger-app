<template>
  <view class="register-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="navbar-back" @click="goBack">
        <u-icon name="arrow-left" size="24" color="#fff"></u-icon>
      </view>
      <text class="navbar-title">创建账号</text>
      <view class="navbar-placeholder"></view>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="account" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.username"
            placeholder="请输入用户名（4-20 位）"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="email" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.email"
            type="email"
            placeholder="请输入邮箱"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="phone" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.phone"
            type="number"
            placeholder="请输入手机号（可选）"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="lock" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.password"
            type="password"
            placeholder="请输入密码（6-20 位）"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="lock" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请确认密码"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <!-- 协议勾选 -->
      <view class="agreement-item" @click="toggleAgree">
        <view class="checkbox" :class="{ checked: formData.agree }">
          <u-icon v-if="formData.agree" name="checkmark" size="14" color="#fff"></u-icon>
        </view>
        <text class="agreement-text">
          我已阅读并同意
          <text class="agreement-link" @click.stop="showAgreement = true">《用户协议》</text>
          和
          <text class="agreement-link" @click.stop="showPrivacy = true">《隐私政策》</text>
        </text>
      </view>

      <!-- 注册按钮 -->
      <view class="submit-btn" @click="handleRegister">
        <text class="btn-text">{{ loading ? '注册中...' : '立即注册' }}</text>
      </view>
    </view>

    <!-- 底部登录提示 -->
    <view class="footer">
      <text class="footer-text">已有账号？</text>
      <text class="footer-link" @click="handleLogin">立即登录</text>
    </view>

    <!-- 用户协议弹窗 -->
    <u-popup v-model="showAgreement" mode="center" :round="16">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">用户协议</text>
          <u-icon name="close" size="24" @click="showAgreement = false"></u-icon>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <text class="content-text">欢迎使用智能记账服务！\n\n在使用我们的服务前，请您仔细阅读并理解本协议内容。本协议是您与智能记账平台之间关于使用本软件服务所订立的协议。\n\n（协议内容开发中）</text>
        </scroll-view>
        <view class="popup-footer">
          <button class="popup-btn" @click="showAgreement = false">我知道了</button>
        </view>
      </view>
    </u-popup>

    <!-- 隐私政策弹窗 -->
    <u-popup v-model="showPrivacy" mode="center" :round="16">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">隐私政策</text>
          <u-icon name="close" size="24" @click="showPrivacy = false"></u-icon>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <text class="content-text">我们非常重视您的隐私保护！\n\n本隐私政策说明我们如何收集、使用和保护您的个人信息。请您仔细阅读本政策，以了解我们对您个人信息的处理方式。\n\n（隐私政策内容开发中）</text>
        </scroll-view>
        <view class="popup-footer">
          <button class="popup-btn" @click="showPrivacy = false">我知道了</button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
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

const toggleAgree = () => {
  formData.agree = !formData.agree
}

const goBack = () => {
  uni.navigateBack()
}

const validateForm = () => {
  if (!formData.username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return false
  }
  if (formData.username.length < 4 || formData.username.length > 20) {
    uni.showToast({ title: '用户名长度为 4-20 位', icon: 'none' })
    return false
  }
  if (!formData.email.trim()) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(formData.email)) {
    uni.showToast({ title: '请输入有效的邮箱', icon: 'none' })
    return false
  }
  if (formData.phone && !/^1[3-9]\d{9}$/.test(formData.phone)) {
    uni.showToast({ title: '请输入有效的手机号', icon: 'none' })
    return false
  }
  if (!formData.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return false
  }
  if (formData.password.length < 6 || formData.password.length > 20) {
    uni.showToast({ title: '密码长度为 6-20 位', icon: 'none' })
    return false
  }
  if (formData.password !== formData.confirmPassword) {
    uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
    return false
  }
  if (!formData.agree) {
    uni.showToast({ title: '请同意用户协议和隐私政策', icon: 'none' })
    return false
  }
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return

  try {
    loading.value = true

    const registerData = {
      username: formData.username,
      email: formData.email,
      phone: formData.phone || undefined,
      password: formData.password
    }

    await userStore.register(registerData)

    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })

    // 注册成功后跳转到登录页
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/login/index'
      })
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLogin = () => {
  uni.reLaunch({
    url: '/pages/login/index'
  })
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px;
  padding-top: 40px;

  &-back {
    width: 40px;
  }

  &-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  &-placeholder {
    width: 40px;
  }
}

.form-section {
  padding: 30px 20px;
}

.form-item {
  margin-bottom: 16px;

  .input-wrapper {
    display: flex;
    align-items: center;
    padding: 14px 18px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    backdrop-filter: blur(10px);

    .input {
      flex: 1;
      margin-left: 12px;
      font-size: 15px;
      color: #333;
    }

    .input-placeholder {
      color: #ccc;
    }
  }
}

.agreement-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;

  .checkbox {
    width: 20px;
    height: 20px;
    border: 2px solid rgba(255, 255, 255, 0.6);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 8px;
    flex-shrink: 0;
    transition: all 0.3s;

    &.checked {
      background-color: #3385ff;
      border-color: #3385ff;
    }
  }

  .agreement-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.5;

    .agreement-link {
      color: #fff;
      font-weight: 600;
    }
  }
}

.submit-btn {
  height: 50px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);

  .btn-text {
    font-size: 16px;
    font-weight: 600;
    color: #667eea;
  }

  &:active {
    opacity: 0.8;
  }
}

.footer {
  text-align: center;
  padding: 20px;

  .footer-text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }

  .footer-link {
    font-size: 14px;
    color: #fff;
    font-weight: 600;
    margin-left: 4px;
  }
}

.popup-container {
  width: 300px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .popup-content {
    height: 300px;
    padding: 20px;

    .content-text {
      font-size: 14px;
      color: #666;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .popup-footer {
    padding: 16px 20px;
    border-top: 1px solid #f0f0f0;

    .popup-btn {
      width: 100%;
      height: 44px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-size: 15px;
      border: none;
      border-radius: 8px;
    }
  }
}
</style>
