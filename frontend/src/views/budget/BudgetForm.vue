<template>
  <div class="budget-form">
    <n-card>
      <template #header>
        <n-space align="center">
          <n-button quaternary circle @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
          </n-button>
          <h2 style="margin: 0">
            {{ isEdit ? '✏️ 编辑预算' : '🎯 新建预算' }}
          </h2>
        </n-space>
      </template>

      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
        <!-- 预算名称 -->
        <n-form-item label="预算名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入预算名称（如：餐饮预算、购物预算）"
            maxlength="30"
            show-count
          />
        </n-form-item>

        <!-- 预算类型 -->
        <n-form-item label="预算类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-space>
              <n-radio value="category">🏷️ 分类预算</n-radio>
              <n-radio value="account">💳 账户预算</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <!-- 选择分类/账户 -->
        <n-form-item :label="formData.type === 'category' ? '选择分类' : '选择账户'" path="targetIds">
          <n-select
            v-model:value="formData.targetIds"
            :options="targetOptions"
            multiple
            placeholder="请选择（支持多选）"
          />
        </n-form-item>

        <!-- 预算金额 -->
        <n-form-item label="预算金额" path="amount">
          <n-input-number
            v-model:value="formData.amount"
            :min="1"
            :max="999999999"
            placeholder="输入预算金额"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <!-- 预算周期 -->
        <n-form-item label="预算周期" path="period">
          <n-radio-group v-model:value="formData.period">
            <n-space>
              <n-radio value="week">📅 每周</n-radio>
              <n-radio value="month">📆 每月</n-radio>
              <n-radio value="year">📊 每年</n-radio>
            </n-space>
          </n-radio-group>
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
import { useBudgetStore } from '@/stores/budget'
import { useCategoryStore } from '@/stores/category'
import { useAccountStore } from '@/stores/account'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const budgetStore = useBudgetStore()
const categoryStore = useCategoryStore()
const accountStore = useAccountStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  name: '',
  type: 'category',
  targetIds: [],
  amount: null,
  period: 'month',
  startDate: new Date()
})

const formRules = {
  name: [
    {
      required: true,
      message: '请输入预算名称',
      trigger: 'blur'
    },
    {
      min: 1,
      max: 30,
      message: '名称长度 1-30 个字符',
      trigger: 'blur'
    }
  ],
  type: [
    {
      required: true,
      message: '请选择预算类型',
      trigger: 'change'
    }
  ],
  targetIds: [
    {
      type: 'array',
      required: true,
      message: '请至少选择一个目标',
      trigger: 'change'
    }
  ],
  amount: [
    {
      required: true,
      message: '请输入预算金额',
      trigger: 'blur'
    },
    {
      type: 'number',
      min: 1,
      message: '金额必须大于 0',
      trigger: 'blur'
    }
  ],
  period: [
    {
      required: true,
      message: '请选择预算周期',
      trigger: 'change'
    }
  ]
}

// 目标选项（分类或账户）
const targetOptions = computed(() => {
  if (formData.type === 'category') {
    return categoryStore.categoryList.map(cat => ({
      label: cat.name,
      value: cat.id
    }))
  }
  return accountStore.accountList.map(acc => ({
    label: acc.name,
    value: acc.id
  }))
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
      type: formData.type,
      targetIds: formData.targetIds,
      amount: formData.amount,
      period: formData.period,
      startDate: new Date(formData.startDate).toISOString()
    }

    if (isEdit.value) {
      await budgetStore.updateBudget(route.params.id, data)
      message.success('预算已更新')
    } else {
      await budgetStore.createBudget(data)
      message.success('预算已创建')
    }

    router.push('/budget')
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
    const budget = await budgetStore.fetchCurrentBudget(route.params.id)
    if (budget) {
      formData.name = budget.name
      formData.type = budget.type
      formData.targetIds = budget.targetIds || []
      formData.amount = budget.amount
      formData.period = budget.period
      formData.startDate = new Date(budget.startDate)
    }
  } catch (error) {
    message.error('加载预算失败：' + (error.message || '未知错误'))
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
.budget-form {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
</style>
