<template>
  <view class="category-management">
    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">分类管理</text>
      <view class="navbar-add" @click="showCreateModal = true">
        <u-icon name="plus" size="24" color="#fff"></u-icon>
      </view>
    </view>

    <!-- 分类类型切换 -->
    <view class="type-tabs">
      <view
        class="tab-item"
        :class="{ active: currentType === 'expense' }"
        @click="currentType = 'expense'"
      >
        支出分类
      </view>
      <view
        class="tab-item"
        :class="{ active: currentType === 'income' }"
        @click="currentType = 'income'"
      >
        收入分类
      </view>
    </view>

    <!-- 分类列表 -->
    <scroll-view class="category-list" scroll-y>
      <view class="category-grid">
        <view
          v-for="category in currentCategories"
          :key="category.id"
          class="category-card"
          @longpress="editCategory(category)"
        >
          <view class="category-content">
            <view class="category-icon" :style="{ backgroundColor: category.color }">
              <u-icon :name="category.iconName" size="32" color="#fff"></u-icon>
            </view>
            <view class="category-info">
              <text class="category-name">{{ category.name }}</text>
              <u-tag v-if="category.isDefault" text="默认" type="info" size="mini"></u-tag>
            </view>
          </view>
          <view class="category-actions" v-if="!category.isDefault">
            <view class="action-btn" @click.stop="editCategory(category)">
              <u-icon name="edit-pen" size="20" color="#666"></u-icon>
            </view>
            <view class="action-btn" @click.stop="confirmDelete(category)">
              <u-icon name="close-circle" size="20" color="#ff6b6b"></u-icon>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="currentCategories.length === 0" class="empty-state">
        <u-icon name="apps" size="80" color="#e0e0e0"></u-icon>
        <text class="empty-text">暂无分类</text>
        <text class="empty-desc">点击右上角添加分类</text>
      </view>
    </scroll-view>

    <!-- 新建/编辑分类弹窗 -->
    <u-popup v-model="showCreateModal" mode="center" :round="16">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">{{ editingCategory ? '编辑分类' : '新建分类' }}</text>
          <u-icon name="close" size="24" @click="closeModal"></u-icon>
        </view>

        <scroll-view class="modal-content" scroll-y>
          <view class="form-item">
            <text class="form-label">分类名称</text>
            <input
              class="form-input"
              v-model="formData.name"
              placeholder="请输入分类名称"
              :maxlength="10"
            />
          </view>

          <view class="form-item">
            <text class="form-label">分类类型</text>
            <view class="type-radio">
              <view
                class="radio-item"
                :class="{ active: formData.type === 'expense' }"
                @click="formData.type = 'expense'"
              >
                支出
              </view>
              <view
                class="radio-item"
                :class="{ active: formData.type === 'income' }"
                @click="formData.type = 'income'"
              >
                收入
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">图标颜色</text>
            <view class="color-picker">
              <view
                v-for="color in colors"
                :key="color"
                class="color-option"
                :class="{ selected: formData.color === color }"
                :style="{ backgroundColor: color }"
                @click="formData.color = color"
              />
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">分类图标</text>
            <scroll-view class="icon-scroll" scroll-x>
              <view class="icon-picker">
                <view
                  v-for="icon in icons"
                  :key="icon.name"
                  class="icon-option"
                  :class="{ selected: formData.icon === icon.name }"
                  @click="formData.icon = icon.name"
                >
                  <u-icon :name="icon.name" size="28" color="#666"></u-icon>
                </view>
              </view>
            </scroll-view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeModal">取消</button>
          <button class="modal-btn confirm" @click="handleSubmit">
            {{ submitLoading ? '提交中...' : '确定' }}
          </button>
        </view>
      </view>
    </u-popup>

    <!-- 删除确认弹窗 -->
    <u-modal
      v-model="showDeleteModal"
      title="确认删除"
      :content="deleteContent"
      :show-cancel-button="true"
      @confirm="handleDelete"
    ></u-modal>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const currentType = ref('expense')
const showCreateModal = ref(false)
const showDeleteModal = ref(false)
const editingCategory = ref(null)
const deleteContent = ref('')
const submitLoading = ref(false)

const colors = [
  '#ff9900', '#3385ff', '#ff6b6b', '#9b59b6',
  '#2ecc71', '#16a085', '#34495e', '#e74c3c'
]

const icons = [
  { name: 'food' }, { name: 'car' }, { name: 'shopping-bag' },
  { name: 'beer' }, { name: 'home' }, { name: 'call' },
  { name: 'heart' }, { name: 'book' }, { name: 'yen' },
  { name: 'gift' }, { name: 'trending-up' }, { name: 'briefcase' },
  { name: 'gamepad' }, { name: 'plane' }, { name: 'shirt' }, { name: 'more' }
]

const formData = reactive({
  name: '',
  type: 'expense',
  color: '#ff9900',
  icon: 'food'
})

const expenseCategories = ref([
  { id: 1, name: '餐饮', type: 'expense', color: '#ff9900', iconName: 'food', sort: 1, isDefault: true },
  { id: 2, name: '交通', type: 'expense', color: '#3385ff', iconName: 'car', sort: 2, isDefault: true },
  { id: 3, name: '购物', type: 'expense', color: '#ff6b6b', iconName: 'shopping-bag', sort: 3, isDefault: true },
  { id: 4, name: '娱乐', type: 'expense', color: '#9b59b6', iconName: 'beer', sort: 4, isDefault: true },
  { id: 5, name: '居住', type: 'expense', color: '#2ecc71', iconName: 'home', sort: 5, isDefault: true },
  { id: 6, name: '通讯', type: 'expense', color: '#16a085', iconName: 'call', sort: 6, isDefault: true },
  { id: 7, name: '医疗', type: 'expense', color: '#e74c3c', iconName: 'heart', sort: 7, isDefault: true },
  { id: 8, name: '教育', type: 'expense', color: '#3498db', iconName: 'book', sort: 8, isDefault: true }
])

