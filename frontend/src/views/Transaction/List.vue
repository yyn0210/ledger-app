<template>
  <div class="transaction-list">
    <n-card>
<<<<<<< HEAD
      <n-space justify="space-between">
        <span>交易列表</span>
        <n-button type="primary" @click="handleCreate">新建交易</n-button>
      </n-space>
    </n-card>
    <n-card class="table-card">
      <n-data-table :columns="columns" :data="tableData" :pagination="pagination" />
=======
      <div class="filter-bar">
        <n-space>
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            placeholder="选择日期范围"
          />
          <n-select
            v-model:value="filterType"
            :options="typeOptions"
            placeholder="交易类型"
            clearable
            style="width: 120px"
          />
          <n-select
            v-model:value="filterCategory"
            :options="categoryOptions"
            placeholder="分类"
            clearable
            style="width: 120px"
          />
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索备注"
            clearable
            style="width: 200px"
          />
          <n-button type="primary" @click="handleSearch">搜索</n-button>
          <n-button @click="handleReset">重置</n-button>
        </n-space>
      </div>
    </n-card>

    <n-card class="table-card">
      <template #header>
        <n-space justify="space-between">
          <span>交易列表</span>
          <n-button type="primary" @click="handleCreate">
            <template #icon>
              <n-icon><PlusOutlined /></n-icon>
            </template>
            新建交易
          </n-button>
        </n-space>
      </template>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
      />
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
    </n-card>
  </div>
</template>

<script setup>
<<<<<<< HEAD
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
=======
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NTag, NButton, NIcon } from 'naive-ui'
import { PlusOutlined } from '@vicons/antd'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const dateRange = ref(null)
const filterType = ref(null)
const filterCategory = ref(null)
const searchKeyword = ref('')

const typeOptions = [
  { label: '收入', value: 'income' },
  { label: '支出', value: 'expense' }
]

const categoryOptions = [
  { label: '餐饮', value: 'food' },
  { label: '交通', value: 'transport' },
  { label: '购物', value: 'shopping' },
  { label: '娱乐', value: 'entertainment' },
  { label: '工资', value: 'salary' },
  { label: '其他', value: 'other' }
]

const renderTag = type => {
  const typeMap = {
    income: { type: 'success', content: '收入' },
    expense: { type: 'error', content: '支出' }
  }
  const config = typeMap[type] || { type: 'default', content: type }
  return h(NTag, { type: config.type }, { default: () => config.content })
}

const renderActions = row => {
  return h(NButton, {
    text: true,
    type: 'primary',
    onClick: () => handleEdit(row)
  }, { default: () => '编辑' })
}

const columns = [
  {
    title: '日期',
    key: 'date',
    width: 120,
    sorter: 'default'
  },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: row => renderTag(row.type)
  },
  {
    title: '分类',
    key: 'category',
    width: 100
  },
  {
    title: '金额',
    key: 'amount',
    width: 100,
    sorter: 'default',
    render: row => `¥${row.amount}`
  },
  {
    title: '账本',
    key: 'bookName',
    width: 120
  },
  {
    title: '备注',
    key: 'note',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: row => renderActions(row)
  }
]

const tableData = ref([
  {
    id: 1,
    date: '2026-03-22',
    type: 'expense',
    category: '餐饮',
    amount: '56.00',
    bookName: '日常账本',
    note: '午餐'
  },
  {
    id: 2,
    date: '2026-03-22',
    type: 'expense',
    category: '交通',
    amount: '20.00',
    bookName: '日常账本',
    note: '地铁'
  },
  {
    id: 3,
    date: '2026-03-21',
    type: 'income',
    category: '工资',
    amount: '10000.00',
    bookName: '日常账本',
    note: '3 月工资'
  }
])

const pagination = {
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: page => {
    pagination.page = page
  },
  onUpdatePageSize: pageSize => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
}

const handleSearch = () => {
  message.success('搜索功能开发中')
}

const handleReset = () => {
  dateRange.value = null
  filterType.value = null
  filterCategory.value = null
  searchKeyword.value = ''
}

const handleCreate = () => {
  router.push('/transaction/create')
}

const handleEdit = row => {
  router.push(`/transaction/edit/${row.id}`)
}
</script>

<style scoped>
.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-bar {
  padding: 10px 0;
}

.table-card {
  min-height: 500px;
}
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
</style>
