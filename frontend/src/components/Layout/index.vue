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
      <div class="logo">{{ appStore.sidebarCollapsed ? '记账' : '智能记账' }}</div>
      <n-menu
        :options="menuOptions"
        :collapsed="appStore.sidebarCollapsed"
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
              <n-avatar :size="32" round>👤</n-avatar>
              <span class="username">{{ userStore.nickname }}</span>
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
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { MenuOutline } from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const renderIcon = icon => () => h(NIcon, null, { default: () => h(icon) })

const menuOptions = computed(() => [
  { label: '仪表盘', key: 'Dashboard', icon: renderIcon(MenuOutlined), onClick: () => router.push('/dashboard') },
  { label: '交易管理', key: 'Transaction', icon: renderIcon(MenuOutlined), onClick: () => router.push('/transaction') },
  { label: '账本管理', key: 'Book', icon: renderIcon(MenuOutlined), onClick: () => router.push('/book') },
  { label: '统计分析', key: 'Statistics', icon: renderIcon(MenuOutlined), onClick: () => router.push('/statistics') },
  { label: '设置', key: 'Settings', icon: renderIcon(MenuOutlined), onClick: () => router.push('/settings') }
])

const userMenuOptions = computed(() => [
  { label: '个人设置', key: 'profile' },
  { label: '退出登录', key: 'logout' }
])

const toggleSidebar = () => appStore.toggleSidebar()

const handleUserMenu = key => {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.logo {
  display: flex; justify-content: center; align-items: center;
  height: 64px; font-size: 18px; font-weight: bold;
  color: var(--primary-color); border-bottom: 1px solid var(--border-color);
}
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 20px; height: 64px;
}
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.content { padding: 20px; background-color: #f5f7f9; }
</style>