const incomeCategories = ref([
  { id: 11, name: '工资', type: 'income', color: '#2ecc71', iconName: 'yen', sort: 1, isDefault: true },
  { id: 12, name: '奖金', type: 'income', color: '#f39c12', iconName: 'gift', sort: 2, isDefault: true },
  { id: 13, name: '投资', type: 'income', color: '#3498db', iconName: 'trending-up', sort: 3, isDefault: true },
  { id: 14, name: '兼职', type: 'income', color: '#9b59b6', iconName: 'briefcase', sort: 4, isDefault: true }
])

const currentCategories = computed(() => {
  return currentType.value === 'expense' ? expenseCategories.value : incomeCategories.value
})

const editCategory = (category) => {
  editingCategory.value = category
  formData.name = category.name
  formData.type = category.type
  formData.color = category.color
  formData.icon = category.iconName
  showCreateModal.value = true
}

const confirmDelete = (category) => {
  deleteContent.value = `确定要删除分类"${category.name}"吗？删除后该分类下的所有记录将不受影响。`
  showDeleteModal.value = true
  editingCategory.value = category
}

const handleDelete = () => {
  if (!editingCategory.value) return
  
  const list = editingCategory.value.type === 'expense' ? expenseCategories : incomeCategories
  const index = list.value.findIndex(c => c.id === editingCategory.value.id)
  if (index > -1) {
    list.value.splice(index, 1)
  }
  
  uni.showToast({
    title: '分类已删除',
    icon: 'success'
  })
}

const handleSubmit = () => {
  if (!formData.name.trim()) {
    uni.showToast({
      title: '请输入分类名称',
      icon: 'none'
    })
    return
  }

  if (editingCategory.value) {
    // 编辑
    const list = editingCategory.value.type === 'expense' ? expenseCategories : incomeCategories
    const index = list.value.findIndex(c => c.id === editingCategory.value.id)
    if (index > -1) {
      list.value[index] = {
        ...list.value[index],
        name: formData.name,
        color: formData.color,
        iconName: formData.icon
      }
    }
    uni.showToast({
      title: '分类已更新',
      icon: 'success'
    })
  } else {
    // 新建
    const newCategory = {
      id: Date.now(),
      name: formData.name,
      type: formData.type,
      color: formData.color,
      iconName: formData.icon,
      sort: currentCategories.value.length + 1,
      isDefault: false
    }
    const list = formData.type === 'expense' ? expenseCategories : incomeCategories
    list.value.push(newCategory)
    uni.showToast({
      title: '分类已创建',
      icon: 'success'
    })
  }

  closeModal()
}

const closeModal = () => {
  showCreateModal.value = false
  editingCategory.value = null
  formData.name = ''
  formData.type = 'expense'
  formData.color = '#ff9900'
  formData.icon = 'food'
}
</script>

<style lang="scss" scoped>
.category-management {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 16px;
  padding-top: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  &-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  &-add {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
  }
}

.type-tabs {
  display: flex;
  background: #fff;
  padding: 12px 16px;
  gap: 12px;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px;
    background: #f5f6f7;
    border-radius: 8px;
    font-size: 14px;
    color: #666;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-weight: 600;
    }
  }
}

.category-list {
  height: calc(100vh - 180px);
  padding: 16px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.category-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.category-info {
  text-align: center;

  .category-name {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
  }
}

.category-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .action-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6f7;
    border-radius: 50%;
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;

  .empty-text {
    display: block;
    font-size: 16px;
    color: #999;
    margin-top: 16px;
  }

  .empty-desc {
    display: block;
    font-size: 14px;
    color: #ccc;
    margin-top: 8px;
  }
}

.modal-container {
  width: 340px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  max-height: 500px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .modal-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.modal-content {
  flex: 1;
  padding: 20px;
  max-height: 340px;
}

.form-item {
  margin-bottom: 16px;

  .form-label {
    display: block;
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }

  .form-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
  }

  .type-radio {
    display: flex;
    gap: 12px;

    .radio-item {
      flex: 1;
      text-align: center;
      padding: 10px;
      background: #f5f6f7;
      border-radius: 8px;
      font-size: 14px;
      color: #666;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 10px;

  .color-option {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    border: 2px solid transparent;

    &.selected {
      border-color: #333;
    }
  }
}

.icon-scroll {
  white-space: nowrap;
}

.icon-picker {
  display: inline-flex;
  gap: 10px;

  .icon-option {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: #f5f6f7;
    border: 2px solid transparent;

    &.selected {
      background: #e6f0ff;
      border-color: #3385ff;
    }
  }
}

.modal-footer {
  display: flex;
  border-top: 1px solid #f0f0f0;

  .modal-btn {
    flex: 1;
    height: 48px;
    border: none;
    font-size: 15px;

    &.cancel {
      background: #f5f6f7;
      color: #666;
      border-radius: 0 0 0 16px;
    }

    &.confirm {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border-radius: 0 0 16px 0;
    }
  }
}
</style>
