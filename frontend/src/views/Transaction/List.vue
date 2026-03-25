<template>
  <div class="transaction-list">
    <n-card>
      <n-space justify="space-between">
        <span>交易列表</span>
        <n-button type="primary" @click="handleCreate">新建交易</n-button>
      </n-space>
    </n-card>
    <n-card class="table-card">
      <n-data-table :columns="columns" :data="tableData" :pagination="pagination" />
    </n-card>
  </div>
</template>

<script setup>
import { h } from 'vue'
import { useRouter } from 'vue-router'
import { NTag, NButton } from 'naive-ui'

const router = useRouter()

const renderTag = type => {
  const typeMap = { income: { type: 'success', content: '收入' }, expense: { type: 'error', content: '支出' } }
  return h(NTag, { type: typeMap[type].type }, { default: () => typeMap[type].content })
}

const renderActions = row => h(NButton, { text: true, type: 'primary', onClick: () => handleEdit(row) }, { default: () => '编辑' })

const columns = [
  { title: '日期', key: 'date', width: 120 },
  { title: '类型', key: 'type', width: 80, render: row => renderTag(row.type) },
  { title: '分类', key: 'category', width: 100 },
  { title: '金额', key: 'amount', width: 100, render: row => `¥${row.amount}` },
  { title: '备注', key: 'note' },
  { title: '操作', key: 'actions', width: 80, render: row => renderActions(row) }
]

const tableData = [
  { id: 1, date: '2026-03-22', type: 'expense', category: '餐饮', amount: '56.00', note: '午餐' },
  { id: 2, date: '2026-03-22', type: 'expense', category: '交通', amount: '20.00', note: '地铁' },
  { id: 3, date: '2026-03-21', type: 'income', category: '工资', amount: '10000.00', note: '3 月工资' }
]

const pagination = { page: 1, pageSize: 10, showSizePicker: true }

const handleCreate = () => router.push('/transaction/create')
const handleEdit = row => router.push(`/transaction/edit/${row.id}`)
</script>

<style scoped>
.transaction-list { display: flex; flex-direction: column; gap: 20px; }
.table-card { min-height: 500px; }
</style>
