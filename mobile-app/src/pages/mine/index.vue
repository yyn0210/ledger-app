<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar">
        <u-avatar size="80" :text="userInitials" bgColor="#3385ff"></u-avatar>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userName }}</text>
        <text class="user-desc">智能记账，让理财更简单</text>
      </view>
      <u-icon name="arrow-right" size="20" color="#fff"></u-icon>
    </view>

    <!-- 数据统计 -->
    <view class="stats-card">
      <view class="stats-item">
        <text class="stats-value">{{ totalDays }}</text>
        <text class="stats-label">记账天数</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item">
        <text class="stats-value">{{ totalRecords }}</text>
        <text class="stats-label">记录笔数</text>
      </view>
      <view class="stats-divider"></view>
      <view class="stats-item">
        <text class="stats-value">{{ totalAmount }}</text>
        <text class="stats-label">累计金额</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-group">
        <u-cell-group>
          <u-cell 
            title="账本管理" 
            :icon="book" 
            isLink
            @click="goToBookManage"
          ></u-cell>
          <u-cell 
            title="分类管理" 
            icon="grid" 
            isLink
            @click="goToCategoryManage"
          ></u-cell>
          <u-cell 
            title="账户管理" 
            icon="bank-card" 
            isLink
            @click="goToAccountManage"
          ></u-cell>
        </u-cell-group>
      </view>

      <view class="menu-group">
        <u-cell-group>
          <u-cell 
            title="预算管理" 
            icon="piechart" 
            isLink
            @click="goToBudget"
          ></u-cell>
          <u-cell 
            title="周期账单" 
            icon="clock" 
            isLink
            @click="goToRecurring"
          ></u-cell>
          <u-cell 
            title="储蓄目标" 
            icon="star" 
            isLink
            @click="goToSavings"
          ></u-cell>
        </u-cell-group>
      </view>

      <view class="menu-group">
        <u-cell-group>
          <u-cell 
            title="数据导出" 
            icon="download" 
            isLink
            @click="goToExport"
          ></u-cell>
          <u-cell 
            title="设置" 
            icon="setting" 
            isLink
            @click="goToSettings"
          ></u-cell>
        </u-cell-group>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const userName = ref('新用户')
const userInitials = computed(() => {
  return userName.value.charAt(0).toUpperCase()
})
const totalDays = ref('0')
const totalRecords = ref('0')
const totalAmount = ref('¥0.00')

const goToBookManage = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToCategoryManage = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToAccountManage = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToBudget = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToRecurring = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToSavings = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToExport = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

const goToSettings = () => {
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
}

.user-card {
  display: flex;
  align-items: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .user-avatar {
    margin-right: 16px;
  }
  
  .user-info {
    flex: 1;
    
    .user-name {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 6px;
    }
    
    .user-desc {
      display: block;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.stats-card {
  display: flex;
  align-items: center;
  margin: 16px;
  padding: 20px;
  background-color: #fff;
  border-radius: 12px;
  
  .stats-item {
    flex: 1;
    text-align: center;
    
    .stats-value {
      display: block;
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 6px;
    }
    
    .stats-label {
      display: block;
      font-size: 13px;
      color: #999;
    }
  }
  
  .stats-divider {
    width: 1px;
    height: 40px;
    background-color: #e0e0e0;
  }
}

.menu-section {
  padding: 0 16px;
}

.menu-group {
  margin-bottom: 16px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.version-info {
  text-align: center;
  padding: 30px 0;
  
  .version-text {
    font-size: 13px;
    color: #999;
  }
}
</style>
