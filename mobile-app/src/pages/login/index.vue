<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="background">
      <view class="bg-circle circle-1"></view>
      <view class="bg-circle circle-2"></view>
      <view class="bg-circle circle-3"></view>
    </view>

    <!-- Logo 区域 -->
    <view class="logo-section">
      <view class="logo">
        <u-icon name="account" size="60" color="#fff"></u-icon>
      </view>
      <text class="app-name">智能记账</text>
      <text class="app-slogan">让理财更简单</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-section">
      <view class="form-item">
        <view class="input-wrapper">
          <u-icon name="account" size="20" color="#999"></u-icon>
          <input
            class="input"
            v-model="formData.username"
            placeholder="请输入用户名/手机号/邮箱"
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
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <view class="form-options">
        <view class="checkbox-wrapper" @click="toggleRemember">
          <view class="checkbox" :class="{ checked: rememberMe }">
            <u-icon v-if="rememberMe" name="checkmark" size="14" color="#fff"></u-icon>
          </view>
          <text class="checkbox-label">记住我</text>
        </view>
        <text class="forgot-password" @click="handleForgotPassword">忘记密码？</text>
      </view>

      <view class="submit-btn" @click="handleLogin">
        <text class="btn-text">{{ loading ? '登录中...' : '登 录' }}</text>
      </view>

      <!-- 其他登录方式 -->
      <view class="other-login">
        <text class="other-text">其他登录方式</text>
        <view class="other-icons">
          <view class="other-icon" @click="handleWechatLogin">
            <u-icon name="wechat-fill" size="28" color="#07c160"></u-icon>
          </view>
          <view class="other-icon" @click="handleAlipayLogin">
            <u-icon name="alipay" size="28" color="#1677ff"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部注册提示 -->
    <view class="footer">
      <text class="footer-text">还没有账号？</text>
      <text class="footer-link" @click="handleRegister">立即注册</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const rememberMe = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const toggleRemember = () => {
  rememberMe.value = !rememberMe.value
}

const handleLogin = async () => {
  if (!formData.username.trim()) {
    uni.showToast({
      title: '请输入用户名',
      icon: 'none'
    })
    return
  }

  if (!formData.password) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  try {
    loading.value = true
    await userStore.login(formData)
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })

    // 如果选择记住我，设置长期 token
    if (rememberMe.value) {
      uni.setStorageSync('token_expire', '30d')
    }

    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 1500)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  uni.navigateTo({
    url: '/pages/register/index'
  })
}

const handleForgotPassword = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleWechatLogin = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const handleAlipayLogin = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 30px 30px;
  display: flex;
  flex-direction: column;
}

.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  left: -50px;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: 100px;
  right: -30px;
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 100px;
  right: 50px;
}

.logo-section {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 50px;

  .logo {
    width: 100px;
    height: 100px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
    backdrop-filter: blur(10px);
  }

  .app-name {
    display: block;
    font-size: 32px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
  }

  .app-slogan {
    display: block;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

.form-section {
  position: relative;
  z-index: 1;
  flex: 1;
}

.form-item {
  margin-bottom: 20px;

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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;

  .checkbox-wrapper {
    display: flex;
    align-items: center;

    .checkbox {
      width: 20px;
      height: 20px;
      border: 2px solid rgba(255, 255, 255, 0.6);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 8px;
      transition: all 0.3s;

      &.checked {
        background-color: #3385ff;
        border-color: #3385ff;
      }
    }

    .checkbox-label {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .forgot-password {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
  }
}

.submit-btn {
  height: 50px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
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

.other-login {
  text-align: center;

  .other-text {
    display: block;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
    margin-bottom: 16px;
  }

  .other-icons {
    display: flex;
    justify-content: center;
    gap: 30px;
  }

  .other-icon {
    width: 50px;
    height: 50px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
  }
}

.footer {
  position: relative;
  z-index: 1;
  text-align: center;
  padding-top: 20px;

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
</style>
