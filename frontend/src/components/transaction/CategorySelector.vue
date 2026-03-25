<template>
  <div class="category-selector">
    <n-popover trigger="click" placement="bottom-start" :width="320">
      <template #trigger>
        <div class="selector-trigger" @click="handleOpen">
          <template v-if="selectedCategory">
            <div
              class="category-icon"
              :style="{ backgroundColor: selectedCategory.color || '#18a058' }"
            >
              <n-icon :component="getIconComponent(selectedCategory.icon)" size="20" color="#fff" />
            </div>
            <n-text>{{ selectedCategory.name }}</n-text>
          </template>
          <template v-else>
            <n-text depth="3">选择分类</n-text>
            <n-icon :component="ChevronDownOutline" size="16" />
          </template>
        </div>
      </template>

      <div>
        <!-- 类型切换 -->
        <n-segmented
          v-model:value="filterType"
          :options="[
            { label: '支出', value: 'expense' },
            { label: '收入', value: 'income' }
          ]"
          style="margin-bottom: 12px"
          @update:value="handleTypeChange"
        />

        <!-- 分类列表 -->
        <n-tabs v-model:value="activeTab" type="line" animated>
          <n-tab-pane name="common" tab="常用">
            <n-grid :cols="4" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="cat in commonCategories"
                :key="cat.id"
                @click="handleSelect(cat)"
              >
                <div
                  class="category-item"
                  :style="{ backgroundColor: cat.color + '15' }"
                >
                  <div
                    class="category-icon-small"
                    :style="{ backgroundColor: cat.color }"
                  >
                    <n-icon :component="getIconComponent(cat.icon)" size="16" color="#fff" />
                  </div>
                  <n-text style="font-size: 12px; margin-top: 4px">
                    {{ cat.name }}
                  </n-text>
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="all" tab="全部">
            <n-space vertical>
              <div v-for="cat in filteredCategories" :key="cat.id">
                <category-item
                  :category="cat"
                  :show-actions="false"
                  @click="handleSelect(cat)"
                />
              </div>
            </n-space>
          </n-tab-pane>
        </n-tabs>
      </div>
    </n-popover>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ChevronDownOutline } from '@vicons/ionicons5'
import { useCategoryStore } from '@/stores/category'
import CategoryItem from '@/components/category/CategoryItem.vue'
import { typeConfigMap } from '@/components/account/AccountTypeIcon.vue'

const props = defineProps({
  modelValue: Number
})

const emit = defineEmits(['update:modelValue'])

const categoryStore = useCategoryStore()
const filterType = ref('expense')
const activeTab = ref('common')

const selectedCategory = computed(() => {
  if (!props.modelValue) return null
  return categoryStore.categoryList.find(cat => cat.id === props.modelValue)
})

const filteredCategories = computed(() => {
  return categoryStore.categoryList.filter(cat => cat.type === filterType.value)
})

const commonCategories = computed(() => {
  // 取前 8 个常用分类
  return filteredCategories.value.slice(0, 8)
})

// 获取图标组件
const getIconComponent = (iconName) => {
  const iconMap = {
    restaurant: () => import('@vicons/ionicons5').then(m => m.RestaurantOutline),
    coffee: () => import('@vicons/ionicons5').then(m => m.CoffeeOutline),
    car: () => import('@vicons/ionicons5').then(m => m.CarOutline),
    wallet: () => import('@vicons/ionicons5').then(m => m.WalletOutline),
    // 默认图标
    default: () => import('@vicons/ionicons5').then(m => m.BoxOutline)
  }
  return iconMap[iconName] || iconMap.default
}

// 打开选择器
const handleOpen = () => {
  if (categoryStore.categoryList.length === 0) {
    categoryStore.fetchCategoryList()
  }
}

// 类型变化
const handleTypeChange = () => {
  activeTab.value = 'common'
}

// 选择分类
const handleSelect = (category) => {
  emit('update:modelValue', category.id)
}

onMounted(() => {
  if (categoryStore.categoryList.length === 0) {
    categoryStore.fetchCategoryList()
  }
})
</script>

<style scoped>
.category-selector {
  width: 100%;
}

.selector-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.selector-trigger:hover {
  border-color: #4F46E5;
}

.category-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.category-icon-small {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.category-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
