<template>
  <div class="transaction-form">
    <n-card>
      <template #header>
        <n-space align="center">
          <n-button quaternary circle @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
          </n-button>
          <h2 style="margin: 0">
            {{ isEdit ? '✏️ 编辑交易' : '➕ 记一笔' }}
          </h2>
        </n-space>
      </template>

      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
        <!-- 类型切换 -->
        <n-form-item label="交易类型">
          <type-switch v-model="formData.type" />
        </n-form-item>

        <!-- 金额输入 -->
        <n-form-item label="金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            :min="0.01"
            :max="999999999"
            placeholder="输入金额"
            style="width: 100%"
            size="large"
          >
            <template #prefix>
              <n-text type="error" style="font-weight: 600">¥</n-text>
            </template>
          </n-input-number>
        </n-form-item>

        <!-- 账本选择 -->
        <n-form-item label="账本" path="ledgerId">
          <ledger-selector v-model="formData.ledgerId" />
        </n-form-item>

        <!-- 分类选择（转账不显示） -->
        <n-form-item v-if="formData.type !== 'transfer'" label="分类" path="categoryId">
          <category-selector v-model="formData.categoryId" />
        </n-form-item>

        <!-- 账户选择 -->
        <n-form-item v-if="formData.type !== 'transfer'" label="账户" path="accountId">
          <account-selector v-model="formData.accountId" />
        </n-form-item>

        <!-- 转账专用字段 -->
        <template v-if="formData.type === 'transfer'">
          <n-form-item label="转出账户" path="accountId">
            <account-selector v-model="formData.accountId" placeholder="选择转出账户" />
          </n-form-item>

          <n-form-item label="转入账户" path="toAccountId">
            <account-selector v-model="formData.toAccountId" placeholder="选择转入账户" />
          </n-form-item>
        </template>

        <!-- 交易日期 -->
        <n-form-item label="交易日期" path="date">
          <n-date-picker
            v-model:value="formData.date"
            type="datetime"
            placeholder="选择交易日期"
            style="width: 100%"
            :default-value="defaultDate"
          />
        </n-form-item>

        <!-- 备注 -->
        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="添加备注（可选）"
            maxlength="200"
            show-count
            :rows="3"
          />
        </n-form-item>

        <!-- 图片上传（预留） -->
        <n-form-item label="凭证图片">
          <n-upload
            :action="uploadUrl"
            :multiple="true"
            :max="3"
            accept="image/*"
          >
            <n-button>上传图片</n-button>
          </n-upload>
          <n-text depth="3" style="font-size: 12px; margin-top: 4px; display: block">
            最多上传 3 张图片，支持 OCR 识别（功能开发中）
          </n-text>
        </n-form-item>

        <n-divider />

        <n-space justify="end" style="width: 100%">
          <n-button @click="handleBack">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存' : '确认记账' }}
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useTransactionStore } from '@/stores/transaction'
import { useBookStore } from '@/stores/book'
import TypeSwitch from '@/components/transaction/TypeSwitch.vue'
import LedgerSelector from '@/components/transaction/LedgerSelector.vue'
import CategorySelector from '@/components/transaction/CategorySelector.vue'
import AccountSelector from '@/components/transaction/AccountSelector.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const txStore = useTransactionStore()
const bookStore = useBookStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const defaultDate = new Date()

const formData = reactive({
  type: 'expense',
  amount: null,
  ledgerId: null,
  categoryId: null,
  accountId: null,
  toAccountId: null,
  date: defaultDate,
  note: '',
  images: []
})

const formRules = {
  amount: [
    {
      required: true,
      message: '请输入金额',
      trigger: 'blur'
    },
    {
      type: 'number',
      min: 0.01,
      message: '金额必须大于 0',
      trigger: 'blur'
    }
  ],
  ledgerId: [
    {
      required: true,
      message: '请选择账本',
      trigger: 'change'
    }
  ],
  categoryId: [
    {
      required: true,
      message: '请选择分类',
      trigger: 'change',
      validator: (rule, value) => {
        if (formData.type === 'transfer') return true
        return !!value
      }
    }
  ],
  accountId: [
    {
      required: true,
      message: '请选择账户',
      trigger: 'change'
    }
  ],
  toAccountId: [
    {
      required: true,
      message: '请选择转入账户',
      trigger: 'change',
      validator: (rule, value) => {
        if (formData.type !== 'transfer') return true
        return !!value
      }
    }
  ]
}

// 上传地址（预留）
const uploadUrl = '/api/upload'

// 返回
const handleBack = () => {
  router.back()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      type: formData.type,
      amount: formData.amount,
      ledgerId: formData.ledgerId,
      categoryId: formData.type === 'transfer' ? null : formData.categoryId,
      accountId: formData.accountId,
      toAccountId: formData.type === 'transfer' ? formData.toAccountId : null,
      date: new Date(formData.date).toISOString(),
      note: formData.note.trim(),
      images: formData.images
    }

    if (isEdit.value) {
      await txStore.updateTransaction(route.params.id, data)
      message.success('交易已更新')
    } else {
      await txStore.createTransaction(data)
      message.success('记账成功！')
    }

    router.back()
  } catch (error) {
    if (error?.errors) return
    message.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 加载编辑数据
const loadEditData = async () => {
  if (!isEdit.value) return
  
  try {
    const tx = await txStore.fetchCurrentTransaction(route.params.id)
    if (tx) {
      formData.type = tx.type
      formData.amount = tx.amount
      formData.ledgerId = tx.ledgerId
      formData.categoryId = tx.categoryId
      formData.accountId = tx.accountId
      formData.toAccountId = tx.toAccountId
      formData.date = new Date(tx.date)
      formData.note = tx.note || ''
      formData.images = tx.images || []
    }
  } catch (error) {
    message.error('加载交易失败：' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  // 并行加载基础数据
  Promise.all([
    bookStore.fetchBookList(),
    loadEditData()
  ]).catch(error => {
    message.error('加载失败：' + (error.message || '未知错误'))
  })
})
</script>

<style scoped>
.transaction-form {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
</style>
