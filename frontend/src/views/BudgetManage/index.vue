<template>
  <div class="budget-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Wallet" size="28" color="#10b981" />
        预算管理
      </h1>
      <n-button type="success" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="Add" />
        </template>
        新建预算
      </n-button>
    </div>

    <!-- 总览卡片 -->
    <n-grid :cols="3" :x-gap="16" class="stats-grid">
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #dcfce7;">
              <n-icon :component="Wallet" size="24" color="#10b981" />
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ budgetStore.totalBudget }}</div>
              <div class="stat-label">总预算</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef3c7;">
              <n-icon :component="TrendingUp" size="24" color="#f59e0b" />
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ budgetStore.totalSpent }}</div>
              <div class="stat-label">已支出</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #dbeafe;">
              <n-icon :component="PieChart" size="24" color="#3b82f6" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ remainingPercent }}%</div>
              <div class="stat-label">剩余比例</div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 预列表 -->
    <n-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>预算列表</span>
          <n-space>
            <n-radio-group v-model:value="filterType" size="small">
              <n-radio-button value="all">全部</n-radio-button>
              <n-radio-button value="active">进行中</n-radio-button>
              <n-radio-button value="overspent">已超支</n-radio-button>
            </n-radio-group>
          </n-space>
        </div>
      </template>

      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="budget in filteredList" :key="budget.id">
          <n-card :class="['budget-item', { overspent: budget.spent > budget.amount }]">
            <div class="budget-header">
              <div class="budget-info">
                <div class="budget-name">{{ budget.name }}</div>
                <div class="budget-category">
                  <n-tag :type="budget.type === 'category' ? 'info' : 'success'" size="small">
                    {{ budget.type === 'category' ? '分类预算' : '账户预算' }}
                  </n-tag>
                </div>
              </div>
              <div class="budget-actions">
                <n-button size="small" @click="handleEdit(budget)">编辑</n-button>
                <n-button size="small" type="error" @click="handleDelete(budget)">删除</n-button>
              </div>
            </div>

            <div class="budget-amounts">
              <div class="amount-row">
                <span class="amount-label">预算金额</span>
                <span class="amount-value">¥{{ budget.amount }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">已支出</span>
                <span class="amount-value spent">¥{{ budget.spent || 0 }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">剩余</span>
                <span class="amount-value remaining">¥{{ Math.max(0, budget.amount - (budget.spent || 0)) }}</span>
              </div>
            </div>

            <div class="budget-progress">
              <n-progress
                type="line"
                :percentage="Math.min(100, ((budget.spent || 0) / budget.amount * 100).toFixed(1))"
                :status="budget.spent > budget.amount ? 'error' : 'default'"
                :show-indicator="true"
                :height="12"
              />
              <div class="progress-warning" v-if="budget.spent > budget.amount * 0.9 && budget.spent <= budget.amount">
                <n-alert type="warning" :bordered="false" style="padding: 8px; margin-top: 8px;">
                  <template #icon>
                    <n-icon :component="Warning" />
                  </template>
                  预算即将用尽，请控制支出
                </n-alert>
              </div>
              <div class="progress-overspent" v-if="budget.spent > budget.amount">
                <n-alert type="error" :bordered="false" style="padding: 8px; margin-top: 8px;">
                  <template #icon>
                    <n-icon :component="AlertCircle" />
                  </template>
                  已超支 ¥{{ (budget.spent - budget.amount).toFixed(2) }}
                </n-alert>
              </div>
            </div>

            <div class="budget-footer">
              <span class="budget-period">
                <n-icon :component="Calendar" size="14" />
                {{ getPeriodText(budget.period) }}
              </span>
              <n-tag :type="budget.status === 'active' ? 'success' : 'default'" size="small">
                {{ budget.status === 'active' ? '启用中' : '已暂停' }}
              </n-tag>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-empty
        v-if="filteredList.length === 0 && !budgetStore.loading"
        description="暂无预算，点击右上角新建"
        size="large"
        style="padding: 40px 0"
      />
    </n-card>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      preset="card"
      :title="editingId ? '编辑预算' : '新建预算'"
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
        <n-form-item label="预算名称" path="name">
          <n-input v-model:value="formData.name" placeholder="如：每月餐饮预算" />
        </n-form-item>

        <n-form-item label="预算类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="category">分类预算</n-radio>
            <n-radio value="account">账户预算</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="关联对象" path="targetId">
          <n-select
            v-model:value="formData.targetId"
            :options="targetOptions"
            placeholder="请选择分类或账户"
          />
        </n-form-item>

        <n-form-item label="预算金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            placeholder="请输入预算金额"
            :min="1"
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item label="周期" path="period">
          <n-select
            v-model:value="formData.period"
            :options="periodOptions"
            placeholder="请选择周期"
          />
        </n-form-item>

        <n-form-item label="开始日期" path="startDate">
          <n-date-picker
            v-model:value="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
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
          <n-button type="success" @click="handleSubmit" :loading="submitting">
            确认
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useMessage } from 'naive-ui'
import {
  Wallet, Add, TrendingUp, PieChart, Calendar, Warning, AlertCircle, CreateOutline as Edit, Trash
} from '@vicons/ionicons5'
import { useBudgetStore } from '@/stores/budget'
import { getCategoryList } from '@/api/category'
import { deleteBudget } from '@/api/budget'

const message = useMessage()
const budgetStore = useBudgetStore()

const showCreateModal = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const filterType = ref('all')
const categories = ref([])
const accounts = ref([])

const formData = reactive({
  name: '',
  type: 'category',
  targetId: null,
  amount: null,
  period: 'monthly',
  startDate: Date.now(),
  status: 'active'
})

const formRules = {
  name: { required: true, message: '请输入预算名称', trigger: 'blur' },
  targetId: { required: true, message: '请选择关联对象', trigger: 'change' },
  amount: { required: true, message: '请输入预算金额', trigger: 'blur' },
  period: { required: true, message: '请选择周期', trigger: 'change' }
}

const periodOptions = [
  { label: '每日', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
  { label: '每年', value: 'yearly' }
]

const targetOptions = computed(() => {
  if (formData.type === 'category') {
    return (categories.value || []).map(c => ({ label: c.name, value: c.id }))
  }
  return (accounts.value || []).map(a => ({ label: a.name, value: a.id }))
})

const filteredList = computed(() => {
  if (filterType.value === 'all') return budgetStore.list
  if (filterType.value === 'active') return budgetStore.list.filter(b => b.status === 'active')
  if (filterType.value === 'overspent') return budgetStore.list.filter(b => b.spent > b.amount)
  return budgetStore.list
})

const remainingPercent = computed(() => {
  if (budgetStore.totalBudget === 0) return 100
  return ((budgetStore.totalBudget - budgetStore.totalSpent) / budgetStore.totalBudget * 100).toFixed(1)
})

const formRef = ref(null)

onMounted(async () => {
  await loadCategories()
  await budgetStore.fetchList()
})

const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    const defaults = data.defaults || []
    categories.value = defaults.filter(c => c.type === 'expense')
    accounts.value = [
      { id: 1, name: '现金' },
      { id: 2, name: '银行卡' },
      { id: 3, name: '支付宝' },
      { id: 4, name: '微信' }
    ]
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const getPeriodText = (period) => {
  const map = { daily: '每日', weekly: '每周', monthly: '每月', yearly: '每年' }
  return map[period] || period
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(formData, { ...row })
  showCreateModal.value = true
}

const handleDelete = async (row) => {
  await deleteBudget(row.id)
  message.success('删除成功')
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    if (editingId.value) {
      await budgetStore.update(editingId.value, formData)
      message.success('更新成功')
    } else {
      await budgetStore.create(formData)
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
    type: 'category',
    targetId: null,
    amount: null,
    period: 'monthly',
    startDate: Date.now(),
    status: 'active'
  })
}
</script>

<style scoped>
.budget-page {
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
  font-size: 24px;
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

.budget-item {
  transition: all 0.3s;
  border-left: 4px solid #10b981;
}

.budget-item.overspent {
  border-left-color: #ef4444;
  background: #fef2f2;
}

.budget-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.budget-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.budget-category {
  margin-bottom: 8px;
}

.budget-actions {
  display: flex;
  gap: 8px;
}

.budget-amounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount-label {
  font-size: 13px;
  color: #6b7280;
}

.amount-value {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;

  &.spent {
    color: #f59e0b;
  }

  &.remaining {
    color: #10b981;
  }
}

.budget-progress {
  margin-bottom: 16px;
}

.progress-warning,
.progress-overspent {
  margin-top: 8px;
}

.budget-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.budget-period {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

:deep(.n-card__content) {
  padding: 20px;
}
</style>
