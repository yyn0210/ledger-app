<template>
  <div class="category-management">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Grid" size="28" color="#3385ff" />
        分类管理
      </h1>
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="AddCircle" />
        </template>
        新建分类
      </n-button>
    </div>

    <!-- 分类类型切换 -->
    <n-tabs v-model:value="currentType" type="segment" animated>
      <n-tab-pane name="expense" tab="支出分类">
        <div class="category-list">
          <n-grid :cols="4" :x-gap="16" :y-gap="16">
            <n-grid-item v-for="category in expenseCategories" :key="category.id">
              <n-card class="category-card" :class="{ default: category.isDefault }">
                <div class="category-content">
                  <div class="category-icon" :style="{ backgroundColor: category.color }">
                    <n-icon :component="category.icon" size="28" color="#fff" />
                  </div>
                  <div class="category-info">
                    <div class="category-name">{{ category.name }}</div>
                    <n-tag v-if="category.isDefault" type="info" size="small">默认</n-tag>
                  </div>
                </div>
                <div class="category-actions" v-if="!category.isDefault">
                  <n-button quaternary circle size="small" @click="editCategory(category)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button quaternary circle size="small" @click="confirmDelete(category)">
                    <template #icon>
                      <n-icon :component="Trash" />
                    </template>
                  </n-button>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </div>
      </n-tab-pane>

      <n-tab-pane name="income" tab="收入分类">
        <div class="category-list">
          <n-grid :cols="4" :x-gap="16" :y-gap="16">
            <n-grid-item v-for="category in incomeCategories" :key="category.id">
              <n-card class="category-card" :class="{ default: category.isDefault }">
                <div class="category-content">
                  <div class="category-icon" :style="{ backgroundColor: category.color }">
                    <n-icon :component="category.icon" size="28" color="#fff" />
                  </div>
                  <div class="category-info">
                    <div class="category-name">{{ category.name }}</div>
                    <n-tag v-if="category.isDefault" type="info" size="small">默认</n-tag>
                  </div>
                </div>
                <div class="category-actions" v-if="!category.isDefault">
                  <n-button quaternary circle size="small" @click="editCategory(category)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button quaternary circle size="small" @click="confirmDelete(category)">
                    <template #icon>
                      <n-icon :component="Trash" />
                    </template>
                  </n-button>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 新建/编辑分类弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      :title="editingCategory ? '编辑分类' : '新建分类'"
      preset="dialog"
      :show-icon="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="分类名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入分类名称"
            :maxlength="10"
            show-count
          />
        </n-form-item>

        <n-form-item label="分类类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="expense">支出</n-radio>
            <n-radio value="income">收入</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="图标颜色" path="color">
          <div class="color-picker">
            <div
              v-for="color in colors"
              :key="color"
              class="color-option"
              :class="{ selected: formData.color === color }"
              :style="{ backgroundColor: color }"
              @click="formData.color = color"
            />
          </div>
        </n-form-item>

        <n-form-item label="分类图标" path="icon">
          <div class="icon-picker">
            <div
              v-for="icon in icons"
              :key="icon.value"
              class="icon-option"
              :class="{ selected: formData.icon === icon.value }"
              @click="formData.icon = icon.value"
            >
              <n-icon :component="icon.component" size="28" />
            </div>
          </div>
        </n-form-item>

        <n-form-item label="排序" path="sort">
          <n-input-number
            v-model:value="formData.sort"
            :min="1"
            :max="100"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <template #action>
        <n-button @click="showCreateModal = false">取消</n-button>
        <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Grid, AddCircle, Create, Trash,
  FastFood, Car, Cart, Beer, Home, Phone, Medical, School,
  Cash, Gift, TrendingUp, Briefcase, Gamepad, Plane, Shirt, Heart, ellipsisHorizontal
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

const currentType = ref('expense')
const showCreateModal = ref(false)
const editingCategory = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const colors = [
  '#ff9900', '#3385ff', '#ff6b6b', '#9b59b6',
  '#2ecc71', '#16a085', '#34495e', '#e74c3c'
]

const icons = [
  { value: 'FastFood', component: FastFood },
  { value: 'Car', component: Car },
  { value: 'Cart', component: Cart },
  { value: 'Beer', component: Beer },
  { value: 'Home', component: Home },
  { value: 'Phone', component: Phone },
  { value: 'Medical', component: Medical },
  { value: 'School', component: School },
  { value: 'Cash', component: Cash },
  { value: 'Gift', component: Gift },
  { value: 'TrendingUp', component: TrendingUp },
  { value: 'Briefcase', component: Briefcase },
  { value: 'Gamepad', component: Gamepad },
  { value: 'Plane', component: Plane },
  { value: 'Shirt', component: Shirt },
  { value: 'Heart', component: Heart },
  { value: 'ellipsisHorizontal', component: ellipsisHorizontal }
]

