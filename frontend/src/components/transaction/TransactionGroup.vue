<template>
  <div class="transaction-group">
    <!-- 日期头部 -->
    <div class="group-header">
      <n-text strong>{{ groupLabel }}</n-text>
      <n-text depth="3" style="font-size: 12px">
        {{ itemCount }} 笔 · 支出 {{ formatAmount(totalExpense) }} · 收入 {{ formatAmount(totalIncome) }}
      </n-text>
    </div>

    <!-- 交易列表 -->
    <n-space vertical>
      <transaction-item
        v-for="tx in transactions"
        :key="tx.id"
        :transaction="tx"
        :show-category="showCategory"
        :hide-balance="hideBalance"
        @click="$emit('click', $event)"
      />
    </n-space>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TransactionItem from './TransactionItem.vue'

const props = defineProps({
  date: {
    type: String,
    required: true
  },
  transactions: {
    type: Array,
    required: true
  },
  showCategory: {
    type: Boolean,
    default: true
  },
  hideBalance: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])

// 格式化日期标签
const groupLabel = computed(() => {
  const date = new Date(props.date)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  // 今天
  if (date.toDateString() === today.toDateString()) {
    return '今天'
  }
  
  // 昨天
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 本周
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  if (isThisWeek(date)) {
    return weekDays[date.getDay()]
  }

  // 其他日期
  return `${date.getMonth() + 1}月${date.getDate()}日`
})

// 判断是否本周
const isThisWeek = (date) => {
  const now = new Date()
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay())
  startOfWeek.setHours(0, 0, 0, 0)
  
  return date >= startOfWeek
}

// 交易笔数
const itemCount = computed(() => props.transactions.length)

// 总支出
const totalExpense = computed(() => {
  return props.transactions
    .filter(tx => tx.type === 'expense')
    .reduce((sum, tx) => sum + tx.amount, 0)
})

// 总收入
const totalIncome = computed(() => {
  return props.transactions
    .filter(tx => tx.type === 'income')
    .reduce((sum, tx) => sum + tx.amount, 0)
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.transaction-group {
  margin-bottom: 20px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
  padding: 0 4px;
}
</style>
