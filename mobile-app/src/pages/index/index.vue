<template>
  <view class="container">
    <!-- 自定义导航栏 -->
    <view class="navbar">
      <view class="navbar-content">
        <text class="navbar-title">智能记账</text>
        <view class="navbar-actions">
          <u-icon name="bell" size="24" color="#333"></u-icon>
        </view>
      </view>
    </view>

    <!-- 月度概览卡片 -->
    <view class="month-card">
      <view class="month-header">
        <text class="month-title">{{ currentMonth }}</text>
        <u-icon name="arrow-down" size="16" color="#fff"></u-icon>
      </view>
      <view class="month-stats">
        <view class="stat-item">
          <text class="stat-label">支出</text>
          <text class="stat-value expense">¥{{ monthExpense }}</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-label">收入</text>
          <text class="stat-value income">¥{{ monthIncome }}</text>
        </view>
      </view>
      <view class="month-balance">
        <text class="balance-label">结余</text>
        <text class="balance-value">¥{{ monthBalance }}</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="goToAccount">
        <u-icon name="edit-pen" size="32" color="#3385ff"></u-icon>
        <text class="action-text">记一笔</text>
      </view>
      <view class="action-item" @click="goToStats">
        <u-icon name="piechart" size="32" color="#3385ff"></u-icon>
        <text class="action-text">统计</text>
      </view>
      <view class="action-item" @click="goToBook">
        <u-icon name="book" size="32" color="#3385ff"></u-icon>
        <text class="action-text">账本</text>
      </view>
    </view>

    <!-- 最近记录 -->
    <view class="recent-section">
      <view class="section-header">
        <text class="section-title">最近记录</text>
        <text class="section-more">更多</text>
      </view>
      <view class="recent-list">
        <view class="recent-item" v-for="item in recentList" :key="item.id">
          <view class="item-icon">
            <u-icon :name="item.icon" size="36" :color="item.color"></u-icon>
          </view>
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <view class="item-amount" :class="item.type">
            {{ item.type === 'expense' ? '-' : '+' }}¥{{ item.amount }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentMonth = ref('2026 年 3 月')
const monthExpense = ref('3,280.50')
const monthIncome = ref('8,500.00')
const monthBalance = computed(() => {
  return (parseFloat(monthIncome.value.replace(',', '')) - parseFloat(monthExpense.value.replace(',', ''))).toFixed(2)
})

const recentList = ref([
  { id: 1, name: '早餐', time: '今天 08:30', amount: '15.00', type: 'expense', icon: 'coffee', color: '#ff9900' },
  { id: 2, name: '地铁', time: '今天 08:15', amount: '5.00', type: 'expense', icon: 'car', color: '#3385ff' },
  { id: 3, name: '工资', time: '昨天 18:00', amount: '8,500.00', type: 'income', icon: 'money', color: '#52c41a' },
  { id: 4, name: '晚餐', time: '昨天 19:30', amount: '68.00', type: 'expense', icon: 'food', color: '#ff9900' }
])

const goToAccount = () => {
  uni.switchTab({ url: '/pages/account/index' })
}

const goToStats = () => {
  uni.switchTab({ url: '/pages/stats/index' })
}

const goToBook = () => {
  uni.switchTab({ url: '/pages/book/index' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 20px;
}

.navbar {
  background-color: #fff;
  padding: 10px 16px;
  
  &-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  &-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
}

.month-card {
  margin: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  color: #fff;
  
  &-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  &-title {
    font-size: 16px;
  }
  
  &-stats {
    display: flex;
    justify-content: space-around;
    margin-bottom: 20px;
  }
  
  &-balance {
    text-align: center;
    padding-top: 16px;
    border-top: 1px solid rgba(255, 255, 255, 0.3);
  }
}

.stat-item {
  text-align: center;
  
  .stat-label {
    display: block;
    font-size: 13px;
    opacity: 0.8;
    margin-bottom: 8px;
  }
  
  .stat-value {
    display: block;
    font-size: 24px;
    font-weight: 600;
    
    &.expense {
      color: #ffcc80;
    }
    
    &.income {
      color: #a5d6a7;
    }
  }
}

.stat-divider {
  width: 1px;
  background-color: rgba(255, 255, 255, 0.3);
}

.balance-label {
  display: block;
  font-size: 13px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.balance-value {
  display: block;
  font-size: 28px;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  margin: 0 16px 16px;
  background-color: #fff;
  padding: 20px 0;
  border-radius: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .action-text {
    margin-top: 8px;
    font-size: 13px;
    color: #666;
  }
}

.recent-section {
  margin: 0 16px;
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  
  .section-more {
    font-size: 13px;
    color: #999;
  }
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .item-icon {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f6f7;
    border-radius: 50%;
    margin-right: 12px;
  }
  
  .item-info {
    flex: 1;
    
    .item-name {
      display: block;
      font-size: 15px;
      color: #333;
      margin-bottom: 4px;
    }
    
    .item-time {
      display: block;
      font-size: 12px;
      color: #999;
    }
  }
  
  .item-amount {
    font-size: 16px;
    font-weight: 600;
    
    &.expense {
      color: #ff6b6b;
    }
    
    &.income {
      color: #52c41a;
    }
  }
}
</style>
