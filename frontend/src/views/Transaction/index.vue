<template>
  <div class="transaction-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="SwapHorizontal" size="28" color="#3385ff" />
        交易记录
      </h1>
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="AddCircle" />
        </template>
        记一笔
      </n-button>
    </div>

    <!-- 筛选栏 -->
    <n-card class="filter-card">
      <n-form inline :label-width="80" :model="filterForm">
        <n-form-item label="日期范围">
          <n-date-picker
            v-model:value="filterForm.dateRange"
            type="daterange"
            placeholder="选择日期范围"
            style="width: 240px"
            @update:value="applyFilter"
          />
        </n-form-item>
        <n-form-item label="交易类型">
          <n-select
            v-model:value="filterForm.type"
            :options="typeOptions"
            placeholder="全部类型"
            clearable
            style="width: 120px"
            @update:value="applyFilter"
          />
        </n-form-item>
        <n-form-item label="分类">
          <n-select
            v-model:value="filterForm.categoryId"
            :options="categoryOptions"
            placeholder="全部分类"
            clearable
            style="width: 120px"
            @update:value="applyFilter"
          />
        </n-form-item>
        <n-form-item label="账户">
          <n-select
            v-model:value="filterForm.accountId"
            :options="accountOptions"
            placeholder="全部账户"
            clearable
            style="width: 120px"
            @update:value="applyFilter"
          />
        </n-form-item>
        <n-form-item>
          <n-button @click="resetFilter">重置</n-button>
        </n-form-item>
      </n-form>
    </n-card>

    <!-- 月度统计 -->
    <n-card class="stats-card">
      <n-grid :cols="3" :x-gap="16">
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">本月收入</div>
            <div class="stat-value income">¥{{ monthlyStats.income }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">本月支出</div>
            <div class="stat-value expense">¥{{ monthlyStats.expense }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-label">本月结余</div>
            <div class="stat-value balance">¥{{ monthlyStats.balance }}</div>
          </div>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 交易列表 -->
    <n-card class="transaction-list-card">
      <template #header>
        <div class="list-header">
          <span>交易明细</span>
          <n-space>
            <n-text depth="3">{{ transactions.length }}条记录</n-text>
          </n-space>
        </div>
      </template>

      <div v-for="(group, date) in groupedTransactions" :key="date" class="date-group">
        <div class="date-header">
          <span class="date-text">{{ formatDateGroup(date) }}</span>
          <span class="date-summary">
            支出 ¥{{ group.filter(t => t.type === 'expense').reduce((s, t) => s + t.amount, 0).toFixed(2) }}
            收入 ¥{{ group.filter(t => t.type === 'income').reduce((s, t) => s + t.amount, 0).toFixed(2) }}
          </span>
        </div>

        <div class="transaction-items">
          <div
            v-for="transaction in group"
            :key="transaction.id"
            class="transaction-item"
            @click="editTransaction(transaction)"
          >
            <div class="transaction-icon" :style="{ backgroundColor: transaction.categoryColor }">
              <n-icon :component="getIconComponent(transaction.categoryIcon)" size="20" color="#fff" />
            </div>
            <div class="transaction-info">
              <div class="transaction-main">
                <span class="transaction-name">{{ transaction.categoryName }}</span>
                <n-tag
                  :type="transaction.type === 'income' ? 'success' : 'error'"
                  size="small"
                  round
                >
                  {{ transaction.type === 'income' ? '收入' : '支出' }}
                </n-tag>
              </div>
              <div class="transaction-sub">
                <span class="transaction-note">{{ transaction.note || transaction.accountName }}</span>
                <span class="transaction-time">{{ transaction.time || '' }}</span>
              </div>
            </div>
            <div class="transaction-amount" :class="transaction.type">
              {{ transaction.type === 'income' ? '+' : '-' }}¥{{ transaction.amount.toFixed(2) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <n-empty
        v-if="transactions.length === 0"
        description="暂无交易记录，记一笔开始吧"
        size="large"
        style="padding: 40px 0"
      >
        <template #extra>
          <n-button type="primary" @click="showCreateModal = true">记一笔</n-button>
        </template>
      </n-empty>

      <!-- 分页 -->
      <div v-if="transactions.length > 0" class="pagination">
        <n-pagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="total"
          @update:page="loadTransactions"
        />
      </div>
    </n-card>

    <!-- 新建/编辑交易弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      :title="editingTransaction ? '编辑交易' : '新建交易'"
      preset="dialog"
      :show-icon="false"
      style="width: 600px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="交易类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="expense">支出</n-radio>
            <n-radio value="income">收入</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="金额" path="amount">
              <n-input-number
                v-model:value="formData.amount"
                placeholder="请输入金额"
                :min="0.01"
                :max="999999999"
                :precision="2"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </n-input-number>
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="日期" path="date">
              <n-date-picker
                v-model:value="formData.date"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="分类" path="categoryId">
              <n-select
                v-model:value="formData.categoryId"
                :options="currentCategoryOptions"
                placeholder="请选择分类"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="账户" path="accountId">
              <n-select
                v-model:value="formData.accountId"
                :options="accountOptions"
                placeholder="请选择账户"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="请输入备注（可选）"
            :maxlength="200"
            show-count
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="凭证图片">
          <n-upload
            :file-list="imageList"
            :max="3"
            multiple
            @change="handleImageChange"
          >
            <n-button>上传图片</n-button>
          </n-upload>
          <n-text depth="3" style="margin-left: 12px">最多上传 3 张图片</n-text>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  SwapHorizontal, AddCircle, Create, Trash,
  FastFood, Car, Cart, Home, Wallet, Business, Beer,
  Heart, School, GameController, Airplane, Shirt, Bag,
  Cash, Gift, TrendingUp
} from '@vicons/ionicons5'
import { getTransactionList, createTransaction, updateTransaction, deleteTransaction } from '@/api/transaction'
import { getCategoryList } from '@/api/category'
import { getAccountList } from '@/api/account'
import { useTransactionStore } from '@/stores/transaction'

const message = useMessage()
const dialog = useDialog()
const transactionStore = useTransactionStore()

const showCreateModal = ref(false)
const editingTransaction = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const transactions = ref([])
const categories = ref({ expense: [], income: [] })
const accounts = ref([])
const imageList = ref([])

const filterForm = reactive({
  dateRange: null,
  type: null,
  categoryId: null,
  accountId: null
})

const formData = reactive({
  type: 'expense',
  amount: null,
  date: Date.now(),
  categoryId: null,
  accountId: null,
  note: '',
  images: []
})

const formRules = {
  amount: {
    required: true,
    message: '请输入金额',
    trigger: 'blur'
  },
  categoryId: {
    required: true,
    message: '请选择分类',
    trigger: 'change'
  },
  accountId: {
    required: true,
    message: '请选择账户',
    trigger: 'change'
  }
}

const typeOptions = [
  { label: '支出', value: 'expense' },
  { label: '收入', value: 'income' }
]

const categoryOptions = computed(() => {
  const expense = (categories.value.expense || []).map(c => ({ label: c.name, value: c.id }))
  const income = (categories.value.income || []).map(c => ({ label: c.name, value: c.id }))
  return [...expense, ...income]
})

const currentCategoryOptions = computed(() => {
  const list = formData.type === 'income' ? categories.value.income : categories.value.expense
  return (list || []).map(c => ({ label: c.name, value: c.id }))
})

const accountOptions = computed(() => {
  return (accounts.value || []).map(a => ({ label: a.name, value: a.id }))
})

const monthlyStats = computed(() => {
  const now = new Date()
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()
  
  const monthTransactions = transactions.value.filter(t => {
    const tDate = new Date(t.date)
    return tDate.getMonth() === currentMonth && tDate.getFullYear() === currentYear
  })
  
  const income = monthTransactions
    .filter(t => t.type === 'income')
    .reduce((sum, t) => sum + (t.amount || 0), 0)
  
  const expense = monthTransactions
    .filter(t => t.type === 'expense')
    .reduce((sum, t) => sum + (t.amount || 0), 0)
  
  return {
    income: income.toFixed(2),
    expense: expense.toFixed(2),
    balance: (income - expense).toFixed(2)
  }
})

const groupedTransactions = computed(() => {
  const groups = {}
  transactions.value.forEach(transaction => {
    const date = transaction.date
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(transaction)
  })
  return groups
})

onMounted(async () => {
  await Promise.all([
    loadTransactions(),
    loadCategories(),
    loadAccounts()
  ])
})

const loadTransactions = async () => {
  try {
    const data = await getTransactionList({ page: currentPage.value, pageSize: pageSize.value })
    transactions.value = data.list || data || []
    total.value = data.total || transactions.value.length
  } catch (error) {
    console.error('获取交易列表失败:', error)
  }
}

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

const loadAccounts = async () => {
  try {
    const data = await getAccountList()
    accounts.value = data.list || data || []
  } catch (error) {
    console.error('获取账户失败:', error)
  }
}

const applyFilter = () => {
  currentPage.value = 1
  loadTransactions()
}

const resetFilter = () => {
  filterForm.dateRange = null
  filterForm.type = null
  filterForm.categoryId = null
  filterForm.accountId = null
  currentPage.value = 1
  loadTransactions()
}

const formatDateGroup = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getIconComponent = (iconName) => {
  const iconMap = {
    'FastFood': FastFood,
    'Car': Car,
    'Cart': Cart,
    'Home': Home,
    'Wallet': Wallet,
    'Business': Business,
    'Beer': Beer,
    'Heart': Heart,
    'School': School,
    'GameController': GameController,
    'Airplane': Airplane,
    'Shirt': Shirt,
    'Bag': Bag,
    'Cash': Cash,
    'Gift': Gift,
    'TrendingUp': TrendingUp
  }
  return iconMap[iconName] || FastFood
}

const editTransaction = (transaction) => {
  editingTransaction.value = transaction
  formData.type = transaction.type
  formData.amount = transaction.amount
  formData.date = new Date(transaction.date).getTime()
  formData.categoryId = transaction.categoryId
  formData.accountId = transaction.accountId
  formData.note = transaction.note || ''
  formData.images = transaction.images || []
  imageList.value = (transaction.images || []).map(url => ({ url }))
  showCreateModal.value = true
}

const handleImageChange = ({ fileList }) => {
  imageList.value = fileList
  formData.images = fileList.map(f => f.url).filter(Boolean)
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const submitData = {
      ...formData,
      date: new Date(formData.date).toISOString().split('T')[0]
    }

    if (editingTransaction.value) {
      await updateTransaction(editingTransaction.value.id, submitData)
      message.success('交易已更新')
    } else {
      await createTransaction(submitData)
      message.success('交易已创建')
    }

    showCreateModal.value = false
    editingTransaction.value = null
    resetForm()
    await loadTransactions()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.type = 'expense'
  formData.amount = null
  formData.date = Date.now()
  formData.categoryId = null
  formData.accountId = null
  formData.note = ''
  formData.images = []
  imageList.value = []
  formRef.value?.restoreValidation()
}
</script>

<style scoped>
.transaction-page {
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

.filter-card {
  margin-bottom: 16px;
}

.stats-card {
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

  &.income {
    color: #52c41a;
  }

  &.expense {
    color: #ff6b6b;
  }

  &.balance {
    color: #3385ff;
  }
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-group {
  margin-bottom: 24px;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.date-text {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

.date-summary {
  font-size: 13px;
  color: #999;
}

.transaction-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f3f4f6;
  }
}

.transaction-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-right: 12px;
  flex-shrink: 0;
}

.transaction-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.transaction-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.transaction-name {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
}

.transaction-sub {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.transaction-note {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.transaction-time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.transaction-amount {
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 12px;

  &.income {
    color: #52c41a;
  }

  &.expense {
    color: #ff6b6b;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.n-card__content) {
  padding: 16px 20px;
}
</style>
