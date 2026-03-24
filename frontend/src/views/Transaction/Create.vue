<template>
  <div class="transaction-create">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="AddCircle" size="28" color="#3385ff" />
        快速记账
      </h1>
    </div>

    <n-card :bordered="false" class="form-card">
      <TransactionForm
        ref="formRef"
        :book-list="bookList"
        @submit="handleSubmit"
      />
    </n-card>

    <!-- 最近记录 -->
    <n-card :bordered="false" class="recent-card" title="最近记录">
      <div class="recent-list">
        <div
          v-for="item in recentTransactions"
          :key="item.id"
          class="recent-item"
          @click="editTransaction(item.id)"
        >
          <div class="item-icon" :style="{ backgroundColor: item.color }">
            <n-icon :component="item.icon" size="20" color="#fff" />
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.categoryName }}</div>
            <div class="item-note">{{ item.note || item.bookName }}</div>
          </div>
          <div class="item-amount" :class="item.type">
            {{ item.type === 'expense' ? '-' : '+' }}¥{{ item.amount }}
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { AddCircle, FastFood, Car, Cart, Cash, ellipsisHorizontal } from '@vicons/ionicons5'
import TransactionForm from '@/components/TransactionForm/index.vue'
import { getBookList } from '@/api/book'
import { createTransaction, getTransactionList } from '@/api/transaction'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)

const bookList = ref([])
const recentTransactions = ref([])

const categoryIcons = {
  food: FastFood,
  transport: Car,
  shopping: Cart,
  salary: Cash,
  other: ellipsisHorizontal
}

const categoryColors = {
  food: '#ff9900',
  transport: '#3385ff',
  shopping: '#ff6b6b',
  salary: '#2ecc71',
  other: '#95a5a6'
}

onMounted(async () => {
  await loadBookList()
  await loadRecentTransactions()
})

const loadBookList = async () => {
  try {
    const data = await getBookList()
    bookList.value = data.list || data || []
  } catch (error) {
    console.error('获取账本列表失败:', error)
  }
}

const loadRecentTransactions = async () => {
  try {
    const data = await getTransactionList({ page: 1, pageSize: 5 })
    recentTransactions.value = (data.list || []).map(item => ({
      ...item,
      icon: categoryIcons[item.category] || ellipsisHorizontal,
      color: categoryColors[item.category] || '#95a5a6'
    }))
  } catch (error) {
    console.error('获取最近记录失败:', error)
  }
}

const handleSubmit = async (formData) => {
  try {
    await createTransaction({
      ...formData,
      amount: parseFloat(formData.amount)
    })
    message.success('记账成功')
    formRef.value?.resetFields()
    await loadRecentTransactions()
  } catch (error) {
    console.error('创建交易失败:', error)
  }
}

const editTransaction = (id) => {
  router.push(`/transaction/edit/${id}`)
}
</script>

<style scoped>
.transaction-create {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
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

.form-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.recent-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f6f7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.recent-item:hover {
  background: #e8eaed;
  transform: translateX(4px);
}

.item-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  margin-right: 16px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.item-note {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-amount {
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  
  &.expense {
    color: #ff6b6b;
  }
  
  &.income {
    color: #52c41a;
  }
}
</style>
