<template>
  <div class="account-selector">
    <n-select
      v-model:value="selectedId"
      :options="accountOptions"
      :placeholder="placeholder"
      :disabled="disabled"
      label-field="name"
      value-field="id"
      @update:value="$emit('update:modelValue', $event)"
    >
      <template #option="{ option }">
        <div class="account-option">
          <account-type-icon :type="option.type" :size="28" />
          <div class="account-info">
            <n-text strong>{{ option.name }}</n-text>
            <n-text depth="3" style="font-size: 12px">
              {{ option.typeLabel }}
            </n-text>
          </div>
          <n-text v-if="showBalance" :type="option.balance >= 0 ? 'default' : 'error'">
            ¥{{ formatAmount(option.balance) }}
          </n-text>
        </div>
      </template>

      <template #tag="{ option }">
        <div class="account-tag">
          <account-type-icon :type="option.type" :size="20" />
          <n-text style="margin-left: 6px">{{ option.name }}</n-text>
        </div>
      </template>
    </n-select>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AccountTypeIcon from '@/components/account/AccountTypeIcon.vue'
import { useAccountStore } from '@/stores/account'
import { typeConfigMap } from '@/components/account/AccountTypeIcon.vue'

const props = defineProps({
  modelValue: Number,
  placeholder: {
    type: String,
    default: '选择账户'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showBalance: {
    type: Boolean,
    default: true
  },
  filterType: {
    type: String,
    default: 'all'
  }
})

const emit = defineEmits(['update:modelValue'])

const accountStore = useAccountStore()

const selectedId = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const accountOptions = computed(() => {
  let accounts = accountStore.accountList
  
  // 类型筛选
  if (props.filterType !== 'all') {
    accounts = accounts.filter(acc => acc.type === props.filterType)
  }
  
  return accounts.map(acc => ({
    id: acc.id,
    name: acc.name,
    type: acc.type,
    typeLabel: typeConfigMap[acc.type]?.label || '其他',
    balance: acc.balance || 0,
    color: acc.color
  }))
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.account-selector {
  width: 100%;
}

.account-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.account-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.account-tag {
  display: flex;
  align-items: center;
}
</style>
