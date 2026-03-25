<template>
  <div class="account-type-icon" :style="{ backgroundColor: typeConfig.color }">
    <n-icon :component="iconComponent" size="24" color="#fff" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  CashOutline,
  BankOutline,
  CardOutline,
  LogoAlipay,
  LogoWechat,
  BoxOutline
} from '@vicons/ionicons5'

const props = defineProps({
  type: {
    type: String,
    default: 'other'
  },
  size: {
    type: Number,
    default: 40
  }
})

// 账户类型配置（符合设计规范）
const typeConfigMap = {
  cash: { label: '现金', icon: CashOutline, color: '#10b981' },
  bank: { label: '银行卡', icon: BankOutline, color: '#3b82f6' },
  credit: { label: '信用卡', icon: CardOutline, color: '#f59e0b' },
  alipay: { label: '支付宝', icon: LogoAlipay, color: '#1677ff' },
  wechat: { label: '微信支付', icon: LogoWechat, color: '#07c160' },
  other: { label: '其他', icon: BoxOutline, color: '#64748b' }
}

// 导出配置供外部使用
export { typeConfigMap }

const typeConfig = computed(() => {
  return typeConfigMap[props.type] || typeConfigMap.other
})

const iconComponent = computed(() => {
  return typeConfig.value.icon
})

// 导出配置供外部使用
export { typeConfigMap }
</script>

<style scoped>
.account-type-icon {
  width: v-bind('size + "px"');
  height: v-bind('size + "px"');
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
