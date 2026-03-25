<template>
  <div class="transaction-detail">
    <n-spin :show="loading">
      <template v-if="transaction">
        <!-- 头部操作 -->
        <n-card>
          <n-space justify="space-between" align="center">
            <n-space align="center">
              <n-button quaternary circle @click="handleBack">
                <template #icon>
                  <n-icon :component="ArrowBackOutline" />
                </template>
              </n-button>
              <h2 style="margin: 0">📝 交易详情</h2>
            </n-space>
            <n-space>
              <n-button @click="handleEdit">
                <template #icon>
                  <n-icon :component="CreateOutline" />
                </template>
                编辑
              </n-button>
              <n-button type="error" secondary @click="handleDelete">
                <template #icon>
                  <n-icon :component="TrashOutline" />
                </template>
                删除
              </n-button>
            </n-space>
          </n-space>
        </n-card>

        <!-- 金额展示 -->
        <n-card style="margin-top: 20px; text-align: center">
          <n-text depth="3" style="font-size: 14px">交易金额</n-text>
          <div :class="['amount-display', transaction.type]">
            <span v-if="transaction.type === 'income'">+</span>
            <span v-else-if="transaction.type === 'expense'">-</span>
            ¥{{ formatAmount(transaction.amount) }}
          </div>
          <n-tag :type="typeTagColor">
            {{ typeLabel }}
          </n-tag>
        </n-card>

        <!-- 详细信息 -->
        <n-card style="margin-top: 20px">
          <n-space vertical>
            <!-- 分类 -->
            <n-space v-if="transaction.type !== 'transfer'" justify="space-between">
              <n-text depth="3">分类</n-text>
              <n-space align="center">
                <div
                  class="category-icon-small"
                  :style="{ backgroundColor: category?.color || '#18a058' }"
                >
                  <n-icon :component="category?.icon || 'wallet'" size="16" color="#fff" />
                </div>
                <n-text>{{ category?.name || '-' }}</n-text>
              </n-space>
            </n-space>

            <!-- 账本 -->
            <n-space justify="space-between">
              <n-text depth="3">账本</n-text>
              <n-text>{{ ledger?.name || '-' }}</n-text>
            </n-space>

            <!-- 账户 -->
            <n-space justify="space-between">
              <n-text depth="3">账户</n-text>
              <n-text>{{ account?.name || '-' }}</n-text>
            </n-space>

            <!-- 转账信息 -->
            <template v-if="transaction.type === 'transfer'">
              <n-space justify="space-between">
                <n-text depth="3">转入账户</n-text>
                <n-text>{{ toAccount?.name || '-' }}</n-text>
              </n-space>
            </template>

            <!-- 交易日期 -->
            <n-space justify="space-between">
              <n-text depth="3">交易日期</n-text>
              <n-text>{{ formatDate(transaction.date) }}</n-text>
            </n-space>

            <!-- 备注 -->
            <n-space v-if="transaction.note" justify="space-between" align="start">
              <n-text depth="3">备注</n-text>
              <n-text>{{ transaction.note }}</n-text>
            </n-space>

            <!-- 创建时间 -->
            <n-divider />
            <n-space justify="space-between">
              <n-text depth="3">创建时间</n-text>
              <n-text depth="3">{{ formatDate(transaction.createdAt) }}</n-text>
            </n-space>
          </n-space>
        </n-card>

        <!-- 图片凭证 -->
        <n-card v-if="transaction.images?.length > 0" style="margin-top: 20px">
          <template #header>
            <n-text>凭证图片</n-text>
          </template>
          <n-grid :cols="3" :x-gap="12" :y-gap="12">
            <n-grid-item v-for="(img, index) in transaction.images" :key="index">
              <n-image
                :src="img"
                :preview-src="transaction.images"
                :preview-src-index="index"
                style="width: 100%; border-radius: 8px"
              />
            </n-grid-item>
          </n-grid>
        </n-card>
      </template>

      <n-empty
        v-else-if="!loading"
        description="交易不存在或已被删除"
        style="padding: 60px 0"
      >
        <template #extra>
          <n-button @click="handleBack">返回交易列表</n-button>
        </template>
      </n-empty>
    </n-spin>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import {
  ArrowBackOutline,
  CreateOutline,
  TrashOutline
} from '@vicons/ionicons5'
import { useTransactionStore } from '@/stores/transaction'
import { useBookStore } from '@/stores/book'
import { useAccountStore } from '@/stores/account'
import { useCategoryStore } from '@/stores/category'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const txStore = useTransactionStore()
const bookStore = useBookStore()
const accountStore = useAccountStore()
const categoryStore = useCategoryStore()

const transactionId = computed(() => route.params.id)
const transaction = computed(() => txStore.currentTransaction)
const loading = computed(() => txStore.loading)

// 类型标签
const typeLabel = computed(() => {
  const map = {
    income: '收入',
    expense: '支出',
    transfer: '转账'
  }
  return map[transaction.value?.type] || ''
})

const typeTagColor = computed(() => {
  const map = {
    income: 'success',
    expense: 'error',
    transfer: 'info'
  }
  return map[transaction.value?.type] || 'default'
})

// 关联数据
const category = computed(() => {
  return categoryStore.categoryList.find(
    cat => cat.id === transaction.value?.categoryId
  )
})

const ledger = computed(() => {
  return bookStore.bookList.find(
    book => book.id === transaction.value?.ledgerId
  )
})

const account = computed(() => {
  return accountStore.accountList.find(
    acc => acc.id === transaction.value?.accountId
  )
})

const toAccount = computed(() => {
  return accountStore.accountList.find(
    acc => acc.id === transaction.value?.toAccountId
  )
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/transaction/${transactionId.value}/edit`)
}

// 删除
const handleDelete = () => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这笔交易吗？删除后无法恢复！',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await txStore.deleteTransaction(transactionId.value)
        message.success('交易已删除')
        router.back()
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

onMounted(() => {
  Promise.all([
    txStore.fetchCurrentTransaction(transactionId.value),
    bookStore.fetchBookList(),
    accountStore.fetchAccountList(),
    categoryStore.fetchCategoryList()
  ]).catch(error => {
    message.error('加载失败：' + (error.message || '未知错误'))
  })
})
</script>

<style scoped>
.transaction-detail {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.amount-display {
  font-size: 36px;
  font-weight: 700;
  margin: 16px 0;
}

.amount-display.income {
  color: #10B981;
}

.amount-display.expense {
  color: #EF4444;
}

.amount-display.transfer {
  color: #4F46E5;
}

.category-icon-small {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
