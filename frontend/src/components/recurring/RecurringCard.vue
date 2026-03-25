<template>
  <n-card class="recurring-card" :style="{ borderLeft: `4px solid ${statusColor}` }">
    <template #header>
      <n-space justify="space-between" align="center">
        <n-space>
          <div class="recurring-icon" :style="{ backgroundColor: statusColor }">
            <n-icon :component="iconComponent" size="22" color="#fff" />
          </div>
          <div>
            <n-text strong style="font-size: 16px">{{ bill.name }}</n-text>
            <n-text depth="3" style="font-size: 12px; display: block">
              {{ recurringTypeLabel }} · {{ nextExecutionLabel }}
            </n-text>
          </div>
        </n-space>
        <n-tag :type="statusTagType" size="large">
          {{ statusText }}
        </n-tag>
      </n-space>
    </template>

    <n-space vertical>
      <!-- 基本信息 -->
      <n-grid :cols="2" :x-gap="12">
        <n-grid-item>
          <n-statistic label="金额">
            <n-text :type="amountColor" strong>
              {{ transactionType === 1 ? '+' : '-' }}¥{{ formatAmount(bill.amount) }}
            </n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="分类">
            <n-text strong>{{ bill.categoryName || '-' }}</n-text>
          </n-statistic>
        </n-grid-item>
      </n-grid>

      <!-- 执行信息 -->
      <n-space justify="space-between" style="margin-top: 8px">
        <n-space vertical>
          <n-text depth="3" style="font-size: 12px">下次执行</n-text>
          <n-text :type="upcomingColor" strong>{{ formatNextDate }}</n-text>
        </n-space>
        <n-space vertical align="end">
          <n-text depth="3" style="font-size: 12px">已执行</n-text>
          <n-text strong>{{ bill.executionCount || 0 }}{{ maxExecutionsText }}</n-text>
        </n-space>
      </n-space>

      <!-- 操作按钮 -->
      <n-space v-if="showActions" justify="end" style="margin-top: 12px" wrap>
        <n-button
          v-if="bill.status === 1"
          size="small"
          @click="$emit('execute', bill)"
        >
          <template #icon>
            <n-icon :component="PlayOutline" />
          </template>
          执行
        </n-button>
        <n-button
          size="small"
          :type="bill.status === 1 ? 'warning' : 'success'"
          secondary
          @click="$emit('toggle-status', bill)"
        >
          {{ bill.status === 1 ? '暂停' : '恢复' }}
        </n-button>
        <n-button size="small" @click="$emit('edit', bill)">
          <template #icon>
            <n-icon :component="CreateOutline" />
          </template>
          编辑
        </n-button>
        <n-button
          size="small"
          type="error"
          secondary
          @click="$emit('delete', bill)"
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
  PlayOutline,
  CalendarOutline,
  RepeatOutline
} from '@vicons/ionicons5'

const props = defineProps({
  bill: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['edit', 'delete', 'execute', 'toggle-status'])

// 周期类型标签
const recurringTypeLabel = computed(() => {
  const map = {
    1: '每日',
    2: '每周',
    3: '每两周',
    4: '每月',
    5: '每季度',
    6: '每年'
  }
  return map[props.bill.recurringType] || '每月'
})

// 下次执行标签
const nextExecutionLabel = computed(() => {
  if (!props.bill.nextExecutionDate) return ''
  const next = new Date(props.bill.nextExecutionDate)
  const today = new Date()
  const diff = Math.floor((next - today) / (1000 * 60 * 60 * 24))
  
  if (diff < 0) return '已过期'
  if (diff === 0) return '今天执行'
  if (diff === 1) return '明天执行'
  if (diff <= 7) return `${diff}天后执行`
  return next.toLocaleDateString('zh-CN')
})

// 状态判断
const statusText = computed(() => {
  const map = { 1: '执行中', 2: '已暂停', 3: '已完成', 4: '已取消' }
  return map[props.bill.status] || '未知'
})

const statusTagType = computed(() => {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'error' }
  return map[props.bill.status] || 'default'
})

const statusColor = computed(() => {
  const map = { 1: '#10B981', 2: '#F97316', 3: '#6B7280', 4: '#EF4444' }
  return map[props.bill.status] || '#6B7280'
})

// 图标
const iconComponent = computed(() => {
  return props.bill.transactionType === 1 ? CalendarOutline : RepeatOutline
})

// 金额颜色
const amountColor = computed(() => {
  return props.bill.transactionType === 1 ? 'success' : 'error'
})

// 即将执行颜色
const upcomingColor = computed(() => {
  if (!props.bill.nextExecutionDate) return 'default'
  const next = new Date(props.bill.nextExecutionDate)
  const today = new Date()
  const diff = Math.floor((next - today) / (1000 * 60 * 60 * 24))
  
  if (diff < 0) return 'default'
  if (diff === 0) return 'error'
  if (diff <= 3) return 'warning'
  return 'default'
})

// 格式化下次执行日期
const formatNextDate = computed(() => {
  if (!props.bill.nextExecutionDate) return '-'
  return new Date(props.bill.nextExecutionDate).toLocaleDateString('zh-CN')
})

// 最大执行次数文本
const maxExecutionsText = computed(() => {
  if (props.bill.maxExecutions) {
    return `/${props.bill.maxExecutions}次`
  }
  return '次'
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.recurring-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.recurring-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.recurring-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
