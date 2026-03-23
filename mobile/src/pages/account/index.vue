<template>
  <view class="container">
    <!-- 金额输入区 -->
    <view class="amount-section">
      <view class="amount-display" @click="showKeyboard = true">
        <text class="amount-symbol">¥</text>
        <text class="amount-value">{{ amount || '0.00' }}</text>
      </view>
      <view class="type-toggle">
        <view 
          class="type-btn" 
          :class="{ active: type === 'expense' }"
          @click="type = 'expense'; updateCategories()"
        >
          支出
        </view>
        <view 
          class="type-btn" 
          :class="{ active: type === 'income' }"
          @click="type = 'income'; updateCategories()"
        >
          收入
        </view>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <!-- 分类选择 -->
      <view class="form-item" @click="showCategoryPicker = true">
        <view class="form-label">
          <u-icon name="grid" size="20" color="#666"></u-icon>
          <text class="label-text">分类</text>
        </view>
        <view class="form-value">
          <text class="value-text" :class="{ 'has-value': selectedCategory }">
            {{ selectedCategory || '选择分类' }}
          </text>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 账本选择 -->
      <view class="form-item" @click="showBookPicker = true">
        <view class="form-label">
          <u-icon name="book" size="20" color="#666"></u-icon>
          <text class="label-text">账本</text>
        </view>
        <view class="form-value">
          <text class="value-text" :class="{ 'has-value': selectedBook }">
            {{ selectedBook || '默认账本' }}
          </text>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 日期选择 -->
      <view class="form-item" @click="showDatePicker = true">
        <view class="form-label">
          <u-icon name="clock" size="20" color="#666"></u-icon>
          <text class="label-text">日期</text>
        </view>
        <view class="form-value">
          <text class="value-text">{{ selectedDate }}</text>
          <u-icon name="arrow-right" size="16" color="#ccc"></u-icon>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item">
        <view class="form-label">
          <u-icon name="edit-pen" size="20" color="#666"></u-icon>
          <text class="label-text">备注</text>
        </view>
        <input 
          class="form-input" 
          v-model="note" 
          placeholder="输入备注信息（可选）"
          placeholder-class="input-placeholder"
        />
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-btn" @click="handleSubmit">
        确认记账
      </button>
    </view>

    <!-- 数字键盘 -->
    <u-popup v-model="showKeyboard" mode="bottom" :round="16" :closeable="false">
      <view class="keyboard-container">
        <view class="keyboard-display">
          <text class="keyboard-amount">¥ {{ amount || '0.00' }}</text>
        </view>
        <view class="keyboard-grid">
          <view 
            v-for="key in keyboardKeys" 
            :key="key.value"
            class="keyboard-key"
            :class="{ 'key-confirm': key.confirm, 'key-delete': key.delete }"
            @click="handleKeyPress(key)"
          >
            <text class="key-text">{{ key.text }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 分类选择器 -->
    <u-popup v-model="showCategoryPicker" mode="bottom" :round="20">
      <view class="picker-container">
        <view class="picker-header">
          <text class="picker-title">{{ type === 'expense' ? '支出分类' : '收入分类' }}</text>
          <u-icon name="close" size="24" @click="showCategoryPicker = false"></u-icon>
        </view>
        <scroll-view class="category-scroll" scroll-y>
          <view class="category-grid">
            <view 
              class="category-item" 
              v-for="cat in categories" 
              :key="cat.id"
              :class="{ active: selectedCategory === cat.name }"
              @click="selectCategory(cat)"
            >
              <view class="category-icon-wrapper" :style="{ backgroundColor: cat.color }">
                <u-icon :name="cat.icon" size="28" color="#fff"></u-icon>
              </view>
              <text class="category-name">{{ cat.name }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </u-popup>

    <!-- 账本选择器 -->
    <u-popup v-model="showBookPicker" mode="bottom" :round="20">
      <view class="picker-container">
        <view class="picker-header">
          <text class="picker-title">选择账本</text>
          <u-icon name="close" size="24" @click="showBookPicker = false"></u-icon>
        </view>
        <view class="book-list">
          <view 
            class="book-item" 
            v-for="book in bookList" 
            :key="book.id"
            :class="{ active: selectedBook === book.name }"
            @click="selectBook(book)"
          >
            <view class="book-icon" :style="{ backgroundColor: book.color }">
              <u-icon name="book" size="24" color="#fff"></u-icon>
            </view>
            <view class="book-info">
              <text class="book-name">{{ book.name }}</text>
              <text class="book-desc">{{ book.description || '默认账本' }}</text>
            </view>
            <u-icon v-if="selectedBook === book.name" name="checkmark" size="24" color="#3385ff"></u-icon>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 日期选择器 -->
    <u-popup v-model="showDatePicker" mode="center" :round="16">
      <picker 
        mode="date" 
        :value="selectedDate" 
        @change="onDateChange"
      >
        <view class="date-picker-content">
          <text>选择日期</text>
        </view>
      </picker>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { createTransaction } from '@/api/transaction'
import { getBookList } from '@/api/book'

const amount = ref('')
const type = ref('expense')
const selectedCategory = ref('')
const selectedCategoryData = ref(null)
const selectedBook = ref('')
const selectedBookData = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const note = ref('')
const showKeyboard = ref(false)
const showCategoryPicker = ref(false)
const showBookPicker = ref(false)
const showDatePicker = ref(false)

const bookList = ref([])

const expenseCategories = ref([
  { id: 1, name: '餐饮', icon: 'food', color: '#ff9900' },
  { id: 2, name: '交通', icon: 'car', color: '#3385ff' },
  { id: 3, name: '购物', icon: 'shopping-bag', color: '#ff6b6b' },
  { id: 4, name: '娱乐', icon: 'movie', color: '#9b59b6' },
  { id: 5, name: '居住', icon: 'home', color: '#2ecc71' },
  { id: 6, name: '医疗', icon: 'heart', color: '#e74c3c' },
  { id: 7, name: '教育', icon: 'book', color: '#3498db' },
  { id: 8, name: '通讯', icon: 'call', color: '#16a085' },
  { id: 9, name: '其他', icon: 'more', color: '#95a5a6' }
])

const incomeCategories = ref([
  { id: 1, name: '工资', icon: 'yen', color: '#2ecc71' },
  { id: 2, name: '奖金', icon: 'gift', color: '#f39c12' },
  { id: 3, name: '投资', icon: 'trending-up', color: '#3498db' },
  { id: 4, name: '兼职', icon: 'briefcase', color: '#9b59b6' },
  { id: 5, name: '其他', icon: 'more', color: '#95a5a6' }
])

const categories = ref(expenseCategories.value)

const keyboardKeys = ref([
  { text: '1', value: '1' },
  { text: '2', value: '2' },
  { text: '3', value: '3' },
  { text: '4', value: '4' },
  { text: '5', value: '5' },
  { text: '6', value: '6' },
  { text: '7', value: '7' },
  { text: '8', value: '8' },
  { text: '9', value: '9' },
  { text: '.', value: '.' },
  { text: '0', value: '0' },
  { text: '⌫', value: 'delete', delete: true },
  { text: '完成', value: 'confirm', confirm: true }
])

onMounted(async () => {
  await loadBookList()
})

const loadBookList = async () => {
  try {
    const res = await getBookList()
    bookList.value = res.data || [
      { id: 1, name: '默认账本', description: '日常收支', color: '#3385ff' }
    ]
    if (bookList.value.length > 0) {
      selectedBook.value = bookList.value[0].name
      selectedBookData.value = bookList.value[0]
    }
  } catch (error) {
    console.error('获取账本列表失败:', error)
    bookList.value = [
      { id: 1, name: '默认账本', description: '日常收支', color: '#3385ff' }
    ]
  }
}

const updateCategories = () => {
  categories.value = type.value === 'expense' ? expenseCategories.value : incomeCategories.value
  selectedCategory.value = ''
  selectedCategoryData.value = null
}

const selectCategory = (cat) => {
  selectedCategory.value = cat.name
  selectedCategoryData.value = cat
  showCategoryPicker.value = false
}

const selectBook = (book) => {
  selectedBook.value = book.name
  selectedBookData.value = book
  showBookPicker.value = false
}

const onDateChange = (e) => {
  selectedDate.value = e.detail.value
  showDatePicker.value = false
}

const handleKeyPress = (key) => {
  if (key.delete) {
    if (amount.value.length > 0) {
      amount.value = amount.value.slice(0, -1)
    }
    return
  }
  
  if (key.confirm) {
    showKeyboard.value = false
    return
  }
  
  // 防止输入多个小数点
  if (key.value === '.') {
    if (amount.value.includes('.')) return
    if (amount.value === '') {
      amount.value = '0.'
      return
    }
  }
  
  // 限制小数位数为 2 位
  const parts = amount.value.split('.')
  if (parts[1] && parts[1].length >= 2) {
    return
  }
  
  // 限制最大金额
  if (amount.value.replace('.', '').length >= 8) {
    uni.showToast({
      title: '金额过大',
      icon: 'none'
    })
    return
  }
  
  amount.value += key.value
}

const handleSubmit = async () => {
  if (!amount.value || parseFloat(amount.value) <= 0) {
    uni.showToast({
      title: '请输入金额',
      icon: 'none'
    })
    return
  }
  
  if (!selectedCategory.value) {
    uni.showToast({
      title: '请选择分类',
      icon: 'none'
    })
    return
  }
  
  try {
    const transactionData = {
      type: type.value,
      amount: parseFloat(amount.value),
      category: selectedCategoryData.value?.id || 0,
      categoryName: selectedCategory.value,
      bookId: selectedBookData.value?.id || 1,
      bookName: selectedBook.value,
      date: selectedDate.value,
      note: note.value
    }
    
    await createTransaction(transactionData)
    
    uni.showToast({
      title: '记账成功',
      icon: 'success'
    })
    
    // 重置表单
    setTimeout(() => {
      amount.value = ''
      selectedCategory.value = ''
      selectedCategoryData.value = null
      note.value = ''
      // 跳转到首页
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 1500)
  } catch (error) {
    console.error('创建交易失败:', error)
    uni.showToast({
      title: '记账失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 30px;
}

.amount-section {
  background-color: #fff;
  padding: 30px 20px;
  margin-bottom: 12px;
}

.amount-display {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .amount-symbol {
    font-size: 28px;
    color: #fff;
    margin-right: 8px;
    font-weight: 600;
  }
  
  .amount-value {
    font-size: 56px;
    font-weight: 700;
    color: #fff;
  }
}

.type-toggle {
  display: flex;
  justify-content: center;
  gap: 20px;
  
  .type-btn {
    padding: 10px 40px;
    border-radius: 24px;
    background-color: #f0f0f0;
    color: #666;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.3s;
    
    &.active {
      background-color: #3385ff;
      color: #fff;
      box-shadow: 0 4px 12px rgba(51, 133, 255, 0.4);
    }
  }
}

.form-section {
  background-color: #fff;
  margin-bottom: 12px;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .form-label {
    display: flex;
    align-items: center;
    
    .label-text {
      margin-left: 12px;
      font-size: 15px;
      color: #333;
    }
  }
  
  .form-value {
    display: flex;
    align-items: center;
    
    .value-text {
      font-size: 15px;
      color: #999;
      
      &.has-value {
        color: #333;
        font-weight: 500;
      }
    }
  }
  
  .form-input {
    flex: 1;
    text-align: right;
    font-size: 15px;
    color: #333;
  }
  
  .input-placeholder {
    color: #ccc;
  }
}

.submit-section {
  padding: 20px 16px;
  
  .submit-btn {
    width: 100%;
    height: 52px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    border: none;
    border-radius: 26px;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  }
}

.keyboard-container {
  background: #fff;
  padding-bottom: env(safe-area-inset-bottom);
}

.keyboard-display {
  padding: 20px;
  text-align: right;
  border-bottom: 1px solid #f0f0f0;
  
  .keyboard-amount {
    font-size: 32px;
    font-weight: 600;
    color: #333;
  }
}

.keyboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  background: #f0f0f0;
  
  .keyboard-key {
    background: #fff;
    padding: 24px 0;
    text-align: center;
    font-size: 22px;
    font-weight: 500;
    
    &.key-confirm {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
    
    &.key-delete {
      background: #f5f6f7;
      color: #ff6b6b;
    }
    
    &:active {
      background: #f0f0f0;
    }
  }
}

.picker-container {
  padding: 20px;
  max-height: 500px;
  
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .picker-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .category-scroll {
    max-height: 360px;
  }
  
  .category-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    padding-bottom: 20px;
  }
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 8px;
    border-radius: 16px;
    background-color: #f5f6f7;
    transition: all 0.3s;
    
    &.active {
      background-color: #e6f0ff;
      border: 2px solid #3385ff;
    }
    
    .category-icon-wrapper {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 14px;
      margin-bottom: 10px;
      transition: transform 0.3s;
    }
    
    &:active .category-icon-wrapper {
      transform: scale(0.9);
    }
    
    .category-name {
      font-size: 13px;
      color: #666;
    }
  }
  
  .book-list {
    .book-item {
      display: flex;
      align-items: center;
      padding: 16px;
      border-radius: 12px;
      margin-bottom: 12px;
      background: #f5f6f7;
      
      &.active {
        background: #e6f0ff;
        border: 2px solid #3385ff;
      }
      
      .book-icon {
        width: 44px;
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        margin-right: 16px;
      }
      
      .book-info {
        flex: 1;
        
        .book-name {
          display: block;
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }
        
        .book-desc {
          display: block;
          font-size: 13px;
          color: #999;
        }
      }
    }
  }
}

.date-picker-content {
  padding: 30px;
  text-align: center;
  font-size: 16px;
  color: #333;
}
</style>
