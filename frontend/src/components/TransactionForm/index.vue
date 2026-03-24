<template>
  <n-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-placement="top"
    class="transaction-form"
  >
    <n-form-item label="交易类型" path="type">
      <n-radio-group v-model:value="formData.type">
        <n-radio-button value="expense">支出</n-radio-button>
        <n-radio-button value="income">收入</n-radio-button>
      </n-radio-group>
    </n-form-item>

    <n-form-item label="金额" path="amount">
      <AmountInput v-model="formData.amount" placeholder="请输入金额" />
    </n-form-item>

    <n-form-item label="分类" path="category">
      <CategoryPicker
        v-model="formData.category"
        :type="formData.type"
        placeholder="请选择分类"
      />
    </n-form-item>

    <n-form-item label="账本" path="bookId">
      <n-select
        v-model:value="formData.bookId"
        :options="bookOptions"
        placeholder="请选择账本"
      />
    </n-form-item>

    <n-form-item label="日期" path="date">
      <n-date-picker
        v-model:value="formData.date"
        type="date"
        placeholder="选择日期"
        style="width: 100%"
      />
    </n-form-item>

    <n-form-item label="备注" path="note">
      <n-input
        v-model:value="formData.note"
        type="textarea"
        placeholder="请输入备注（可选）"
        :rows="3"
      />
    </n-form-item>

    <n-form-item>
      <n-space>
        <n-button type="primary" @click="handleSubmit">
          {{ loading ? '提交中...' : '提交' }}
        </n-button>
        <n-button @click="handleReset">重置</n-button>
      </n-space>
    </n-form-item>
  </n-form>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useMessage } from 'naive-ui'
import AmountInput from '@/components/AmountInput/index.vue'
import CategoryPicker from '@/components/CategoryPicker/index.vue'

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({})
  },
  bookList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit', 'reset'])

const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  type: props.initialData.type || 'expense',
  amount: props.initialData.amount || null,
  category: props.initialData.category || '',
  bookId: props.initialData.bookId || '',
  date: props.initialData.date ? new Date(props.initialData.date).getTime() : Date.now(),
  note: props.initialData.note || ''
})

const formRules = {
  amount: {
    required: true,
    message: '请输入金额',
    trigger: ['blur', 'change']
  },
  category: {
    required: true,
    message: '请选择分类',
    trigger: ['blur', 'change']
  },
  bookId: {
    required: true,
    message: '请选择账本',
    trigger: ['blur', 'change']
  }
}

const bookOptions = computed(() => {
  return props.bookList.map(book => ({
    label: book.name,
    value: book.id
  }))
})

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    emit('submit', { ...formData })
  } catch (error) {
    message.error('请检查表单填写是否正确')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formRef.value?.restoreValidation()
  formData.type = 'expense'
  formData.amount = null
  formData.category = ''
  formData.bookId = ''
  formData.date = Date.now()
  formData.note = ''
  emit('reset')
}
</script>

<style scoped>
.transaction-form {
  max-width: 600px;
}
</style>
