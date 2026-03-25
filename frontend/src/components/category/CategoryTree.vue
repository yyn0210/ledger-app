<template>
  <div class="category-tree">
    <n-space vertical>
      <div v-for="category in rootCategories" :key="category.id">
        <category-item
          :category="category"
          :show-actions="showActions"
          @edit="$emit('edit', $event)"
          @delete="$emit('delete', $event)"
        />

        <!-- 子分类 -->
        <div v-if="category.children?.length > 0" class="child-categories">
          <category-item
            v-for="child in category.children"
            :key="child.id"
            :category="child"
            :show-actions="showActions"
            @edit="$emit('edit', $event)"
            @delete="$emit('delete', $event)"
          />
        </div>
      </div>

      <n-empty
        v-if="rootCategories.length === 0"
        description="暂无分类"
        style="padding: 40px 0"
      />
    </n-space>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CategoryItem from './CategoryItem.vue'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['edit', 'delete'])

// 构建树形结构
const rootCategories = computed(() => {
  const categoryMap = {}
  const roots = []

  // 创建映射
  props.categories.forEach(cat => {
    categoryMap[cat.id] = { ...cat, children: [] }
  })

  // 构建树
  props.categories.forEach(cat => {
    if (cat.parentId) {
      const parent = categoryMap[cat.parentId]
      if (parent) {
        parent.children.push(categoryMap[cat.id])
      } else {
        // 父分类不存在，作为根分类
        roots.push(categoryMap[cat.id])
      }
    } else {
      roots.push(categoryMap[cat.id])
    }
  })

  return roots
})
</script>

<style scoped>
.category-tree {
  width: 100%;
}

.child-categories {
  margin-left: 40px;
  margin-top: 8px;
  padding-left: 16px;
  border-left: 2px dashed #e5e7eb;
}
</style>
