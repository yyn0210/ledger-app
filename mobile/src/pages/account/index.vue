<template>
  <view class="container">
    <!-- 金额输入区 -->
    <view class="amount-section">
      <view class="amount-display">
        <text class="amount-symbol">¥</text>
        <text class="amount-value">{{ amount || '0.00' }}</text>
      </view>
      <view class="type-toggle">
        <view 
          class="type-btn" 
          :class="{ active: type === 'expense' }"
          @click="type = 'expense'"
        >
          支出
        </view>
        <view 
          class="type-btn" 
          :class="{ active: type === 'income' }"
          @click="type = 'income'"
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
          <text class="value-text">{{ selectedCategory || '选择分类' }}</text>
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
          <text class="value-text">{{ selectedBook || '默认账本' }}</text>
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
          placeholder="输入备注信息"
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

    <!-- 分类选择器 -->
    <u-popup v-model="showCategoryPicker" mode="bottom" :round="20">
      <view class="picker-container">
        <view class="picker-header">
          <text class="picker-title">选择分类</text>
          <u-icon name="close" size="24" @click="showCategoryPicker = false"></u-icon>
        </view>
        <view class="category-grid">
          <view 
            class="category-item" 
            v-for="cat in expenseCategories" 
            :key="cat.id"
            :class="{ active: selectedCategory === cat.name }"
            @click="selectCategory(cat.name)"
          >
            <u-icon :name="cat.icon" size="32" :color="cat.color"></u-icon>
            <text class="category-name">{{ cat.name }}</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 日期选择器 -->
    <u-popup v-model="showDatePicker" mode="center" :round="20">
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
import { ref } from 'vue'

const amount = ref('')
const type = ref('expense')
const selectedCategory = ref('')
const selectedBook = ref('')
const selectedDate = ref(new Date().toISOString().split('T')[0])
const note = ref('')
const showCategoryPicker = ref(false)
const showBookPicker = ref(false)
const showDatePicker = ref(false)

const expenseCategories = ref([
  { id: 1, name: '餐饮', icon: 'food', color: '#ff9900' },
  { id: 2, name: '交通', icon: 'car', color: '#3385ff' },
  { id: 3, name: '购物', icon: 'shopping-bag', color: '#ff6b6b' },
  { id: 4, name: '娱乐', icon: 'movie', color: '#9b59b6' },
  { id: 5, name: '居住', icon: 'home', color: '#2ecc71' },
  { id: 6, name: '医疗', icon: 'heart', color: '#e74c3c' },
  { id: 7, name: '教育', icon: 'book', color: '#3498db' },
  { id: 8, name: '其他', icon: 'more', color: '#95a5a6' }
])

const selectCategory = (name) => {
  selectedCategory.value = name
  showCategoryPicker.value = false
}

const onDateChange = (e) => {
  selectedDate.value = e.detail.value
  showDatePicker.value = false
}

const handleSubmit = () => {
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
  
  // TODO: 调用 API 提交记账数据
  uni.showToast({
    title: '记账成功',
    icon: 'success'
  })
  
  // 重置表单
  amount.value = ''
  selectedCategory.value = ''
  note.value = ''
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
  
  .amount-symbol {
    font-size: 24px;
    color: #333;
    margin-right: 4px;
  }
  
  .amount-value {
    font-size: 48px;
    font-weight: 600;
    color: #333;
  }
}

.type-toggle {
  display: flex;
  justify-content: center;
  gap: 20px;
  
  .type-btn {
    padding: 10px 30px;
    border-radius: 20px;
    background-color: #f0f0f0;
    color: #666;
    font-size: 15px;
    
    &.active {
      background-color: #3385ff;
      color: #fff;
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
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .form-label {
    display: flex;
    align-items: center;
    
    .label-text {
      margin-left: 10px;
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
    height: 50px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    border: none;
    border-radius: 25px;
  }
}

.picker-container {
  padding: 20px;
  
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
  
  .category-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 8px;
    border-radius: 12px;
    background-color: #f5f6f7;
    
    &.active {
      background-color: #e6f0ff;
    }
    
    .category-name {
      margin-top: 8px;
      font-size: 13px;
      color: #666;
    }
  }
}

.date-picker-content {
  padding: 20px;
  text-align: center;
  font-size: 16px;
  color: #333;
}
</style>
