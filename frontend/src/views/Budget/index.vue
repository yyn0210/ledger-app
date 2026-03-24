<template>
  <div class="budget-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="PieChart" size="28" color="#f59e0b" />
        预算管理
      </h1>
      <n-space>
        <n-date-picker
          v-model:value="selectedMonth"
          type="month"
          placeholder="选择月份"
          style="width: 160px"
          @update:value="loadBudgets"
        />
        <n-button type="primary" @click="showCreateModal = true">
          <template #icon>
            <n-icon :component="AddCircle" />
          </template>
          设置预算
        </n-button>
      </n-space>
    </div>

    <!-- 预算概览 -->
    <n-card class="overview-card">
      <n-grid :cols="4" :x-gap="16">
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">总预算</div>
            <div class="stat-value">¥{{ totalBudget }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">已使用</div>
            <div class="stat-value used">¥{{ totalUsed }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">剩余</div>
            <div class="stat-value remaining">¥{{ totalRemaining }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">超支预算</div>
            <div class="stat-value exceeded">{{ exceededCount }}个</div>
          </div>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 预算类型切换 -->
    <n-card class="filter-card">
      <n-tabs v-model:value="budgetType" type="segment" @update:value="loadBudgets">
        <n-tab-pane name="category" tab="分类预算" />
        <n-tab-pane name="account" tab="账户预算" />
      </n-tabs>
    </n-card>

    <!-- 预算列表 -->
    <n-card class="budget-list-card">
      <template #header>
        <div class="list-header">
          <span>{{ budgetType === 'category' ? '分类预算' : '账户预算' }}</span>
          <n-text depth="3">{{ budgets.length }}条记录</n-text>
        </div>
      </template>

      <n-data-table
        :columns="columns"
        :data="budgets"
        :row-key="row => row.id"
        :loading="loading"
        :pagination="false"
      />

      <!-- 空状态 -->
      <n-empty
        v-if="budgets.length === 0"
        description="暂无预算，设置一个开始吧"
        size="large"
        style="padding: 40px 0"
      >
        <template #extra>
          <n-button type="primary" @click="showCreateModal = true">设置预算</n-button>
        </template>
      </n-empty>
    </n-card>

    <!-- 新建/编辑预算弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      :title="editingBudget ? '编辑预算' : '设置预算'"
      preset="dialog"
      :show-icon="false"
      style="width: 500px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="预算类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="category">分类预算</n-radio>
            <n-radio value="account">账户预算</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-form-item
          :label="formData.type === 'category' ? '分类' : '账户'"
          :path="formData.type === 'category' ? 'categoryId' : 'accountId'"
        >
          <n-select
            v-if="formData.type === 'category'"
            v-model:value="formData.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
          <n-select
            v-else
            v-model:value="formData.accountId"
            :options="accountOptions"
            placeholder="请选择账户"
          />
        </n-form-item>

        <n-form-item label="预算金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            placeholder="请输入预算金额"
            :min="0.01"
            :max="999999999"
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="请输入备注（可选）"
            :maxlength="200"
            show-count
            :rows="2"
          />
        </n-form-item>
      </n-form>

      <template #action>
        <n-button @click="showCreateModal = false">取消</n-button>
        <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useMessage, useDialog, NProgress, NTag } from 'naive-ui'
import { PieChart, AddCircle, Create, Trash } from '@vicons/ionicons5'
import { getBudgetList, createBudget, updateBudget, deleteBudget } from '@/api/budget'
import { getCategoryList } from '@/api/category'
import { getAccountList } from '@/api/account'
import { useBudgetStore } from '@/stores/budget'

const message = useMessage()
const dialog = useDialog()
const budgetStore = useBudgetStore()

const showCreateModal = ref(false)
const editingBudget = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const budgetType = ref('category')
const selectedMonth = ref(Date.now())

const categories = ref({ expense: [], income: [] })
const accounts = ref([])
const budgets = computed(() => budgetStore.budgets)
const loading = computed(() => budgetStore.loading)

const formData = reactive({
  type: 'category',
  categoryId: null,
  accountId: null,
  amount: null,
  note: ''
})

const formRules = {
  amount: {
    required: true,
    message: '请输入预算金额',
    trigger: 'blur'
  },
  categoryId: {
    required: true,
    message: '请选择分类',
    trigger: 'change',
    validator: (rule, value) => {
      if (formData.type === 'category' && !value) {
        return new Error('请选择分类')
      }
      return true
    }
  },
  accountId: {
    required: true,
    message: '请选择账户',
    trigger: 'change',
    validator: (rule, value) => {
      if (formData.type === 'account' && !value) {
        return new Error('请选择账户')
      }
      return true
    }
  }
}

const categoryOptions = computed(() => {
  const list = categories.value.expense || []
  return list.map(c => ({ label: c.name, value: c.id }))
})

const accountOptions = computed(() => {
  return (accounts.value || []).map(a => ({ label: a.name, value: a.id }))
})

const totalBudget = computed(() => budgetStore.totalBudget.toFixed(2))
const totalUsed = computed(() => budgetStore.totalUsed.toFixed(2))
const totalRemaining = computed(() => (budgetStore.totalBudget - budgetStore.totalUsed).toFixed(2))
const exceededCount = computed(() => budgetStore.exceededBudgets.length)

const columns = computed(() => [
  {
    title: '名称',
    key: 'name',
    width: 150,
    render: (row) => h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
      h('div', {
        style: `width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 8px; background: ${row.color || '#e5e7eb'};`
      }, [row.icon ? h('span', { innerHTML: row.icon }) : null]),
      h('span', row.name)
    ])
  },
  {
    title: '预算金额',
    key: 'amount',
    width: 120,
    render: (row) => h('span', { style: 'font-weight: 600;' }, `¥${(row.amount || 0).toFixed(2)}`)
  },
  {
    title: '已使用',
    key: 'used',
    width: 120,
    render: (row) => h('span', {
      style: `color: ${(row.used || 0) > (row.amount || 0) ? '#ef4444' : '#6b7280'};`
    }, `¥${(row.used || 0).toFixed(2)}`)
  },
  {
    title: '进度',
    key: 'progress',
    width: 200,
    render: (row) => {
      const percent = row.amount > 0 ? Math.min(100, ((row.used || 0) / row.amount) * 100) : 0
      const isExceeded = (row.used || 0) > (row.amount || 0)
      return h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
        h(NProgress, {
          percentage: percent,
          status: isExceeded ? 'error' : 'success',
          processing: false,
          showIndicator: false,
          style: 'flex: 1;'
        }),
        h('span', { style: 'width: 50px; text-align: right; font-size: 12px;' },
          `${percent.toFixed(0)}%`
        )
      ])
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const isExceeded = (row.used || 0) > (row.amount || 0)
      const isWarning = !isExceeded && ((row.used || 0) / (row.amount || 1)) > 0.8
      return h(NTag, {
        type: isExceeded ? 'error' : isWarning ? 'warning' : 'success',
        size: 'small',
        bordered: false
      }, { default: () => isExceeded ? '超支' : isWarning ? '预警' : '正常' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) => h('div', { style: 'display: flex; gap: 8px;' }, [
      h(NTag, {
        type: 'info',
        size: 'small',
        style: 'cursor: pointer;',
        onClick: () => editBudget(row)
      }, { default: () => '编辑' }),
      h(NTag, {
        type: 'error',
        size: 'small',
        style: 'cursor: pointer;',
        onClick: () => deleteBudgetItem(row)
      }, { default: () => '删除' })
    ])
  }
])

