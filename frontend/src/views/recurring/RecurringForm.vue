<template>
  <div class="recurring-form">
    <n-card>
      <template #header>
        <n-space align="center">
          <n-button quaternary circle @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
          </n-button>
          <h2 style="margin: 0">
            {{ isEdit ? '✏️ 编辑周期账单' : '📅 新建周期账单' }}
          </h2>
        </n-space>
      </template>

      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
        <!-- 账单名称 -->
        <n-form-item label="账单名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入账单名称（如：房租、订阅费）"
            maxlength="30"
            show-count
          />
        </n-form-item>

        <!-- 周期类型 -->
        <n-form-item label="周期类型" path="recurringType">
          <n-select
            v-model:value="formData.recurringType"
            :options="[
              { label: '每日', value: 1 },
              { label: '每周', value: 2 },
              { label: '每两周', value: 3 },
              { label: '每月', value: 4 },
              { label: '每季度', value: 5 },
              { label: '每年', value: 6 }
            ]"
            placeholder="请选择周期类型"
          />
        </n-form-item>

        <!-- 周期值 -->
        <n-form-item
          v-if="formData.recurringType === 2 || formData.recurringType === 3"
          label="每周的第几天"
          path="recurringValue"
        >
          <n-select
            v-model:value="formData.recurringValue"
            :options="[
              { label: '周一', value: 1 },
              { label: '周二', value: 2 },
              { label: '周三', value: 3 },
              { label: '周四', value: 4 },
              { label: '周五', value: 5 },
              { label: '周六', value: 6 },
              { label: '周日', value: 7 }
            ]"
            placeholder="请选择"
          />
        </n-form-item>

        <n-form-item
          v-else-if="formData.recurringType === 4 || formData.recurringType === 5"
          label="每月的第几天"
          path="recurringValue"
        >
          <n-input-number
            v-model:value="formData.recurringValue"
            :min="1"
            :max="31"
            placeholder="1-31"
            style="width: 100%"
          />
        </n-form-item>

        <!-- 金额 -->
        <n-form-item label="金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            :min="0.01"
            :max="999999999"
            placeholder="输入金额"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <!-- 交易类型 -->
        <n-form-item label="交易类型" path="transactionType">
          <n-radio-group v-model:value="formData.transactionType">
            <n-space>
              <n-radio :value="1">💰 收入</n-radio>
              <n-radio :value="2">💸 支出</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <!-- 选择分类 -->
        <n-form-item label="分类" path="categoryId">
          <n-select
            v-model:value="formData.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>

        <!-- 选择账户 -->
        <n-form-item label="账户" path="accountId">
          <n-select
            v-model:value="formData.accountId"
            :options="accountOptions"
            placeholder="请选择账户"
          />
        </n-form-item>

        <!-- 开始日期 -->
        <n-form-item label="开始日期" path="startDate">
          <n-date-picker
            v-model:value="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </n-form-item>

        <!-- 结束日期（可选） -->
        <n-form-item label="结束日期（可选）" path="endDate">
          <n-date-picker
            v-model:value="formData.endDate"
            type="date"
            placeholder="选择结束日期（留空表示无限期）"
            style="width: 100%"
            clearable
          />
        </n-form-item>

        <!-- 最大执行次数（可选） -->
        <n-form-item label="最大执行次数（可选）" path="maxExecutions">
          <n-input-number
            v-model:value="formData.maxExecutions"
            :min="1"
            :max="9999"
            placeholder="留空表示无限次"
            style="width: 100%"
          />
        </n-form-item>

        <!-- 商户（可选） -->
        <n-form-item label="商户（可选）" path="merchant">
          <n-input
            v-model:value="formData.merchant"
            placeholder="输入商户名称"
            maxlength="50"
          />
        </n-form-item>

        <!-- 备注（可选） -->
        <n-form-item label="备注（可选）" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="输入备注信息"
            maxlength="200"
            show-count
            :rows="3"
          />
        </n-form-item>

        <!-- 是否自动执行 -->
        <n-form-item label="自动执行" path="autoExecute">
          <n-switch v-model:value="formData.autoExecute">
            <template #checked>开启</template>
            <template #unchecked>关闭</template>
          </n-switch>
          <n-text depth="3" style="margin-left: 12px">
            开启后将在到期日自动创建交易记录
          </n-text>
        </n-form-item>

        <n-divider />

        <n-space justify="end" style="width: 100%">
          <n-button @click="handleBack">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存' : '创建' }}
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
import { useRecurringBillStore } from '@/stores/recurring'
import { useCategoryStore } from '@/stores/category'
import { useAccountStore } from '@/stores/account'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const recurringBillStore = useRecurringBillStore()
const categoryStore = useCategoryStore()
const accountStore = useAccountStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  name: '',
  recurringType: 4, // 默认每月
  recurringValue: 1,
  amount: null,
  transactionType: 2, // 默认支出
  categoryId: null,
  accountId: null,
  startDate: new Date(),
  endDate: null,
  maxExecutions: null,
  merchant: '',
  note: '',
  autoExecute: true
})

