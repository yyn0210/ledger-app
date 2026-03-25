<template>
  <n-card class="budget-card" :style="{ borderLeft: `4px solid ${statusColor}` }">
    <template #header>
      <n-space justify="space-between" align="center">
        <n-space>
          <div class="budget-icon" :style="{ backgroundColor: statusColor }">
            <n-icon :component="iconComponent" size="22" color="#fff" />
          </div>
          <div>
            <n-text strong style="font-size: 16px">{{ budget.name }}</n-text>
            <n-text depth="3" style="font-size: 12px; display: block">
              {{ typeLabel }} · {{ periodLabel }}
            </n-text>
          </div>
        </n-space>
        <n-tag :type="statusTagType" size="large">
          {{ statusText }}
        </n-tag>
      </n-space>
    </template>

    <n-space vertical>
      <!-- 目标分类/账户 -->
      <n-space v-if="budget.targetNames?.length" wrap>
        <n-tag
          v-for="name in budget.targetNames"
          :key="name"
          size="small"
          :bordered="false"
        >
          {{ name }}
        </n-tag>
      </n-space>

      <!-- 进度条 -->
      <budget-progress
        :used="budget.usedAmount || 0"
        :total="budget.amount"
        :show-details="true"
      />

      <!-- 统计信息 -->
      <n-grid :cols="3" :x-gap="12">
        <n-grid-item>
          <n-statistic label="预算金额">
            <n-text strong>¥{{ formatAmount(budget.amount) }}</n-text>
          </n-statistic>
        </n-grid-item>
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
      </n-grid>

      <!-- 操作按钮 -->
      <n-space v-if="showActions" justify="end" style="margin-top: 12px">
        <n-button size="small" @click="$emit('edit', budget)">
          <template #icon>
            <n-icon :component="CreateOutline" />
          </template>
          编辑
        </n-button>
        <n-button
          size="small"
          type="error"
          secondary
          @click="$emit('delete', budget)"
        >
          <template #icon>
            <n-icon :component="TrashOutline" />
          </template>
          删除
        </n-button>
      </n-space>
    </n-space>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import {
  CreateOutline,
  TrashOutline,
  WalletOutline,
  PieChartOutline
} from '@vicons/ionicons5'
import BudgetProgress from './BudgetProgress.vue'

const props = defineProps({
  budget: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['edit', 'delete'])

// 类型标签
const typeLabel = computed(() => {
  return props.budget.type === 'category' ? '分类预算' : '账户预算'
})

// 周期标签
const periodLabel = computed(() => {
  const map = {
    week: '每周',
    month: '每月',
    year: '每年'
  }
  return map[props.budget.period] || '每月'
})

// 图标
const iconComponent = computed(() => {
  return props.budget.type === 'category' ? PieChartOutline : WalletOutline
})

// 状态判断
const isOverBudget = computed(() => {
  return (props.budget.usedAmount || 0) > props.budget.amount
})

const isWarning = computed(() => {
  const pct = ((props.budget.usedAmount || 0) / props.budget.amount) * 100
  return pct >= 90 && pct <= 100
})

// 状态颜色
const statusColor = computed(() => {
  if (isOverBudget.value) return '#EF4444'
  if (isWarning.value) return '#F97316'
  return '#10B981'
})

// 状态标签类型
const statusTagType = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'success'
})

// 状态文字
const statusText = computed(() => {
  if (isOverBudget.value) return '超支'
  if (isWarning.value) return '预警'
  return '正常'
})

// 已用金额颜色
const usedColor = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'default'
})

// 剩余金额颜色
const remainingColor = computed(() => {
  if (isOverBudget.value) return 'error'
  if (isWarning.value) return 'warning'
  return 'success'
})

// 剩余金额
const remainingAmount = computed(() => {
  return props.budget.amount - (props.budget.usedAmount || 0)
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.budget-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.budget-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.budget-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