onMounted(async () => {
  await Promise.all([
    loadBudgets(),
    loadCategories(),
    loadAccounts()
  ])
})

const loadBudgets = async () => {
  await budgetStore.fetchBudgets({ type: budgetType.value, month: selectedMonth.value })
}

const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    const defaults = data.defaults || []
    categories.value.expense = defaults.filter(c => c.type === 'expense')
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const loadAccounts = async () => {
  try {
    const data = await getAccountList()
    accounts.value = data.list || data || []
  } catch (error) {
    console.error('获取账户失败:', error)
  }
}

const editBudget = (budget) => {
  editingBudget.value = budget
  formData.type = budget.type || 'category'
  formData.categoryId = budget.categoryId
  formData.accountId = budget.accountId
  formData.amount = budget.amount
  formData.note = budget.note || ''
  showCreateModal.value = true
}

const deleteBudgetItem = (budget) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除"${budget.name}"的预算吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await budgetStore.removeBudget(budget.id)
      message.success('预算已删除')
      await loadBudgets()
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const submitData = {
      ...formData,
      month: new Date(selectedMonth.value).toISOString().slice(0, 7)
    }

    if (editingBudget.value) {
      await budgetStore.updateBudgetData(editingBudget.value.id, submitData)
      message.success('预算已更新')
    } else {
      await budgetStore.addBudget(submitData)
      message.success('预算已设置')
    }

    showCreateModal.value = false
    editingBudget.value = null
    resetForm()
    await loadBudgets()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.type = 'category'
  formData.categoryId = null
  formData.accountId = null
  formData.amount = null
  formData.note = ''
  formRef.value?.restoreValidation()
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

.overview-card {
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  padding: 12px 0;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;

  &.used {
    color: #f59e0b;
  }

  &.remaining {
    color: #10b981;
  }

  &.exceeded {
    color: #ef4444;
  }
}

.filter-card {
  margin-bottom: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.budget-list-card {
  :deep(.n-data-table-th) {
    font-weight: 600;
  }
}

:deep(.n-card__content) {
  padding: 16px 20px;
}
</style>
