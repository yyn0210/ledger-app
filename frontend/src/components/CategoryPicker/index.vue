<template>
<<<<<<< HEAD
  <n-select
    v-model:value="selectedValue"
    :options="categoryOptions"
    :placeholder="placeholder"
    :clearable="clearable"
    @update:value="handleChange"
  />
=======
  <div class="category-picker">
    <n-popover v-model:show="showPicker" trigger="manual" placement="bottom-start">
      <template #trigger>
        <div class="category-input" @click="showPicker = true">
          <div v-if="selectedCategory" class="selected-category">
            <span class="category-icon" :style="{ backgroundColor: selectedCategory.color }">
              <n-icon :component="selectedCategory.icon" size="18" color="#fff" />
            </span>
            <span class="category-name">{{ selectedCategory.name }}</span>
          </div>
          <div v-else class="placeholder">
            <n-icon :component="Grid" size="18" />
            <span>{{ placeholder }}</span>
          </div>
          <n-icon :component="ChevronDown" size="18" class="arrow" />
        </div>
      </template>
      
      <div class="category-panel">
        <div class="panel-header">
          <span class="panel-title">{{ type === 'expense' ? '支出分类' : '收入分类' }}</span>
        </div>
        <div class="category-grid">
          <div
            v-for="cat in categoryOptions"
            :key="cat.value"
            class="category-item"
            :class="{ active: modelValue === cat.value }"
            @click="selectCategory(cat)"
          >
            <div class="category-icon-wrapper" :style="{ backgroundColor: cat.color }">
              <n-icon :component="cat.icon" size="24" color="#fff" />
            </div>
            <span class="category-item-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </n-popover>
  </div>
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
</template>

<script setup>
import { ref, computed, watch } from 'vue'
<<<<<<< HEAD

const props = defineProps({
  modelValue: [String, Number],
  type: { type: String, default: 'expense', validator: v => ['income', 'expense'].includes(v) },
  placeholder: { type: String, default: '请选择分类' },
  clearable: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'change'])
const selectedValue = ref(props.modelValue)

watch(() => props.modelValue, val => { selectedValue.value = val })

const expenseCategories = [
  { label: '餐饮', value: 'food' }, { label: '交通', value: 'transport' },
  { label: '购物', value: 'shopping' }, { label: '娱乐', value: 'entertainment' },
  { label: '居住', value: 'housing' }, { label: '其他', value: 'other' }
]

const incomeCategories = [
  { label: '工资', value: 'salary' }, { label: '奖金', value: 'bonus' },
  { label: '投资', value: 'investment' }, { label: '其他', value: 'other' }
]

const categoryOptions = computed(() => props.type === 'expense' ? expenseCategories : incomeCategories)
const handleChange = value => { emit('update:modelValue', value); emit('change', value) }
</script>
=======
import { Grid, ChevronDown } from '@vicons/ionicons5'
import { 
  FastFood, Car, Cart, Beer, Home, Phone, Medical, School, 
  Cash, Gift, TrendingUp, Briefcase, GameController, Restaurant, 
  Subway, Shirt, Heart, Book,ellipsisHorizontal 
} from '@vicons/ionicons5'

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
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const showPicker = ref(false)

const expenseCategories = [
  { name: '餐饮', value: 'food', color: '#ff9900', icon: FastFood },
  { name: '交通', value: 'transport', color: '#3385ff', icon: Car },
  { name: '购物', value: 'shopping', color: '#ff6b6b', icon: Cart },
  { name: '娱乐', value: 'entertainment', color: '#9b59b6', icon: Beer },
  { name: '居住', value: 'housing', color: '#2ecc71', icon: Home },
  { name: '通讯', value: 'communication', color: '#16a085', icon: Phone },
  { name: '医疗', value: 'medical', color: '#e74c3c', icon: Medical },
  { name: '教育', value: 'education', color: '#3498db', icon: School },
  { name: '其他', value: 'other', color: '#95a5a6', icon: ellipsisHorizontal }
]

const incomeCategories = [
  { name: '工资', value: 'salary', color: '#2ecc71', icon: Cash },
  { name: '奖金', value: 'bonus', color: '#f39c12', icon: Gift },
  { name: '投资', value: 'investment', color: '#3498db', icon: TrendingUp },
  { name: '兼职', value: 'parttime', color: '#9b59b6', icon: Briefcase },
  { name: '其他', value: 'other', color: '#95a5a6', icon: ellipsisHorizontal }
]

const categoryOptions = computed(() => {
  return props.type === 'expense' ? expenseCategories : incomeCategories
})

const selectedCategory = computed(() => {
  return categoryOptions.value.find(cat => cat.value === props.modelValue)
})

watch(() => props.modelValue, val => {
  if (val) {
    showPicker.value = false
  }
})

const selectCategory = (category) => {
  emit('update:modelValue', category.value)
  emit('change', category)
  showPicker.value = false
}
</script>

<style scoped>
.category-picker {
  width: 100%;
}

.category-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.category-input:hover {
  border-color: #3385ff;
}

.selected-category {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.category-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.category-name {
  font-size: 14px;
  color: #333;
}

.placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
}

.arrow {
  color: #999;
  transition: transform 0.3s;
}

.category-panel {
  width: 360px;
  padding: 16px;
}

.panel-header {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f6f7;
}

.category-item:hover {
  background: #e8eaed;
  transform: translateY(-2px);
}

.category-item.active {
  background: #e6f0ff;
  border: 2px solid #3385ff;
}

.category-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-bottom: 8px;
  transition: transform 0.3s;
}

.category-item:hover .category-icon-wrapper {
  transform: scale(1.1);
}

.category-item-name {
  font-size: 13px;
  color: #666;
  text-align: center;
}
</style>
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
