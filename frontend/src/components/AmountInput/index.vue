<template>
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
</style>
