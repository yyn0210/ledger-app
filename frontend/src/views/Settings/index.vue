<template>
  <div class="settings">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Settings" size="28" color="#3385ff" />
        设置
      </h1>
    </div>

    <div class="settings-list">
      <!-- 用户资料 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="Person" size="20" />
            <span>账户设置</span>
          </div>
        </template>
        
        <div class="profile-section">
          <div class="avatar-section">
            <n-avatar :size="80" :text="userInitials" :color="avatarColor" />
            <n-button size="small" @click="changeAvatar">
              <template #icon>
                <n-icon :component="Camera" />
              </template>
              更换头像
            </n-button>
          </div>
          <div class="profile-info">
            <n-form :model="profileForm" label-placement="top" label-width="100px">
              <n-form-item label="昵称">
                <n-input v-model:value="profileForm.nickname" placeholder="请输入昵称" />
              </n-form-item>
              <n-form-item label="邮箱">
                <n-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
              </n-form-item>
              <n-form-item label="手机号">
                <n-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
              </n-form-item>
            </n-form>
            <n-button type="primary" @click="saveProfile">保存资料</n-button>
          </div>
        </div>
      </n-card>

      <!-- 修改密码 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="LockClosed" size="20" />
            <span>安全设置</span>
          </div>
        </template>
        
        <n-form :model="passwordForm" label-placement="top" label-width="100px">
          <n-form-item label="当前密码">
            <n-input
              v-model:value="passwordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
            />
          </n-form-item>
          <n-form-item label="新密码">
            <n-input
              v-model:value="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
            />
          </n-form-item>
          <n-form-item label="确认密码">
            <n-input
              v-model:value="passwordForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
            />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="changePassword">修改密码</n-button>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 记账提醒 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="Notifications" size="20" />
            <span>记账提醒</span>
          </div>
        </template>
        
        <div class="setting-item">
          <div class="setting-label">
            <span>每日提醒</span>
            <span class="setting-desc">每天定时提醒你记账</span>
          </div>
          <n-switch v-model:value="settings.reminderEnabled" />
        </div>
        
        <div v-if="settings.reminderEnabled" class="setting-item">
          <div class="setting-label">
            <span>提醒时间</span>
            <span class="setting-desc">设置每日提醒时间</span>
          </div>
          <n-time-picker v-model:value="settings.reminderTime" format="HH:mm" style="width: 150px" />
        </div>
      </n-card>

      <!-- 主题设置 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="ColorPalette" size="20" />
            <span>主题设置</span>
          </div>
        </template>
        
        <div class="setting-item">
          <div class="setting-label">
            <span>深色模式</span>
            <span class="setting-desc">切换深色/浅色主题</span>
          </div>
          <n-switch v-model:value="isDark" @update:value="toggleTheme">
            <template #checked>
              深色
            </template>
            <template #unchecked>
              浅色
            </template>
          </n-switch>
        </div>
      </n-card>

      <!-- 数据导出 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="Download" size="20" />
            <span>数据导出</span>
          </div>
        </template>
        
        <div class="setting-item">
          <div class="setting-label">
            <span>导出格式</span>
            <span class="setting-desc">选择导出文件格式</span>
          </div>
          <n-select
            v-model:value="exportFormat"
            :options="exportFormatOptions"
            style="width: 150px"
          />
        </div>
        
        <div class="setting-item">
          <div class="setting-label">
            <span>导出时间范围</span>
            <span class="setting-desc">选择导出的时间范围</span>
          </div>
          <n-date-picker
            v-model:value="exportDateRange"
            type="daterange"
            style="width: 250px"
          />
        </div>
        
        <n-button type="primary" @click="exportData">
          <template #icon>
            <n-icon :component="Download" />
          </template>
          导出数据
        </n-button>
      </n-card>

      <!-- 关于 -->
      <n-card class="settings-section">
        <template #header>
          <div class="section-header">
            <n-icon :component="InformationCircle" size="20" />
            <span>关于</span>
          </div>
        </template>
        
        <div class="about-info">
          <div class="app-logo">
            <n-icon :component="Wallet" size="48" color="#3385ff" />
          </div>
          <h3 class="app-name">智能记账</h3>
          <p class="app-version">版本：v1.0.0</p>
          <p class="app-desc">让理财更简单</p>
        </div>
      </n-card>

      <!-- 退出登录 -->
      <n-card class="settings-section danger">
        <div class="setting-item">
          <div class="setting-label">
            <span class="danger-text">退出登录</span>
            <span class="setting-desc">退出当前账号</span>
          </div>
          <n-button type="error" @click="confirmLogout">退出登录</n-button>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Settings, Person, LockClosed, Notifications, ColorPalette,
  Download, InformationCircle, Wallet, Camera
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const isDark = ref(false)

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
  reminderEnabled: false,
  reminderTime: new Date().setHours(21, 0, 0)
})

const exportFormat = ref('excel')
const exportFormatOptions = [
  { label: 'Excel', value: 'excel' },
  { label: 'CSV', value: 'csv' },
  { label: 'PDF', value: 'pdf' }
]
const exportDateRange = ref([Date.now() - 7 * 24 * 60 * 60 * 1000, Date.now()])

const userInitials = computed(() => {
  const name = profileForm.nickname || '用户'
  return name.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  const colors = ['#3385ff', '#52c41a', '#ff9900', '#9b59b6', '#e74c3c']
  const index = (profileForm.nickname || '').length % colors.length
  return colors[index]
})

const saveProfile = () => {
  // TODO: 调用 API 保存资料
  message.success('资料已保存')
}

const changePassword = () => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    message.error('请填写完整的密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    message.error('密码长度至少 6 位')
    return
  }
  
  // TODO: 调用 API 修改密码
  message.success('密码已修改')
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const changeAvatar = () => {
  message.info('头像上传功能开发中')
}

const toggleTheme = (value) => {
  // TODO: 实现主题切换
  message.success(`已切换到${value ? '深色' : '浅色'}模式`)
}

const exportData = () => {
  message.info(`导出 ${exportFormat.value.toUpperCase()} 格式数据，功能开发中`)
}

const confirmLogout = () => {
  dialog.warning({
    title: '确认退出',
    content: '确定要退出登录吗？',
    positiveText: '确定退出',
    negativeText: '取消',
    onPositiveClick: async () => {
      await userStore.logout()
      message.success('已退出登录')
      // 跳转到登录页
      window.location.href = '/login'
    }
  })
}
</script>

<style scoped>
.settings {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-section {
  transition: all 0.3s;
  
  &.danger {
    border-color: #ff6b6b;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.profile-section {
  display: flex;
  gap: 30px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.profile-info {
  flex: 1;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .setting-desc {
    font-size: 12px;
    color: #999;
  }
}

.about-info {
  text-align: center;
  padding: 20px;
  
  .app-logo {
    margin-bottom: 16px;
  }
  
  .app-name {
    font-size: 20px;
    font-weight: 700;
    color: #333;
    margin: 0 0 8px 0;
  }
  
  .app-version {
    font-size: 14px;
    color: #999;
    margin: 0 0 4px 0;
  }
  
  .app-desc {
    font-size: 13px;
    color: #ccc;
    margin: 0;
  }
}

.danger-text {
  color: #ff6b6b;
  font-weight: 600;
}
</style>
