<template>
  <view class="book-management">
    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">账本管理</text>
      <view class="navbar-add" @click="showCreateModal = true">
        <u-icon name="plus" size="24" color="#fff"></u-icon>
      </view>
    </view>

    <!-- 账本列表 -->
    <scroll-view class="book-list" scroll-y>
      <view class="book-item" v-for="book in bookList" :key="book.id">
        <view class="book-card" @click="selectBook(book)">
          <view class="book-icon" :style="{ backgroundColor: book.color }">
            <u-icon :name="book.iconName || 'book'" size="32" color="#fff"></u-icon>
          </view>
          <view class="book-info">
            <text class="book-name">{{ book.name }}</text>
            <text class="book-desc">{{ book.description || '暂无描述' }}</text>
            <view class="book-stats">
              <text class="stat-text">
                <u-icon name="account" size="14"></u-icon>
                {{ book.memberCount || 1 }}人
              </text>
              <text class="stat-text">
                <u-icon name="document" size="14"></u-icon>
                {{ book.recordCount || 0 }}笔
              </text>
            </view>
          </view>
          <view class="book-badge" v-if="book.id === currentBookId">
            <text class="badge-text">使用中</text>
          </view>
        </view>
        <view class="book-actions">
          <view class="action-btn" @click="editBook(book)">
            <u-icon name="edit-pen" size="22" color="#666"></u-icon>
          </view>
          <view class="action-btn" @click="confirmDelete(book)">
            <u-icon name="close-circle" size="22" color="#ff6b6b"></u-icon>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="bookList.length === 0" class="empty-state">
        <u-icon name="book" size="80" color="#e0e0e0"></u-icon>
        <text class="empty-text">还没有账本</text>
        <text class="empty-desc">创建一个账本开始记账吧</text>
      </view>
    </scroll-view>

    <!-- 新建/编辑账本弹窗 -->
    <u-popup v-model="showCreateModal" mode="center" :round="16">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">{{ editingBook ? '编辑账本' : '新建账本' }}</text>
          <u-icon name="close" size="24" @click="closeModal"></u-icon>
        </view>

        <scroll-view class="modal-content" scroll-y>
          <view class="form-item">
            <text class="form-label">账本名称</text>
            <input
              class="form-input"
              v-model="formData.name"
              placeholder="请输入账本名称"
              :maxlength="20"
            />
          </view>

          <view class="form-item">
            <text class="form-label">账本描述</text>
            <textarea
              class="form-textarea"
              v-model="formData.description"
              placeholder="请输入账本描述（可选）"
              :maxlength="100"
              :rows="3"
            />
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
            <text class="form-label">图标</text>
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
import { ref, reactive, onMounted } from 'vue'
import { getBookList, createBook, updateBook, deleteBook } from '@/api/book'
import { useBookStore } from '@/stores/book'

const bookStore = useBookStore()

const bookList = ref([])
const currentBookId = ref(null)
const showCreateModal = ref(false)
const showDeleteModal = ref(false)
const editingBook = ref(null)
const deleteContent = ref('')
const submitLoading = ref(false)

const colors = [
  '#3385ff', '#52c41a', '#ff9900', '#ff6b6b',
  '#9b59b6', '#16a085', '#34495e', '#e74c3c'
]

const icons = [
  { name: 'book' },
  { name: 'wallet' },
  { name: 'food' },
  { name: 'car' },
  { name: 'shopping-bag' },
  { name: 'home' },
  { name: 'shop' },
  { name: 'beer' },
  { name: 'heart' },
  { name: 'gift' }
]

const formData = reactive({
  name: '',
  description: '',
  color: '#3385ff',
  icon: 'book'
})

onMounted(async () => {
  await loadBookList()
  currentBookId.value = bookStore.currentBookId
})

const loadBookList = async () => {
  try {
    const res = await getBookList()
    bookList.value = res.data || []
    if (bookList.value.length > 0 && !currentBookId.value) {
      currentBookId.value = bookList.value[0].id
    }
  } catch (error) {
    console.error('获取账本列表失败:', error)
    // 默认账本
    bookList.value = [
      { id: 1, name: '默认账本', description: '日常收支', color: '#3385ff', icon: 'book' }
    ]
  }
}

const selectBook = (book) => {
  bookStore.switchBook(book.id)
  currentBookId.value = book.id
  uni.showToast({
    title: `已切换到 ${book.name}`,
    icon: 'success'
  })
}

const editBook = (book) => {
  editingBook.value = book
  formData.name = book.name
  formData.description = book.description || ''
  formData.color = book.color || '#3385ff'
  formData.icon = book.icon || 'book'
  showCreateModal.value = true
}

const confirmDelete = (book) => {
  deleteContent.value = `确定要删除账本"${book.name}"吗？删除后该账本下的所有记录都将被删除，此操作不可恢复。`
  showDeleteModal.value = true
  editingBook.value = book
}

const handleDelete = async () => {
  if (!editingBook.value) return
  
  try {
    await deleteBook(editingBook.value.id)
    uni.showToast({
      title: '账本已删除',
      icon: 'success'
    })
    await loadBookList()
  } catch (error) {
    console.error('删除账本失败:', error)
    uni.showToast({
      title: '删除失败',
      icon: 'none'
    })
  }
}

const handleSubmit = async () => {
  if (!formData.name.trim()) {
    uni.showToast({
      title: '请输入账本名称',
      icon: 'none'
    })
    return
  }

  try {
    submitLoading.value = true

    if (editingBook.value) {
      await updateBook(editingBook.value.id, formData)
      uni.showToast({
        title: '账本已更新',
        icon: 'success'
      })
    } else {
      await createBook(formData)
      uni.showToast({
        title: '账本已创建',
        icon: 'success'
      })
    }

    closeModal()
    await loadBookList()
  } catch (error) {
    console.error('提交失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  } finally {
    submitLoading.value = false
  }
}

const closeModal = () => {
  showCreateModal.value = false
  editingBook.value = null
  formData.name = ''
  formData.description = ''
  formData.color = '#3385ff'
  formData.icon = 'book'
}
</script>

<style lang="scss" scoped>
.book-management {
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

.book-list {
  height: calc(100vh - 140px);
  padding: 16px;
}

.book-item {
  margin-bottom: 12px;
}

.book-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.book-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  margin-right: 16px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.book-info {
  flex: 1;
  min-width: 0;

  .book-name {
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
  }

  .book-desc {
    display: block;
    font-size: 13px;
    color: #999;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .book-stats {
    display: flex;
    gap: 16px;

    .stat-text {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #999;
    }
  }
}

.book-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 12px;
  margin-left: 12px;

  .badge-text {
    font-size: 12px;
    color: #fff;
    font-weight: 600;
  }
}

.book-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-left: 16px;

  .action-btn {
    width: 36px;
    height: 36px;
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

  .form-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
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
