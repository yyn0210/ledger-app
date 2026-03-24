<template>
  <div class="recurring-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Repeat" size="28" color="#8b5cf6" />
        周期账单
      </h1>
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="Add" />
        </template>
        新建周期账单
      </n-button>
    </div>

    <!-- 统计卡片 -->
    <n-grid :cols="3" :x-gap="16" class="stats-grid">
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f3e8ff;">
              <n-icon :component="Repeat" size="24" color="#8b5cf6" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ recurringStore.list.length }}</div>
              <div class="stat-label">周期账单</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #dcfce7;">
              <n-icon :component="TrendingUp" size="24" color="#10b981" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ monthlyTotal }}</div>
              <div class="stat-label">本月待执行</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef3c7;">
              <n-icon :component="Calendar" size="24" color="#f59e0b" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayCount }}</div>
              <div class="stat-label">今日待执行</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 周期账单列表 -->
    <n-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>周期账单列表</span>
          <n-space>
            <n-radio-group v-model:value="filterType" size="small">
              <n-radio-button value="all">全部</n-radio-button>
              <n-radio-button value="active">进行中</n-radio-button>
              <n-radio-button value="completed">已完成</n-radio-button>
            </n-radio-group>
          </n-space>
        </div>
      </template>

      <n-data-table
        :columns="columns"
        :data="filteredList"
        :loading="recurringStore.loading"
        :pagination="false"
        striped
      />

      <n-empty
        v-if="filteredList.length === 0 && !recurringStore.loading"
        description="暂无周期账单，点击右上角新建"
        size="large"
        style="padding: 40px 0"
      />
    </n-card>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      preset="card"
      :title="editingId ? '编辑周期账单' : '新建周期账单'"
      style="width: 600px"
      :closable="true"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="账单名称" path="name">
          <n-input v-model:value="formData.name" placeholder="如：每月房租" />
        </n-form-item>

        <n-form-item label="交易类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="expense">支出</n-radio>
            <n-radio value="income">收入</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            placeholder="请输入金额"
            :min="0.01"
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item label="分类" path="categoryId">
          <n-select
            v-model:value="formData.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>

        <n-form-item label="周期类型" path="frequency">
          <n-select
            v-model:value="formData.frequency"
            :options="frequencyOptions"
            placeholder="请选择周期"
          />
        </n-form-item>

        <n-form-item label="执行日期" path="executionDate">
          <n-date-picker
            v-model:value="formData.executionDate"
            type="datetime"
            placeholder="选择首次执行日期"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-switch v-model:value="formData.status" :checked-value="'active'" :unchecked-value="'paused'" />
          <span style="margin-left: 12px; color: #6b7280;">
            {{ formData.status === 'active' ? '启用' : '暂停' }}
          </span>
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            确认
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 执行记录弹窗 -->
    <n-modal
      v-model:show="showHistoryModal"
      preset="card"
      title="执行记录"
      style="width: 600px"
      :closable="true"
    >
      <n-timeline>
        <n-timeline-item
          v-for="record in executionHistory"
          :key="record.id"
          :type="record.success ? 'success' : 'error'"
          :title="record.date"
          :content="record.note || '自动执行'"
          :time="`金额：¥${record.amount}`"
        />
      </n-timeline>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useMessage, NButton, NTag, NIcon } from 'naive-ui'
import {
  Repeat, Add, TrendingUp, Calendar, CreateOutline as Edit, Trash, Time as History, Play
} from '@vicons/ionicons5'
import { useRecurringStore } from '@/stores/recurring'
import { getCategoryList } from '@/api/category'
import { deleteRecurring, executeRecurring, getExecutionHistory } from '@/api/recurring'

const message = useMessage()
const recurringStore = useRecurringStore()

const showCreateModal = ref(false)
const showHistoryModal = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const filterType = ref('all')
const categories = ref({ expense: [], income: [] })
const executionHistory = ref([])

const formData = reactive({
  name: '',
  type: 'expense',
  amount: null,
  categoryId: null,
  frequency: 'monthly',
  executionDate: Date.now(),
  note: '',
  status: 'active'
})

const formRules = {
  name: { required: true, message: '请输入账单名称', trigger: 'blur' },
  amount: { required: true, message: '请输入金额', trigger: 'blur' },
  categoryId: { required: true, message: '请选择分类', trigger: 'change' },
  frequency: { required: true, message: '请选择周期', trigger: 'change' },
  executionDate: { required: true, message: '请选择执行日期', trigger: 'change' }
}

