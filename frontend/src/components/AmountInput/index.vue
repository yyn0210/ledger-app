<template>
  <n-input-number v-model:value="modelValue" :min="0" :step="0.01" :precision="2" placeholder="请输入金额" style="width: 100%">
    <template #prefix>¥</template>
  </n-input-number>
</template>

<script setup>
const modelValue = defineModel({ type: Number, default: 0 })
</script>
