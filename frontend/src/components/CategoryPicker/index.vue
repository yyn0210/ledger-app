<template>
  <n-select
    v-model:value="selectedValue"
    :options="categoryOptions"
    :placeholder="placeholder"
    :clearable="clearable"
    @update:value="handleChange"
  />
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: [String, Number],
  type: {
    type: String,
    default: 'expense',
    validator: value => ['income', 'expense'].includes(value)
  },
  placeholder: {
    type: String,
    default: '请选择分类'
  },
  clearable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)

watch(() => props.modelValue, val => {
  selectedValue.value = val
})

const expenseCategories = [
  { label: '餐饮', value: 'food' },
  { label: '交通', value: 'transport' },
  { label: '购物', value: 'shopping' },
  { label: '娱乐', value: 'entertainment' },
  { label: '居住', value: 'housing' },
  { label: '通讯', value: 'communication' },
  { label: '医疗', value: 'medical' },
  { label: '教育', value: 'education' },
  { label: '其他', value: 'other' }
]

const incomeCategories = [
  { label: '工资', value: 'salary' },
  { label: '奖金', value: 'bonus' },
  { label: '投资', value: 'investment' },
  { label: '兼职', value: 'parttime' },
  { label: '其他', value: 'other' }
]

const categoryOptions = computed(() => {
  return props.type === 'expense' ? expenseCategories : incomeCategories
})

const handleChange = value => {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>