const frequencyOptions = [
  { label: '每日', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
  { label: '每年', value: 'yearly' }
]

const categoryOptions = computed(() => {
  const list = formData.type === 'income' ? categories.value.income : categories.value.expense
  return (list || []).map(c => ({ label: c.name, value: c.id }))
})

const filteredList = computed(() => {
  if (filterType.value === 'all') return recurringStore.list
  return recurringStore.list.filter(r => r.status === filterType.value)
})

const monthlyTotal = computed(() => {
  const now = new Date()
  const thisMonth = now.getMonth()
  return recurringStore.list.filter(r => {
    if (r.status !== 'active') return false
    const nextDate = new Date(r.nextDate)
    return nextDate.getMonth() === thisMonth && nextDate.getFullYear() === now.getFullYear()
  }).length
})

const todayCount = computed(() => {
  const today = new Date().toDateString()
  return recurringStore.list.filter(r => {
    if (r.status !== 'active') return false
    return new Date(r.nextDate).toDateString() === today
  }).length
})

const columns = [
  {
    title: '账单名称',
    key: 'name',
    width: 180,
    render: (row) => h('div', { style: 'font-weight: 600;' }, row.name)
  },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: (row) => h(NTag, {
      type: row.type === 'income' ? 'success' : 'error',
      size: 'small'
    }, { default: () => row.type === 'income' ? '收入' : '支出' })
  },
  {
    title: '金额',
    key: 'amount',
    width: 100,
    render: (row) => h('span', { style: 'font-weight: 600; color: #059669;' }, `¥${row.amount}`)
  },
  {
    title: '周期',
    key: 'frequency',
    width: 80,
    render: (row) => {
      const map = { daily: '每日', weekly: '每周', monthly: '每月', yearly: '每年' }
      return map[row.frequency] || row.frequency
    }
  },
  {
    title: '下次执行',
    key: 'nextDate',
    width: 120,
    render: (row) => new Date(row.nextDate).toLocaleDateString('zh-CN')
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => h(NTag, {
      type: row.status === 'active' ? 'success' : 'default',
      size: 'small'
    }, { default: () => row.status === 'active' ? '启用' : '暂停' })
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
      h(NButton, {
        size: 'small',
        type: 'primary',
        onClick: () => handleEdit(row)
      }, { default: () => '编辑' }),
      h(NButton, {
        size: 'small',
        onClick: () => handleExecute(row)
      }, {
        icon: () => h(NIcon, { component: Play }),
        default: () => '执行'
      }),
      h(NButton, {
        size: 'small',
        onClick: () => handleHistory(row)
      }, {
        icon: () => h(NIcon, { component: History }),
        default: () => '记录'
      }),
      h(NButton, {
        size: 'small',
        type: 'error',
        onClick: () => handleDelete(row)
      }, {
        icon: () => h(NIcon, { component: Trash }),
        default: () => ''
      })
    ])
  }
]

onMounted(async () => {
  await loadCategories()
  await recurringStore.fetchList()
})

const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    const defaults = data.defaults || []
    categories.value.expense = defaults.filter(c => c.type === 'expense')
    categories.value.income = defaults.filter(c => c.type === 'income')
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(formData, { ...row })
  showCreateModal.value = true
}

const handleDelete = async (row) => {
  await deleteRecurring(row.id)
  message.success('删除成功')
}

const handleExecute = async (row) => {
  await executeRecurring(row.id)
  message.success('执行成功')
}

const handleHistory = async (row) => {
  const res = await getExecutionHistory(row.id)
  executionHistory.value = res.data || []
  showHistoryModal.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    if (editingId.value) {
      await recurringStore.update(editingId.value, formData)
      message.success('更新成功')
    } else {
      await recurringStore.create(formData)
      message.success('创建成功')
    }

    showCreateModal.value = false
    resetForm()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(formData, {
    name: '',
    type: 'expense',
    amount: null,
    categoryId: null,
    frequency: 'monthly',
    executionDate: Date.now(),
    note: '',
    status: 'active'
  })
}
</script>

<style scoped>
.recurring-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.stats-grid {
  margin-bottom: 16px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.list-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.n-card__content) {
  padding: 16px 20px;
}
</style>
