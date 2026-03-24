<template>
  <n-select v-model:value="modelValue" :options="options" placeholder="请选择分类" />
</template>

<script setup>
import { computed } from 'vue'

const modelValue = defineModel({ type: [Number, String] })
const props = defineProps({
  type: { type: String, default: 'expense' }
})

const expenseCategories = [
  { label: '餐饮', value: 1 },
  { label: '交通', value: 2 },
  { label: '购物', value: 3 },
  { label: '娱乐', value: 4 },
  { label: '居住', value: 5 }
]

const incomeCategories = [
  { label: '工资', value: 11 },
  { label: '奖金', value: 12 },
  { label: '投资', value: 13 }
]

const options = computed(() => props.type === 'income' ? incomeCategories : expenseCategories)
</script>
