<template>
  <div class="transaction-edit">
    <n-card title="编辑交易">
      <TransactionForm
        v-if="loading"
        :book-list="bookList"
        :initial-data="initialData"
        @submit="handleSubmit"
        @reset="handleReset"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import TransactionForm from '@/components/TransactionForm/index.vue'
import { getBookList } from '@/api/book'
import { getTransactionDetail, updateTransaction } from '@/api/transaction'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const loading = ref(true)
const bookList = ref([])
const initialData = reactive({})

onMounted(async () => {
  try {
    const [bookData, transactionData] = await Promise.all([
      getBookList(),
      getTransactionDetail(route.params.id)
    ])
    
    bookList.value = bookData.list || bookData || []
    
    Object.assign(initialData, {
      type: transactionData.type,
      amount: transactionData.amount,
      category: transactionData.category,
      bookId: transactionData.bookId,
      date: transactionData.date,
      note: transactionData.note
    })
  } catch (error) {
    message.error('获取交易详情失败')
    console.error('获取交易详情失败:', error)
  } finally {
    loading.value = false
  }
})

const handleSubmit = async formData => {
  try {
    await updateTransaction(route.params.id, formData)
    message.success('交易更新成功')
    router.push('/transaction/list')
  } catch (error) {
    console.error('更新交易失败:', error)
  }
}

const handleReset = () => {
  console.log('表单已重置')
}
</script>

<style scoped>
.transaction-edit {
  max-width: 800px;
  margin: 0 auto;
}
</style>
