<template>
  <div class="type-switch">
    <n-segmented
      :value="modelValue"
      :options="options"
      @update:value="$emit('update:modelValue', $event)"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: 'expense',
    validator: value => ['expense', 'income', 'transfer'].includes(value)
  }
})

defineEmits(['update:modelValue'])

const options = computed(() => [
  {
    label: '💸 支出',
    value: 'expense'
  },
  {
    label: '💰 收入',
    value: 'income'
  },
  {
    label: '💱 转账',
    value: 'transfer'
  }
])
</script>

<style scoped>
.type-switch {
  width: 100%;
}

:deep(.n-segmented) {
  width: 100%;
}

:deep(.n-segmented-option) {
  flex: 1;
}
</style>
