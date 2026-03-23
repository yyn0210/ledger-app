<template>
  <view class="settings">
    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">设置</text>
    </view>

    <scroll-view class="settings-list" scroll-y>
      <!-- 用户资料卡片 -->
      <view class="profile-card">
        <view class="profile-header">
          <view class="avatar-wrapper">
            <view class="avatar" :style="{ backgroundColor: avatarColor }">
              <text class="avatar-text">{{ userInitials }}</text>
            </view>
            <view class="avatar-badge" @click="changeAvatar">
              <u-icon name="camera" size="16" color="#fff"></u-icon>
            </view>
          </view>
          <view class="profile-info">
            <text class="nickname">{{ profileForm.nickname || '未设置昵称' }}</text>
            <text class="user-id">ID: {{ userId }}</text>
          </view>
        </view>
      </view>

      <!-- 账户设置 -->
      <view class="settings-group">
        <view class="group-header">
          <u-icon name="account" size="18" color="#3385ff"></u-icon>
          <text class="group-title">账户设置</text>
        </view>
        
        <view class="setting-item" @click="showEditProfile = true">
          <view class="item-label">
            <text class="item-text">个人资料</text>
          </view>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
        
        <view class="setting-item" @click="showChangePassword = true">
          <view class="item-label">
            <text class="item-text">修改密码</text>
          </view>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 记账提醒 -->
      <view class="settings-group">
        <view class="group-header">
          <u-icon name="clock" size="18" color="#3385ff"></u-icon>
          <text class="group-title">记账提醒</text>
        </view>
        
        <view class="setting-item">
          <view class="item-label">
            <text class="item-text">每日提醒</text>
            <text class="item-desc">每天定时提醒你记账</text>
          </view>
          <u-switch v-model="settings.reminderEnabled" @change="onReminderChange"></u-switch>
        </view>
        
        <view v-if="settings.reminderEnabled" class="setting-item">
          <view class="item-label">
            <text class="item-text">提醒时间</text>
            <text class="item-desc">设置每日提醒时间</text>
          </view>
          <picker mode="time" :value="reminderTimeText" @change="onTimeChange">
            <view class="picker-value">
              <text>{{ reminderTimeText }}</text>
              <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
            </view>
          </picker>
        </view>
      </view>

      <!-- 主题设置 -->
      <view class="settings-group">
        <view class="group-header">
          <u-icon name="theme" size="18" color="#3385ff"></u-icon>
          <text class="group-title">主题设置</text>
        </view>
        
        <view class="setting-item">
          <view class="item-label">
            <text class="item-text">深色模式</text>
            <text class="item-desc">切换深色/浅色主题</text>
          </view>
          <u-switch v-model="isDark" @change="toggleTheme"></u-switch>
        </view>
      </view>

      <!-- 数据导出 -->
      <view class="settings-group">
        <view class="group-header">
          <u-icon name="download" size="18" color="#3385ff"></u-icon>
          <text class="group-title">数据导出</text>
        </view>
        
        <view class="setting-item" @click="showExportOptions = true">
          <view class="item-label">
            <text class="item-text">导出数据</text>
            <text class="item-desc">导出记账记录为 Excel/CSV</text>
          </view>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 关于 -->
      <view class="settings-group">
        <view class="group-header">
          <u-icon name="info-circle" size="18" color="#3385ff"></u-icon>
          <text class="group-title">关于</text>
        </view>
        
        <view class="setting-item">
          <view class="item-label">
            <text class="item-text">版本信息</text>
          </view>
          <text class="item-value">v1.0.0</text>
        </view>
        
        <view class="setting-item">
          <view class="item-label">
            <text class="item-text">关于我们</text>
          </view>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="settings-group danger-group">
        <view class="setting-item danger" @click="confirmLogout">
          <view class="item-label">
            <text class="item-text danger-text">退出登录</text>
          </view>
          <u-icon name="arrow-right" size="16" color="#ff6b6b"></u-icon>
        </view>
      </view>

      <!-- 底部间距 -->
      <view style="height: 30px;"></view>
    </scroll-view>

    <!-- 编辑资料弹窗 -->
    <u-popup v-model="showEditProfile" mode="center" :round="16">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">编辑资料</text>
          <u-icon name="close" size="24" @click="showEditProfile = false"></u-icon>
        </view>
        <view class="modal-content">
          <view class="form-item">
            <text class="form-label">昵称</text>
            <input class="form-input" v-model="profileForm.nickname" placeholder="请输入昵称" />
          </view>
          <view class="form-item">
            <text class="form-label">邮箱</text>
            <input class="form-input" v-model="profileForm.email" type="email" placeholder="请输入邮箱" />
          </view>
          <view class="form-item">
            <text class="form-label">手机号</text>
            <input class="form-input" v-model="profileForm.phone" type="number" placeholder="请输入手机号" />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="showEditProfile = false">取消</button>
          <button class="modal-btn confirm" @click="saveProfile">保存</button>
        </view>
      </view>
    </u-popup>

    <!-- 修改密码弹窗 -->
    <u-popup v-model="showChangePassword" mode="center" :round="16">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <u-icon name="close" size="24" @click="showChangePassword = false"></u-icon>
        </view>
        <view class="modal-content">
          <view class="form-item">
            <text class="form-label">当前密码</text>
            <input class="form-input" v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" />
          </view>
          <view class="form-item">
            <text class="form-label">新密码</text>
            <input class="form-input" v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
          </view>
          <view class="form-item">
            <text class="form-label">确认密码</text>
            <input class="form-input" v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="showChangePassword = false">取消</button>
          <button class="modal-btn confirm" @click="changePassword">确定</button>
        </view>
      </view>
    </u-popup>

    <!-- 导出选项弹窗 -->
    <u-popup v-model="showExportOptions" mode="bottom" :round="20">
      <view class="export-modal">
        <view class="export-header">
          <text class="export-title">导出数据</text>
        </view>
        <view class="export-options">
          <view class="export-option" @click="exportData('excel')">
            <u-icon name="file-text" size="24" color="#52c41a"></u-icon>
            <text class="option-text">导出为 Excel</text>
          </view>
          <view class="export-option" @click="exportData('csv')">
            <u-icon name="file-text" size="24" color="#3385ff"></u-icon>
            <text class="option-text">导出为 CSV</text>
          </view>
        </view>
        <view class="export-cancel" @click="showExportOptions = false">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/book'