const iconMap = {
  FastFood, Car, Cart, Beer, Home, Phone, Medical, School,
  Cash, Gift, TrendingUp, Briefcase, Gamepad, Plane, Shirt, Heart, ellipsisHorizontal
}

const formData = reactive({
  name: '',
  type: 'expense',
  color: '#ff9900',
  icon: 'FastFood',
  sort: 1
})

const formRules = {
  name: {
    required: true,
    message: '请输入分类名称',
    trigger: 'blur'
  }
}

// 模拟数据
const expenseCategories = ref([
  { id: 1, name: '餐饮', type: 'expense', color: '#ff9900', icon: FastFood, sort: 1, isDefault: true },
  { id: 2, name: '交通', type: 'expense', color: '#3385ff', icon: Car, sort: 2, isDefault: true },
  { id: 3, name: '购物', type: 'expense', color: '#ff6b6b', icon: Cart, sort: 3, isDefault: true },
  { id: 4, name: '娱乐', type: 'expense', color: '#9b59b6', icon: Beer, sort: 4, isDefault: true },
  { id: 5, name: '居住', type: 'expense', color: '#2ecc71', icon: Home, sort: 5, isDefault: true },
  { id: 6, name: '通讯', type: 'expense', color: '#16a085', icon: Phone, sort: 6, isDefault: true },
  { id: 7, name: '医疗', type: 'expense', color: '#e74c3c', icon: Medical, sort: 7, isDefault: true },
  { id: 8, name: '教育', type: 'expense', color: '#3498db', icon: School, sort: 8, isDefault: true }
])

const incomeCategories = ref([
  { id: 11, name: '工资', type: 'income', color: '#2ecc71', icon: Cash, sort: 1, isDefault: true },
  { id: 12, name: '奖金', type: 'income', color: '#f39c12', icon: Gift, sort: 2, isDefault: true },
  { id: 13, name: '投资', type: 'income', color: '#3498db', icon: TrendingUp, sort: 3, isDefault: true },
  { id: 14, name: '兼职', type: 'income', color: '#9b59b6', icon: Briefcase, sort: 4, isDefault: true }
])

const editCategory = (category) => {
  editingCategory.value = category
  formData.name = category.name
  formData.type = category.type
  formData.color = category.color
  formData.icon = Object.keys(iconMap).find(key => iconMap[key] === category.icon) || 'FastFood'
  formData.sort = category.sort
  showCreateModal.value = true
}

const confirmDelete = (category) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除分类"${category.name}"吗？删除后该分类下的所有记录将不受影响，但无法再使用该分类。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const list = category.type === 'expense' ? expenseCategories : incomeCategories
        const index = list.value.findIndex(c => c.id === category.id)
        if (index > -1) {
          list.value.splice(index, 1)
        }
        message.success('分类已删除')
      } catch (error) {
        console.error('删除分类失败:', error)
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (editingCategory.value) {
      // 编辑
      const list = editingCategory.value.type === 'expense' ? expenseCategories : incomeCategories
      const index = list.value.findIndex(c => c.id === editingCategory.value.id)
      if (index > -1) {
        list.value[index] = {
          ...list.value[index],
          name: formData.name,
          color: formData.color,
          icon: iconMap[formData.icon],
          sort: formData.sort
        }
      }
      message.success('分类已更新')
    } else {
      // 新建
      const newCategory = {
        id: Date.now(),
        name: formData.name,
        type: formData.type,
        color: formData.color,
        icon: iconMap[formData.icon],
        sort: formData.sort,
        isDefault: false
      }
      const list = formData.type === 'expense' ? expenseCategories : incomeCategories
      list.value.push(newCategory)
      list.value.sort((a, b) => a.sort - b.sort)
      message.success('分类已创建')
    }

    showCreateModal.value = false
    editingCategory.value = null
    resetForm()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.name = ''
  formData.type = 'expense'
  formData.color = '#ff9900'
  formData.icon = 'FastFood'
  formData.sort = 1
  formRef.value?.restoreValidation()
}
</script>

<style scoped>
.category-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.category-list {
  padding: 16px 0;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
  
  &.default {
    background: #fafafa;
  }
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.category-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.category-info {
  flex: 1;
  min-width: 0;
  
  .category-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
  }
}

.category-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;

  .color-option {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    border: 2px solid transparent;

    &.selected {
      border-color: #333;
      transform: scale(1.1);
    }

    &:hover {
      transform: scale(1.1);
    }
  }
}

.icon-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;

  .icon-option {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: #f5f6f7;
    cursor: pointer;
    transition: all 0.3s;
    border: 2px solid transparent;

    &.selected {
      background: #e6f0ff;
      border-color: #3385ff;
    }

    &:hover {
      background: #e8eaed;
      transform: scale(1.1);
    }
  }
}

:deep(.n-card__content) {
  padding: 16px;
}
</style>
