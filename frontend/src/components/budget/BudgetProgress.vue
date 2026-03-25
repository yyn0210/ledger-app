<template>
  <div class="budget-progress">
    <!-- 进度条 -->
    <n-progress
      :type="type"
      :percentage="percentage"
      :color="progressColor"
      :show-indicator="showIndicator"
      :height="height"
      :indicator-text-color="indicatorTextColor"
    >
      <template v-if="type === 'line' && showIndicator" #indicator>
        <n-text :style="{ color: indicatorTextColor, fontSize: '12px' }">
          {{ formatAmount(used) }} / {{ formatAmount(total) }}
        </n-text>
      </template>
    </n-progress>

    <!-- 详细信息 -->
    <n-space v-if="showDetails" justify="space-between" style="margin-top: 8px">
      <n-space vertical>
        <n-text depth="3" style="font-size: 12px">已用</n-text>
        <n-text :type="usedColor" strong>{{ formatAmount(used) }}</n-text>
      </n-space>

      <n-space vertical align="end">
        <n-text depth="3" style="font-size: 12px">剩余</n-text>
        <n-text :type="remainingColor" strong>{{ formatAmount(remaining) }}</n-text>
      </n-space>

      <n-space vertical align="end">
        <n-text depth="3" style="font-size: 12px">预算</n-text>
        <n-text strong>{{ formatAmount(total) }}</n-text>
      </n-space>
    </n-space>

    <!-- 超支警告 -->
    <n-alert
      v-if="isOverBudget"
      type="error"
      title="⚠️ 已超支"
      style="margin-top: 12px"
      :bordered="false"
    >
      已超出预算 {{ formatAmount(overAmount) }}
    </n-alert>

    <!-- 即将超支警告 -->
    <n-alert
      v-else-if="isWarning"
      type="warning"
      title="⚠️ 即将超支"
      style="margin-top: 12px"
      :bordered="false"
    >
      仅剩 {{ formatAmount(remaining) }} 可用
    </n-alert>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  used: {
    type: Number,
    default: 0
  },
  total: {
    type: Number,
    default: 0
  },
  type: {
    type: String,
    default: 'line', // 'line' | 'circle'
    validator: value => ['line', 'circle'].includes(value)
  },
  height: {
    type: Number,
    default: 18
  },
  showIndicator: {
    type: Boolean,
    default: true
  },
  showDetails: {
    type: Boolean,
    default: true
  }
})

// 计算使用比例
const percentage = computed(() => {
  if (!props.total) return 0
  return Math.min(Math.round((props.used / props.total) * 100), 100)
})

// 剩余金额
const remaining = computed(() => {
  return props.total - props.used
})

// 超支金额
const overAmount = computed(() => {
  return Math.max(props.used - props.total, 0)
})

// 是否超支
const isOverBudget = computed(() => {
  return props.used > props.total
})

// 是否警告（>90%）
const isWarning = computed(() => {
  const pct = percentage.value
  return pct >= 90 && pct <= 100
})

// 进度条颜色
const progressColor = computed(() => {
  const pct = percentage.value
  if (pct >= 100) return '#EF4444' // 红色 - 超支
  if (pct >= 90) return '#F97316' // 橙色 - 警告
  if (pct >= 70) return '#F59E0B' // 黄色 - 注意
  return '#10B981' // 绿色 - 正常
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

// 指示器文字颜色
const indicatorTextColor = computed(() => {
  return '#fff'
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.budget-progress {
  width: 100%;
}
</style>
