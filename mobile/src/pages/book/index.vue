<template>
  <view class="container">
    <view class="book-list">
      <view class="book-item" v-for="book in bookList" :key="book.id">
        <view class="book-icon" :style="{ backgroundColor: book.color }">
          <u-icon name="book" size="32" color="#fff"></u-icon>
        </view>
        <view class="book-info">
          <text class="book-name">{{ book.name }}</text>
          <text class="book-desc">{{ book.description }}</text>
        </view>
        <u-icon name="arrow-right" size="20" color="#ccc"></u-icon>
      </view>
    </view>
    
    <view class="add-book-btn" @click="addBook">
      <u-icon name="plus" size="24" color="#3385ff"></u-icon>
      <text class="btn-text">新建账本</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const bookList = ref([
  { id: 1, name: '日常账本', description: '记录日常生活收支', color: '#3385ff' },
  { id: 2, name: '旅行账本', description: '记录旅行花费', color: '#52c41a' },
  { id: 3, name: '装修账本', description: '记录装修支出', color: '#ff9900' }
])

const addBook = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding: 16px;
}

.book-list {
  .book-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background-color: #fff;
    border-radius: 12px;
    margin-bottom: 12px;
    
    .book-icon {
      width: 50px;
      height: 50px;
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
        font-size: 16px;
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

.add-book-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background-color: #fff;
  border-radius: 12px;
  
  .btn-text {
    margin-left: 8px;
    font-size: 15px;
    color: #3385ff;
  }
}
</style>
