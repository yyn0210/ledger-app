<template>
  <div class="category-management">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Apps" size="28" color="#3385ff" />
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
    <n-tabs v-model:value="activeType" type="segment" animated>
      <n-tab-pane name="expense" tab="支出分类">
        <div class="category-list">
          <n-grid :cols="4" :x-gap="12" :y-gap="12">
            <n-grid-item v-for="category in expenseCategories" :key="category.id">
              <n-card class="category-card" hoverable>
                <div class="category-content">
                  <div class="category-icon" :style="{ backgroundColor: category.color }">
                    <n-icon :component="getIconComponent(category.icon)" size="24" color="#fff" />
                  </div>
                  <div class="category-info">
                    <h4 class="category-name">{{ category.name }}</h4>
                    <p class="category-note">{{ category.note || '暂无备注' }}</p>
                  </div>
                </div>
                <div class="category-actions">
                  <n-button quaternary circle size="small" @click="editCategory(category)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button
                    quaternary
                    circle
                    size="small"
                    @click="confirmDelete(category)"
                  >
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
          <n-grid :cols="4" :x-gap="12" :y-gap="12">
            <n-grid-item v-for="category in incomeCategories" :key="category.id">
              <n-card class="category-card" hoverable>
                <div class="category-content">
                  <div class="category-icon" :style="{ backgroundColor: category.color }">
                    <n-icon :component="getIconComponent(category.icon)" size="24" color="#fff" />
                  </div>
                  <div class="category-info">
                    <h4 class="category-name">{{ category.name }}</h4>
                    <p class="category-note">{{ category.note || '暂无备注' }}</p>
                  </div>
                </div>
                <div class="category-actions">
                  <n-button quaternary circle size="small" @click="editCategory(category)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button
                    quaternary
                    circle
                    size="small"
                    @click="confirmDelete(category)"
                  >
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
        <n-form-item label="分类类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-radio value="expense">支出</n-radio>
            <n-radio value="income">收入</n-radio>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="分类名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入分类名称"
            :maxlength="10"
            show-count
          />
        </n-form-item>

        <n-form-item label="分类备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="请输入分类备注（可选）"
            :maxlength="50"
            show-count
            :rows="2"
          />
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
              <n-icon :component="icon.component" size="24" />
            </div>
          </div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Apps, AddCircle, Create, Trash,
  FastFood, Car, Cart, Home, Wallet, Business, Beer,
  Heart, School, GameController, Airplane, Shirt, Bag,
  Cash, Gift, TrendingUp
} from '@vicons/ionicons5'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { useCategoryStore } from '@/stores/category'

const message = useMessage()
const dialog = useDialog()
const categoryStore = useCategoryStore()

const activeType = ref('expense')
const showCreateModal = ref(false)
const editingCategory = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const expenseCategories = ref([])
const incomeCategories = ref([])

const colors = [
  '#ff9900', '#ff6b6b', '#3385ff', '#52c41a',
  '#9b59b6', '#16a085', '#34495e', '#e74c3c'
]

const expenseIcons = [
  { value: 'FastFood', component: FastFood },
  { value: 'Car', component: Car },
  { value: 'Cart', component: Cart },
  { value: 'Home', component: Home },
  { value: 'Beer', component: Beer },
  { value: 'Heart', component: Heart },
  { value: 'School', component: School },
  { value: 'GameController', component: GameController },
  { value: 'Airplane', component: Airplane },
  { value: 'Shirt', component: Shirt },
  { value: 'Bag', component: Bag }
]

const incomeIcons = [
  { value: 'Cash', component: Cash },
  { value: 'Gift', component: Gift },
  { value: 'TrendingUp', component: TrendingUp },
  { value: 'Wallet', component: Wallet },
  { value: 'Business', component: Business }
]

const icons = computed(() => activeType.value === 'income' ? incomeIcons : expenseIcons)

const formData = reactive({
  type: 'expense',
  name: '',
  note: '',
  color: '#ff9900',
  icon: 'FastFood'
})

const formRules = {
  name: {
    required: true,
    message: '请输入分类名称',
    trigger: 'blur'
  }
}

onMounted(async () => {
  await loadCategories()
})

const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    expenseCategories.value = (data.defaults || []).filter(c => c.type === 'expense')
    incomeCategories.value = (data.defaults || []).filter(c => c.type === 'income')
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const getIconComponent = (iconName) => {
  const allIcons = [...expenseIcons, ...incomeIcons]
  const icon = allIcons.find(i => i.value === iconName)
  return icon?.component || FastFood
}

const editCategory = (category) => {
  editingCategory.value = category
  formData.type = category.type
  formData.name = category.name
  formData.note = category.note || ''
  formData.color = category.color || '#ff9900'
  formData.icon = category.icon || 'FastFood'
  showCreateModal.value = true
}

const confirmDelete = (category) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除分类"${category.name}"吗？删除后该分类下的所有交易记录将受到影响，此操作不可恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteCategory(category.id)
        message.success('分类已删除')
        await loadCategories()
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
      await updateCategory(editingCategory.value.id, formData)
      message.success('分类已更新')
    } else {
      await createCategory(formData)
      message.success('分类已创建')
    }

    showCreateModal.value = false
    editingCategory.value = null
    resetForm()
    await loadCategories()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.type = 'expense'
  formData.name = ''
  formData.note = ''
  formData.color = '#ff9900'
  formData.icon = 'FastFood'
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
  position: relative;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-info {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.category-note {
  font-size: 12px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.category-card:hover .category-actions {
  opacity: 1;
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
  grid-template-columns: repeat(6, 1fr);
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
