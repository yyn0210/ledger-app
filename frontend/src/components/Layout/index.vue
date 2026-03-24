<template>
  <n-layout has-sider class="layout">
    <n-layout-sider
      :collapsed="appStore.sidebarCollapsed"
      :collapsed-width="64"
      :width="240"
      show-trigger
      bordered
      @collapse="appStore.setSidebarCollapsed(true)"
      @expand="appStore.setSidebarCollapsed(false)"
    >
      <div class="logo">
        <span v-if="!appStore.sidebarCollapsed">智能记账</span>
        <span v-else>记账</span>
      </div>
      <n-menu
        :options="menuOptions"
        :collapsed="appStore.sidebarCollapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :default-value="route.name"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-left">
          <n-icon size="20" style="cursor: pointer" @click="toggleSidebar">
            <MenuOutline />
          </n-icon>
        </div>
        <div class="header-right">
          <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
            <div class="user-info">
              <n-avatar :size="32" round>
                <template #icon>
                  <PersonOutline />
                </template>
              </n-avatar>
              <span v-if="!appStore.sidebarCollapsed" class="username">{{ userStore.user?.nickname || '用户' }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>
      <n-layout-content class="content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { MenuOutline, PersonOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const toggleSidebar = () => {
  appStore.setSidebarCollapsed(!appStore.sidebarCollapsed)
}

const menuOptions = computed(() => [
  { label: '仪表盘', key: 'Dashboard', icon: () => '📊' },
  { label: '账本管理', key: 'Book', icon: () => '📒' },
  { label: '交易管理', key: 'Transaction', icon: () => '💰' },
  { label: '统计分析', key: 'Statistics', icon: () => '📈' },
  { label: '设置', key: 'Settings', icon: () => '⚙️' }
])

const userMenuOptions = computed(() => [
  { label: '个人中心', key: 'profile' },
  { label: '设置', key: 'settings' },
  { label: '退出登录', key: 'logout' }
])

const handleUserMenu = (key) => {
  if (key === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  } else if (key === 'settings') {
    router.push('/settings')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.logo {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 600;
  color: #3385ff;
  text-align: center;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.content {
  padding: 20px;
  background: #f5f6f7;
}
</style>
