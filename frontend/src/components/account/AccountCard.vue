<template>
  <n-card class="account-card" :style="{ borderLeft: `4px solid ${typeConfig.color}` }">
    <template #header>
      <n-space align="center" justify="space-between">
        <n-space align="center">
          <account-type-icon :type="account.type" :size="44" />
          
          <div>
            <n-text strong style="font-size: 16px">{{ account.name }}</n-text>
            <n-text depth="3" style="font-size: 12px; display: block">
              {{ typeConfig.label }}
            </n-text>
          </div>
        </n-space>

        <n-space v-if="showActions" align="center">
          <n-button size="small" quaternary @click="$emit('edit', account)">
            <template #icon>
              <n-icon :component="CreateOutline" />
            </template>
          </n-button>
          <n-button
            size="small"
            quaternary
            type="error"
            @click="$emit('delete', account)"
          >
            <template #icon>
              <n-icon :component="TrashOutline" />
            </template>
          </n-button>
        </n-space>
      </n-space>
    </template>

    <n-space vertical>
      <!-- 余额 -->
      <div>
        <n-text depth="3" style="font-size: 12px">当前余额</n-text>
        <balance-display
          :balance="account.balance"
          :show-toggle="true"
          style="margin-top: 4px"
        />
      </div>

      <!-- 信用卡特有信息 -->
      <template v-if="account.type === 'credit'">
        <n-divider style="margin: 12px 0" />

        <n-grid :cols="3" :x-gap="12">
          <n-grid-item>
            <n-text depth="3" style="font-size: 12px">信用额度</n-text>
            <n-text strong>¥{{ formatAmount(account.creditLimit || 0) }}</n-text>
          </n-grid-item>
          <n-grid-item>
            <n-text depth="3" style="font-size: 12px">账单日</n-text>
            <n-text strong>每月{{ account.billDay || '-' }}日</n-text>
          </n-grid-item>
          <n-grid-item>
            <n-text depth="3" style="font-size: 12px">还款日</n-text>
            <n-text strong>每月{{ account.repaymentDay || '-' }}日</n-text>
          </n-grid-item>
        </n-grid>

        <!-- 可用额度 -->
        <n-progress
          v-if="account.creditLimit"
          type="line"
          :percentage="usagePercentage"
          :color="usageColor"
          :show-indicator="true"
          indicator-text-color="#fff"
          style="margin-top: 12px"
        >
          <template #indicator>
            <n-text style="color: #fff; font-size: 12px">
              可用 ¥{{ formatAmount(availableCredit) }}
            </n-text>
          </template>
        </n-progress>
      </template>

      <!-- 备注 -->
      <n-text v-if="account.note" depth="3" style="font-size: 12px">
        📝 {{ account.note }}
      </n-text>
    </n-space>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { CreateOutline, TrashOutline } from '@vicons/ionicons5'
import AccountTypeIcon from './AccountTypeIcon.vue'
import BalanceDisplay from './BalanceDisplay.vue'
import { typeConfigMap } from './AccountTypeIcon.vue'

const props = defineProps({
  account: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['edit', 'delete'])

const typeConfig = computed(() => {
  return typeConfigMap[props.account.type] || typeConfigMap.other
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 信用卡可用额度
const availableCredit = computed(() => {
  if (props.account.type !== 'credit') return 0
  return (props.account.creditLimit || 0) + (props.account.balance || 0)
})

// 信用卡使用比例
const usagePercentage = computed(() => {
  if (!props.account.creditLimit) return 0
  const used = Math.abs(props.account.balance || 0)
  return Math.min(Math.round((used / props.account.creditLimit) * 100), 100)
})

// 进度条颜色
const usageColor = computed(() => {
  const percentage = usagePercentage.value
  if (percentage >= 90) return '#ef4444'
  if (percentage >= 70) return '#f59e0b'
  return '#10b981'
})
</script>

<style scoped>
.account-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.account-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
</style>
