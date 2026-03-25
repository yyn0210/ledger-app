<template>
  <div class="budget-detail">
    <n-spin :show="loading">
      <template v-if="budget">
        <!-- 头部操作 -->
        <n-card>
          <n-space justify="space-between" align="center">
            <n-space align="center">
              <n-button quaternary circle @click="handleBack">
                <template #icon>
                  <n-icon :component="ArrowBackOutline" />
                </template>
              </n-button>
              <h2 style="margin: 0">🎯 预算详情</h2>
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

        <!-- 预算概览 -->
        <n-card style="margin-top: 20px">
          <n-space justify="space-between" align="center">
            <div>
              <n-text strong style="font-size: 20px">{{ budget.name }}</n-text>
              <n-space style="margin-top: 8px">
                <n-tag :type="budget.type === 'category' ? 'info' : 'success'">
                  {{ budget.type === 'category' ? '分类预算' : '账户预算' }}
                </n-tag>
                <n-tag :type="periodTagType">
                  {{ periodLabel }}
                </n-tag>
                <n-tag :type="statusTagType">
                  {{ statusText }}
                </n-tag>
              </n-space>
            </div>
            <div class="budget-amount" :class="statusClass">
              <n-text depth="3" style="font-size: 12px; display: block">预算金额</n-text>
              ¥{{ formatAmount(budget.amount) }}
            </div>
          </n-space>
        </n-card>

        <!-- 执行进度 -->
        <n-card style="margin-top: 20px">
          <template #header>
            <n-text strong>执行进度</n-text>
          </template>

          <budget-progress
            :used="budget.usedAmount || 0"
            :total="budget.amount"
            :show-details="true"
          />

          <n-grid :cols="4" :x-gap="16" style="margin-top: 20px">
            <n-grid-item>
              <n-statistic label="已用金额">
                <n-text :type="usedColor" strong>
                  ¥{{ formatAmount(budget.usedAmount || 0) }}
                </n-text>
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="剩余金额">
                <n-text :type="remainingColor" strong>
                  ¥{{ formatAmount(remainingAmount) }}
                </n-text>
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="使用比例">
                <n-text :type="progressColor" strong>
                  {{ progressPercentage }}%
                </n-text>
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="剩余天数">
                <n-text strong>{{ remainingDays }} 天</n-text>
              </n-statistic>
            </n-grid-item>
          </n-grid>
        </n-card>

        <!-- 目标分类/账户 -->
        <n-card v-if="budget.targetNames?.length" style="margin-top: 20px">
          <template #header>
            <n-text strong>{{ budget.type === 'category' ? '预算分类' : '预算账户' }}</n-text>
          </template>
          <n-space wrap>
            <n-tag
              v-for="name in budget.targetNames"
              :key="name"
              size="large"
              :bordered="false"
            >
              {{ name }}
            </n-tag>
          </n-space>
        </n-card>

        <!-- 执行图表 -->
        <budget-chart
          v-if="executionData?.length"
          :data="executionData"
          type="bar"
          title="预算执行趋势"
          style="margin-top: 20px"
        />

        <!-- 基本信息 -->
        <n-card style="margin-top: 20px">
          <template #header>
            <n-text strong>基本信息</n-text>
          </template>
          <n-space vertical>
            <n-space justify="space-between">
              <n-text depth="3">开始日期</n-text>
              <n-text>{{ formatDate(budget.startDate) }}</n-text>
            </n-space>
            <n-space justify="space-between">
              <n-text depth="3">创建时间</n-text>
              <n-text depth="3">{{ formatDate(budget.createdAt) }}</n-text>
            </n-space>
            <n-space justify="space-between">
              <n-text depth="3">更新时间</n-text>
              <n-text depth="3">{{ formatDate(budget.updatedAt) }}</n-text>
            </n-space>
          </n-space>
        </n-card>
      </template>

      <n-empty
        v-else-if="!loading"
        description="预算不存在或已被删除"
        style="padding: 60px 0"
      >
        <template #extra>
          <n-button @click="handleBack">返回预算列表</n-button>
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
import { useBudgetStore } from '@/stores/budget'
import BudgetProgress from '@/components/budget/BudgetProgress.vue'
import BudgetChart from '@/components/budget/BudgetChart.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const budgetStore = useBudgetStore()

const budgetId = computed(() => route.params.id)
const budget = computed(() => budgetStore.currentBudget)
const loading = computed(() => budgetStore.loading)
const executionData = computed(() => budgetStore.executionData)

// 周期标签
const periodLabel = computed(() => {
  const map = { week: '每周', month: '每月', year: '每年' }
  return map[budget.value?.period] || '每月'
})

const periodTagType = computed(() => {
  const map = { week: 'info', month: 'success', year: 'warning' }
  return map[budget.value?.period] || 'default'
})

// 状态判断
const isOverBudget = computed(() => {
  return (budget.value?.usedAmount || 0) > budget.value?.amount
})

const isWarning = computed(() => {
  const pct = ((budget.value?.usedAmount || 0) / budget.value?.amount) * 100
  return pct >= 90 && pct <= 100
})

// 状态标签
const statusText = computed(() => {
  if (isOverBudget.value) return '超支'
  if (isWarning.value) return '预警'
  return '正常'
})

const statusTagType = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'success'
})

const statusClass = computed(() => {
  if (isOverBudget.value) return 'over'
  if (isWarning.value) return 'warning'
  return 'normal'
})

// 颜色
const usedColor = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'default'
})

const remainingColor = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'success'
})

const progressColor = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'default'
})

// 剩余金额
const remainingAmount = computed(() => {
  return (budget.value?.amount || 0) - (budget.value?.usedAmount || 0)
})

// 使用比例
const progressPercentage = computed(() => {
  if (!budget.value?.amount) return 0
  return Math.round(((budget.value.usedAmount || 0) / budget.value.amount) * 100)
})

// 剩余天数（简化计算）
const remainingDays = computed(() => {
  if (!budget.value) return 0
  const now = new Date()
  const start = new Date(budget.value.startDate)
  const periodDays = { week: 7, month: 30, year: 365 }
  const days = periodDays[budget.value.period] || 30
  const elapsed = Math.floor((now - start) / (1000 * 60 * 60 * 24))
  return Math.max(days - (elapsed % days), 0)
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/budget/${budgetId.value}/edit`)
}

// 删除
const handleDelete = () => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这个预算吗？删除后无法恢复！',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await budgetStore.deleteBudget(budgetId.value)
        message.success('预算已删除')
        router.push('/budget')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

onMounted(() => {
  Promise.all([
    budgetStore.fetchCurrentBudget(budgetId.value),
    budgetStore.fetchExecutionData(budgetId.value)
  ]).catch(error => {
    message.error('加载失败：' + (error.message || '未知错误'))
  })
})
</script>

<style scoped>
.budget-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.budget-amount {
  text-align: right;
  font-size: 28px;
  font-weight: 700;
}

.budget-amount.over {
  color: #EF4444;
}

.budget-amount.warning {
  color: #F97316;
}

.budget-amount.normal {
  color: #10B981;
}
</style>
