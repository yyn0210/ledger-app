<template>
<<<<<<< HEAD
  <n-input-number
    v-model:value="amountValue"
    :placeholder="placeholder"
    :min="min"
    :max="max"
    :precision="2"
    :step="0.01"
    :disabled="disabled"
    @update:value="handleChange"
  >
    <template #prefix><span>¥</span></template>
  </n-input-number>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: Number,
  placeholder: { type: String, default: '请输入金额' },
  min: { type: Number, default: 0 },
  max: { type: Number, default: 999999999 },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change'])
const amountValue = ref(props.modelValue)

watch(() => props.modelValue, val => { amountValue.value = val })
const handleChange = value => { emit('update:modelValue', value); emit('change', value) }
</script>

<style scoped>
:deep(.n-input-number) { width: 100%; }
=======
  <div class="amount-input">
    <div class="amount-display" :class="{ focus: isFocus, error: isError }">
      <span class="currency-symbol">¥</span>
      <input
        ref="inputRef"
        v-model="displayValue"
        class="amount-input-field"
        :placeholder="placeholder"
        type="text"
        :disabled="disabled"
        @focus="handleFocus"
        @blur="handleBlur"
        @input="handleInput"
      />
    </div>
    <div v-if="showQuickAmounts" class="quick-amounts">
      <span
        v-for="amount in quickAmounts"
        :key="amount"
        class="quick-amount-btn"
        @click="selectQuickAmount(amount)"
      >
        {{ amount }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: Number,
  placeholder: {
    type: String,
    default: '0.00'
  },
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 999999999
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showQuickAmounts: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const inputRef = ref(null)
const isFocus = ref(false)
const isError = ref(false)
const displayValue = ref('')

const quickAmounts = [10, 20, 50, 100, 200, 500]

watch(() => props.modelValue, val => {
  if (val !== null && val !== undefined) {
    displayValue.value = formatAmount(val)
  } else {
    displayValue.value = ''
  }
}, { immediate: true })

const formatAmount = (value) => {
  if (value === null || value === undefined) return ''
  return value.toFixed(2)
}

const parseAmount = (str) => {
  if (!str) return null
  const num = parseFloat(str.replace(/,/g, ''))
  if (isNaN(num)) return null
  return num
}

const handleFocus = () => {
  isFocus.value = true
  isError.value = false
}

const handleBlur = () => {
  isFocus.value = false
  const value = parseAmount(displayValue.value)
  if (value !== null && value < props.min) {
    isError.value = true
  }
  emit('update:modelValue', value)
  emit('change', value)
}

const handleInput = (e) => {
  const value = e.target.value
  // 只允许输入数字和小数点
  if (!/^\d*\.?\d{0,2}$/.test(value)) {
    e.target.value = displayValue.value
    return
  }
  displayValue.value = value
}

const selectQuickAmount = (amount) => {
  displayValue.value = amount.toFixed(2)
  emit('update:modelValue', amount)
  emit('change', amount)
  inputRef.value?.focus()
}
</script>

<style scoped>
.amount-input {
  width: 100%;
}

.amount-display {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
}

.amount-display.focus {
  border-color: #3385ff;
  box-shadow: 0 0 0 3px rgba(51, 133, 255, 0.1);
}

.amount-display.error {
  border-color: #ff6b6b;
}

.currency-symbol {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.amount-input-field {
  flex: 1;
  border: none;
  outline: none;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  background: transparent;
}

.amount-input-field::placeholder {
  color: #ccc;
  font-weight: 400;
}

.amount-input-field:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.quick-amount-btn {
  padding: 6px 16px;
  background: #f5f6f7;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-amount-btn:hover {
  background: #e8eaed;
  color: #3385ff;
}

.quick-amount-btn:active {
  transform: scale(0.95);
}
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
</style>