const formRules = {
  name: [
    { required: true, message: '请输入账单名称', trigger: 'blur' },
    { min: 1, max: 30, message: '名称长度 1-30 个字符', trigger: 'blur' }
  ],
  recurringType: [
    { required: true, message: '请选择周期类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于 0', trigger: 'blur' }
  ],
  transactionType: [
    { required: true, message: '请选择交易类型', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  accountId: [
    { required: true, message: '请选择账户', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ]
}

// 分类选项
const categoryOptions = computed(() => {
  const type = formData.transactionType === 1 ? 'income' : 'expense'
  return categoryStore.categoryList
    .filter(cat => cat.type === type)
    .map(cat => ({ label: cat.name, value: cat.id }))
})

// 账户选项
const accountOptions = computed(() => {
  return accountStore.accountList.map(acc => ({ label: acc.name, value: acc.id }))
})

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
      name: formData.name.trim(),
      recurringType: formData.recurringType,
      recurringValue: formData.recurringValue,
      amount: formData.amount,
      transactionType: formData.transactionType,
      categoryId: formData.categoryId,
      accountId: formData.accountId,
      startDate: new Date(formData.startDate).toISOString().split('T')[0],
      endDate: formData.endDate ? new Date(formData.endDate).toISOString().split('T')[0] : null,
      maxExecutions: formData.maxExecutions,
      merchant: formData.merchant.trim(),
      note: formData.note.trim(),
      autoExecute: formData.autoExecute
    }

    if (isEdit.value) {
      await recurringBillStore.updateRecurringBill(route.params.id, data)
      message.success('周期账单已更新')
    } else {
      await recurringBillStore.createRecurringBill(data)
      message.success('周期账单已创建')
    }

    router.push('/recurring')
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
    const bill = await recurringBillStore.fetchCurrentRecurringBill(route.params.id)
    if (bill) {
      formData.name = bill.name
      formData.recurringType = bill.recurringType
      formData.recurringValue = bill.recurringValue
      formData.amount = bill.amount
      formData.transactionType = bill.transactionType
      formData.categoryId = bill.categoryId
      formData.accountId = bill.accountId
      formData.startDate = new Date(bill.startDate)
      formData.endDate = bill.endDate ? new Date(bill.endDate) : null
      formData.maxExecutions = bill.maxExecutions
      formData.merchant = bill.merchant
      formData.note = bill.note
      formData.autoExecute = bill.autoExecute
    }
  } catch (error) {
    message.error('加载周期账单失败：' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  Promise.all([
    categoryStore.fetchCategoryList(),
    accountStore.fetchAccountList(),
    loadEditData()
  ]).catch(error => {
    message.error('加载失败：' + (error.message || '未知错误'))
  })
})
</script>

<style scoped>
.recurring-form {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
</style>
