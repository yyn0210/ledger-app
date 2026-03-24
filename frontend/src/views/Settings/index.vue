<template>
  <div class="settings">
    <n-card title="设置">
      <n-space vertical>
        <n-button @click="message.success('功能开发中')">修改密码</n-button>
        <n-button @click="message.success('功能开发中')">通知设置</n-button>
        <n-button @click="message.success('功能开发中')">数据导出</n-button>
        <n-button type="error" @click="handleLogout">退出登录</n-button>
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'

const message = useMessage()
const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.settings {
  max-width: 600px;
  margin: 0 auto;
}
</style>
