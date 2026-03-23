<template>
  <div class="book-management">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Book" size="28" color="#3385ff" />
        账本管理
      </h1>
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="AddCircle" />
        </template>
        新建账本
      </n-button>
    </div>

    <!-- 账本列表 -->
    <div class="book-list">
      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="book in bookList" :key="book.id">
          <n-card
            class="book-card"
            :class="{ active: currentBookId === book.id }"
            hoverable
            @click="switchBook(book)"
          >
            <template #header>
              <div class="book-header">
                <div class="book-icon" :style="{ backgroundColor: book.color }">
                  <n-icon :component="book.icon || Book" size="28" color="#fff" />
                </div>
                <div class="book-actions">
                  <n-button quaternary circle @click.stop="editBook(book)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button
                    quaternary
                    circle
                    @click.stop="confirmDelete(book)"
                  >
                    <template #icon>
                      <n-icon :component="Trash" />
                    </template>
                  </n-button>
                </div>
              </div>
            </template>

            <div class="book-content">
              <h3 class="book-name">{{ book.name }}</h3>
              <p class="book-desc">{{ book.description || '暂无描述' }}</p>
              
              <div class="book-stats">
                <div class="stat-item">
                  <n-icon :component="People" size="16" />
                  <span>{{ book.memberCount || 1 }}人</span>
                </div>
                <div class="stat-item">
                  <n-icon :component="DocumentText" size="16" />
                  <span>{{ book.recordCount || 0 }}笔</span>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="book-footer">
                <span class="create-date">{{ formatDate(book.createdAt) }}</span>
                <n-tag v-if="book.id === currentBookId" type="success" size="small">
                  当前使用
                </n-tag>
              </div>
            </template>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 空状态 -->
      <n-empty
        v-if="bookList.length === 0"
        description="还没有账本，创建一个开始记账吧"
        size="large"
      />
    </div>

    <!-- 新建/编辑账本弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      :title="editingBook ? '编辑账本' : '新建账本'"
      preset="dialog"
      :show-icon="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="账本名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入账本名称"
            :maxlength="20"
            show-count
          />
        </n-form-item>

        <n-form-item label="账本描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入账本描述（可选）"
            :maxlength="100"
            show-count
            :rows="3"
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

        <n-form-item label="图标" path="icon">
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
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Book, AddCircle, Create, Trash, People, DocumentText,
  FastFood, Car, Cart, Home, Wallet, Business, Beer,
  Heart, School, Gamepad, Plane, Shirt, Pet
} from '@vicons/ionicons5'
import { getBookList, createBook, updateBook, deleteBook } from '@/api/book'
import { useBookStore } from '@/stores/book'

const message = useMessage()
const dialog = useDialog()
const bookStore = useBookStore()

const bookList = ref([])
const currentBookId = ref(null)
const showCreateModal = ref(false)
const editingBook = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const colors = [
  '#3385ff', '#52c41a', '#ff9900', '#ff6b6b',
  '#9b59b6', '#16a085', '#34495e', '#e74c3c'
]

const icons = [
  { value: 'Book', component: Book },
  { value: 'Wallet', component: Wallet },
  { value: 'FastFood', component: FastFood },
  { value: 'Car', component: Car },
  { value: 'Cart', component: Cart },
  { value: 'Home', component: Home },
  { value: 'Business', component: Business },
  { value: 'Beer', component: Beer },
  { value: 'Heart', component: Heart },
  { value: 'School', component: School },
  { value: 'Gamepad', component: Gamepad },
  { value: 'Plane', component: Plane },
  { value: 'Shirt', component: Shirt },
  { value: 'Pet', component: Pet }
]

const formData = reactive({
  name: '',
  description: '',
  color: '#3385ff',
  icon: 'Book'
})

const formRules = {
  name: {
    required: true,
    message: '请输入账本名称',
    trigger: 'blur'
  }
}

onMounted(async () => {
  await loadBookList()
  currentBookId.value = bookStore.currentBookId
})

const loadBookList = async () => {
  try {
    const data = await getBookList()
    bookList.value = (data.list || data || []).map(book => ({
      ...book,
      icon: getIconComponent(book.icon)
    }))
  } catch (error) {
    console.error('获取账本列表失败:', error)
  }
}

const getIconComponent = (iconName) => {
  const iconMap = {
    'Book': Book,
    'Wallet': Wallet,
    'FastFood': FastFood,
    'Car': Car,
    'Cart': Cart,
    'Home': Home,
    'Business': Business,
    'Beer': Beer,
    'Heart': Heart,
    'School': School,
    'Gamepad': Gamepad,
    'Plane': Plane,
    'Shirt': Shirt,
    'Pet': Pet
  }
  return iconMap[iconName] || Book
}

const switchBook = (book) => {
  bookStore.switchBook(book.id)
  currentBookId.value = book.id
  message.success(`已切换到 ${book.name}`)
}

const editBook = (book) => {
  editingBook.value = book
  formData.name = book.name
  formData.description = book.description || ''
  formData.color = book.color || '#3385ff'
  formData.icon = book.icon || 'Book'
  showCreateModal.value = true
}

const confirmDelete = (book) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除账本"${book.name}"吗？删除后该账本下的所有记录都将被删除，此操作不可恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteBook(book.id)
        message.success('账本已删除')
        await loadBookList()
      } catch (error) {
        console.error('删除账本失败:', error)
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (editingBook.value) {
      await updateBook(editingBook.value.id, formData)
      message.success('账本已更新')
    } else {
      await createBook(formData)
      message.success('账本已创建')
    }

    showCreateModal.value = false
    editingBook.value = null
    resetForm()
    await loadBookList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.name = ''
  formData.description = ''
  formData.color = '#3385ff'
  formData.icon = 'Book'
  formRef.value?.restoreValidation()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style scoped>
.book-management {
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

.book-list {
  margin-top: 20px;
}

.book-card {
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    border-color: #3385ff;
    box-shadow: 0 2px 12px rgba(51, 133, 255, 0.3);
  }
}

.book-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.book-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.book-card:hover .book-actions {
  opacity: 1;
}

.book-content {
  .book-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .book-desc {
    font-size: 14px;
    color: #999;
    margin: 0 0 16px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.book-stats {
  display: flex;
  gap: 16px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #666;
  }
}

.book-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .create-date {
    font-size: 12px;
    color: #999;
  }
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
  grid-template-columns: repeat(7, 1fr);
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
  padding: 16px 20px;
}

:deep(.n-card__footer) {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}
</style>
