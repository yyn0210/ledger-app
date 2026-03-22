<template>
  <div class="transaction-create">
    <n-card title="新建交易">
      <TransactionForm
        :book-list="bookList"
        @submit="handleSubmit"
        @reset="handleReset"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import TransactionForm from '@/components/TransactionForm/index.vue'
import { getBookList } from '@/api/book'
import { createTransaction } from '@/api/transaction'

const router = useRouter()
const message = useMessage()

const bookList = ref([])

onMounted(async () => {
  try {
    const data = await getBookList()
    bookList.value = data.list || data || []
  } catch (error) {
    console.error('获取账本列表失败:', error)
  }
})

const handleSubmit = async formData => {
  try {
    await createTransaction(formData)
    message.success('交易创建成功')
    router.push('/transaction/list')
  } catch (error) {
    console.error('创建交易失败:', error)
  }
}

const handleReset = () => {
  console.log('表单已重置')
}
</script>

<style scoped>
.transaction-create {
  max-width: 800px;
  margin: 0 auto;
}
</style>