const userStore = useUserStore()

const isDark = ref(false)
const showEditProfile = ref(false)
const showChangePassword = ref(false)
const showExportOptions = ref(false)

const profileForm = reactive({
  nickname: userStore.userInfo?.nickname || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const settings = reactive({
  reminderEnabled: false
})

const reminderTimeText = ref('21:00')

const userId = computed(() => {
  return userStore.userInfo?.id || '10001'
})

const userInitials = computed(() => {
  const name = profileForm.nickname || '用户'
  return name.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  const colors = ['#3385ff', '#52c41a', '#ff9900', '#9b59b6', '#e74c3c']
  const index = (profileForm.nickname || '').length % colors.length
  return colors[index]
})

const onReminderChange = (value) => {
  uni.showToast({
    title: value ? '已开启提醒' : '已关闭提醒',
    icon: 'none'
  })
}

const onTimeChange = (e) => {
  reminderTimeText.value = e.detail.value
}

const toggleTheme = (value) => {
  uni.showToast({
    title: `已切换到${value ? '深色' : '浅色'}模式`,
    icon: 'none'
  })
}

const saveProfile = () => {
  // TODO: 调用 API 保存资料
  uni.showToast({
    title: '资料已保存',
    icon: 'success'
  })
  showEditProfile.value = false
}

const changePassword = () => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    uni.showToast({
      title: '请填写完整的密码信息',
      icon: 'none'
    })
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }
  if (passwordForm.newPassword.length < 6) {
    uni.showToast({
      title: '密码长度至少 6 位',
      icon: 'none'
    })
    return
  }
  
  // TODO: 调用 API 修改密码
  uni.showToast({
    title: '密码已修改',
    icon: 'success'
  })
  showChangePassword.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const changeAvatar = () => {
  uni.showToast({
    title: '头像上传功能开发中',
    icon: 'none'
  })
}

const exportData = (format) => {
  uni.showToast({
    title: `导出 ${format.toUpperCase()} 格式数据，功能开发中`,
    icon: 'none'
  })
  showExportOptions.value = false
}

const confirmLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    confirmText: '退出',
    confirmColor: '#ff6b6b',
    success: async (res) => {
      if (res.confirm) {
        await userStore.logout()
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
        // 跳转到登录页
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/login/index'
          })
        }, 1500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.settings {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.navbar {
  padding: 20px 16px;
  padding-top: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  &-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
}

.settings-list {
  height: calc(100vh - 100px);
}

.profile-card {
  margin: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  
  .profile-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .avatar-wrapper {
    position: relative;
    
    .avatar {
      width: 70px;
      height: 70px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      font-weight: 700;
      color: #fff;
    }
    
    .avatar-badge {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 28px;
      height: 28px;
      background: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
  }
  
  .profile-info {
    flex: 1;
    
    .nickname {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 6px;
    }
    
    .user-id {
      display: block;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

.settings-group {
  margin: 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  
  .group-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 16px;
    background: #fafafa;
    border-bottom: 1px solid #f0f0f0;
    
    .group-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.danger {
    .item-text {
      color: #ff6b6b;
    }
  }
  
  .item-label {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .item-text {
      font-size: 15px;
      color: #333;
      
      &.danger-text {
        color: #ff6b6b;
      }
    }
    
    .item-desc {
      font-size: 12px;
      color: #999;
    }
  }
  
  .item-value {
    font-size: 14px;
    color: #999;
  }
  
  .picker-value {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #999;
  }
}

.danger-group {
  margin-bottom: 30px;
}

.modal-container {
  width: 320px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    .modal-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .modal-content {
    padding: 20px;
  }
  
  .modal-footer {
    display: flex;
    border-top: 1px solid #f0f0f0;
    
    .modal-btn {
      flex: 1;
      height: 48px;
      border: none;
      font-size: 15px;
      
      &.cancel {
        background: #f5f6f7;
        color: #666;
        border-radius: 0 0 0 16px;
      }
      
      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-radius: 0 0 16px 0;
      }
    }
  }
}

.form-item {
  margin-bottom: 16px;
  
  .form-label {
    display: block;
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }
  
  .form-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
  }
}

.export-modal {
  background: #fff;
  border-radius: 20px 20px 0 0;
  
  .export-header {
    padding: 20px;
    text-align: center;
    border-bottom: 1px solid #f0f0f0;
    
    .export-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .export-options {
    padding: 10px 0;
    
    .export-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      
      &:active {
        background: #f5f6f7;
      }
      
      .option-text {
        font-size: 15px;
        color: #333;
      }
    }
  }
  
  .export-cancel {
    padding: 16px;
    text-align: center;
    border-top: 1px solid #f0f0f0;
    
    .cancel-text {
      font-size: 15px;
      color: #666;
    }
  }
}
</style>
