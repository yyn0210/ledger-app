<template>
  <div class="transaction-list">
    <div class="page-header">
      <h2>交易记录</h2>
      <n-button type="primary" @click="router.push('/transaction/create')">
        <template #icon>➕</template>
        新建交易
      </n-button>
    </div>

    <n-card>
      <n-table :data="transactions" :columns="columns">
        <template #empty>
          <n-empty description="暂无交易记录" />
        </template>
      </n-table>
    </n-card>
  </div>
</template>

<script setup>
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { NTag } from 'naive-ui'

const router = useRouter()

const transactions = ref([
  { id: 1, type: 'expense', amount: 68, categoryName: '餐饮', date: '2026-03-23', note: '午餐' },
  { id: 2, type: 'expense', amount: 5, categoryName: '交通', date: '2026-03-23', note: '地铁' },
  { id: 3, type: 'income', amount: 8500, categoryName: '工资', date: '2026-03-01', note: '3 月工资' }
])

const columns = [
  { title: '日期', key: 'date' },
  { title: '分类', key: 'categoryName' },
  { title: '备注', key: 'note' },
  {
    title: '金额',
    key: 'amount',
    render: (row) => {
      const type = row.type === 'income' ? 'success' : 'error'
      const sign = row.type === 'income' ? '+' : '-'
      return h(NTag, { type }, { default: () => `${sign}¥${row.amount}` })
    }
  }
]
</script>

<style scoped>
.transaction-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
  }
}
</style>
