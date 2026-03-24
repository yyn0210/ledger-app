<template>
  <div class="book-management">
    <n-card>
      <n-space justify="space-between">
        <span>账本列表</span>
        <n-button type="primary">新建账本</n-button>
      </n-space>
    </n-card>
    <n-card>
      <n-grid :cols="3" :x-gap="20" :y-gap="20">
        <n-grid-item v-for="book in bookList" :key="book.id">
          <n-card :title="book.name" hoverable>
            <p>创建时间：{{ book.createdAt }}</p>
            <p>交易数：{{ book.transactionCount || 0 }}</p>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const bookList = ref([
  { id: 1, name: '日常账本', createdAt: '2026-03-01', transactionCount: 156 },
  { id: 2, name: '旅行账本', createdAt: '2026-02-15', transactionCount: 45 },
  { id: 3, name: '装修账本', createdAt: '2026-01-10', transactionCount: 89 }
])
</script>

<style scoped>
.book-management { display: flex; flex-direction: column; gap: 20px; }
</style>
