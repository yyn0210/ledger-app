<template>
  <div class="category-list">
    <!-- 头部操作区 -->
    <n-card>
      <n-space justify="space-between" align="center">
        <n-space>
          <h2 style="margin: 0">🏷️ 分类管理</h2>
          <n-segmented
            v-model:value="filterType"
            :options="[
              { label: '全部', value: 'all' },
              { label: '💰 收入', value: 'income' },
              { label: '💸 支出', value: 'expense' }
            ]"
            @update:value="handleFilterChange"
          />
        </n-space>
        <n-button type="primary" @click="handleCreate">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新建分类
        </n-button>
      </n-space>
    </n-card>

    <!-- 分类列表 -->
    <n-card style="margin-top: 20px">
      <template #header>
        <n-space justify="space-between" align="center">
          <span>分类列表</span>
          <n-tag v-if="filteredCategories.length > 0" type="info">
            共 {{ filteredCategories.length }} 个分类
          </n-tag>
        </n-space>
      </template>

      <n-spin :show="loading">
        <category-tree
          :categories="filteredCategories"
          :show-actions="true"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </n-spin>
    </n-card>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :title="editingCategory ? '编辑分类' : '新建分类'"
      preset="card"
      style="width: 500px"
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
            placeholder="请输入分类名称（如：餐饮、交通）"
            maxlength="20"
            show-count
          />
        </n-form-item>

        <n-form-item label="分类类型" path="type">
          <n-radio-group v-model:value="formData.type">
            <n-space>
              <n-radio value="expense">💸 支出</n-radio>
              <n-radio value="income">💰 收入</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="父分类" path="parentId">
          <n-select
            v-model:value="formData.parentId"
            :options="parentCategoryOptions"
            placeholder="选择父分类（可选，留空为一级分类）"
            clearable
          />
        </n-form-item>

        <n-form-item label="图标" path="icon">
          <icon-picker
            v-model="formData.icon"
            v-model:color="formData.color"
          />
        </n-form-item>

        <n-form-item label="颜色" path="color">
          <color-picker v-model="formData.color" />
        </n-form-item>

        <n-form-item label="排序" path="order">
          <n-input-number
            v-model:value="formData.order"
            :min="0"
            :max="999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ editingCategory ? '保存' : '创建' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { useCategoryStore } from '@/stores/category'
import CategoryTree from '@/components/category/CategoryTree.vue'
import IconPicker from '@/components/category/IconPicker.vue'
import ColorPicker from '@/components/category/ColorPicker.vue'

const message = useMessage()
const dialog = useDialog()
const categoryStore = useCategoryStore()

const formRef = ref(null)
const showModal = ref(false)
const submitting = ref(false)
const filterType = ref('all')
const editingCategory = ref(null)

const loading = computed(() => categoryStore.loading)
const filteredCategories = computed(() => categoryStore.filteredCategories)

const formData = reactive({
  name: '',
  type: 'expense',
  parentId: null,
  icon: '',
  color: '',
  order: 0
})

const formRules = {
  name: [
    {
      required: true,
      message: '请输入分类名称',
      trigger: 'blur'
    },
    {
      min: 1,
      max: 20,
      message: '名称长度 1-20 个字符',
      trigger: 'blur'
    }
  ],
  type: [
    {
      required: true,
      message: '请选择分类类型',
      trigger: 'change'
    }
  ]
}

// 父分类选项（只显示同类型的一级分类）
const parentCategoryOptions = computed(() => {
  const categories = categoryStore.categoryList.filter(
    cat => cat.type === formData.type && !cat.parentId
  )
  return categories.map(cat => ({
    label: cat.name,
    value: cat.id
  }))
})

// 筛选类型变化
const handleFilterChange = (type) => {
  categoryStore.setFilterType(type)
  categoryStore.fetchCategoryList({ type: type !== 'all' ? type : undefined })
}

// 新建分类
const handleCreate = () => {
  editingCategory.value = null
  resetForm()
  showModal.value = true
}

// 编辑分类
const handleEdit = (category) => {
  editingCategory.value = category
  formData.name = category.name
  formData.type = category.type
  formData.parentId = category.parentId
  formData.icon = category.icon
  formData.color = category.color
  formData.order = category.order || 0
  showModal.value = true
}

// 删除分类
const handleDelete = (category) => {
  const title = category.children?.length > 0
    ? `确定要删除分类「${category.name}」及其所有子分类吗？`
    : `确定要删除分类「${category.name}」吗？`

  dialog.warning({
    title: '确认删除',
    content: title,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await categoryStore.deleteCategory(category.id)
        message.success('分类已删除')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.type = 'expense'
  formData.parentId = null
  formData.icon = ''
  formData.color = ''
  formData.order = 0
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      name: formData.name.trim(),
      type: formData.type,
      parentId: formData.parentId || null,
      icon: formData.icon || 'wallet',
      color: formData.color || '#18a058',
      order: formData.order
    }

    if (editingCategory.value) {
      await categoryStore.updateCategory(editingCategory.value.id, data)
      message.success('分类已更新')
    } else {
      await categoryStore.createCategory(data)
      message.success('分类已创建')
    }

    showModal.value = false
    resetForm()
  } catch (error) {
    if (error?.errors) return
    message.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  categoryStore.fetchCategoryList()
})
</script>

<style scoped>
.category-list {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
