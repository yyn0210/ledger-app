<template>
  <view class="container">
    <!-- 时间选择 -->
    <view class="time-selector">
      <u-subsection 
        v-model="timeMode" 
        :list="['月', '年']" 
        :current="0"
        @change="onTimeModeChange"
      ></u-subsection>
      <view class="time-value" @click="showTimePicker = true">
        <text>{{ currentTime }}</text>
        <u-icon name="arrow-down" size="16" color="#3385ff"></u-icon>
      </view>
    </view>

    <!-- 统计图表 -->
    <view class="chart-section">
      <view class="chart-header">
        <text class="chart-title">支出统计</text>
        <text class="chart-total">总支出 ¥{{ totalExpense }}</text>
      </view>
      <view class="chart-container">
        <!-- TODO: 集成 ECharts 或 F2 图表 -->
        <view class="chart-placeholder">
          <u-icon name="piechart" size="80" color="#e0e0e0"></u-icon>
          <text class="placeholder-text">图表开发中</text>
        </view>
      </view>
    </view>

    <!-- 分类排行 -->
    <view class="rank-section">
      <view class="section-header">
        <text class="section-title">分类排行</text>
      </view>
      <view class="rank-list">
        <view class="rank-item" v-for="(item, index) in categoryRank" :key="item.id">
          <view class="rank-index">{{ index + 1 }}</view>
          <view class="rank-info">
            <view class="rank-icon" :style="{ backgroundColor: item.color }">
              <u-icon :name="item.icon" size="20" color="#fff"></u-icon>
            </view>
            <view class="rank-detail">
              <text class="rank-name">{{ item.name }}</text>
              <view class="rank-bar">
                <view class="rank-bar-fill" :style="{ width: item.percent + '%' }"></view>
              </view>
            </view>
          </view>
          <view class="rank-amount">
            <text class="amount-value">¥{{ item.amount }}</text>
            <text class="amount-percent">{{ item.percent }}%</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const timeMode = ref(0)
const currentTime = ref('2026 年 3 月')
const showTimePicker = ref(false)
const totalExpense = ref('3,280.50')

const categoryRank = ref([
  { id: 1, name: '餐饮', icon: 'food', color: '#ff9900', amount: '1,280.50', percent: 39 },
  { id: 2, name: '购物', icon: 'shopping-bag', color: '#ff6b6b', amount: '850.00', percent: 26 },
  { id: 3, name: '交通', icon: 'car', color: '#3385ff', amount: '420.00', percent: 13 },
  { id: 4, name: '娱乐', icon: 'movie', color: '#9b59b6', amount: '380.00', percent: 12 },
  { id: 5, name: '居住', icon: 'home', color: '#2ecc71', amount: '350.00', percent: 10 }
])

const onTimeModeChange = (index) => {
  timeMode.value = index
  currentTime.value = index === 0 ? '2026 年 3 月' : '2026 年'
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 20px;
}

.time-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #fff;
  margin-bottom: 12px;
  
  .time-value {
    display: flex;
    align-items: center;
    font-size: 15px;
    color: #3385ff;
    font-weight: 600;
  }
}

.chart-section {
  background-color: #fff;
  margin-bottom: 12px;
  padding: 16px;
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .chart-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
    
    .chart-total {
      font-size: 14px;
      color: #ff6b6b;
      font-weight: 600;
    }
  }
  
  .chart-container {
    height: 250px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .chart-placeholder {
      text-align: center;
      
      .placeholder-text {
        display: block;
        margin-top: 16px;
        font-size: 14px;
        color: #999;
      }
    }
  }
}

.rank-section {
  background-color: #fff;
  padding: 16px;
  
  .section-header {
    margin-bottom: 16px;
    
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .rank-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .rank-index {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f0f0f0;
      border-radius: 50%;
      font-size: 12px;
      color: #999;
      margin-right: 12px;
    }
    
    .rank-info {
      flex: 1;
      display: flex;
      align-items: center;
      
      .rank-icon {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        margin-right: 12px;
      }
      
      .rank-detail {
        flex: 1;
        
        .rank-name {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 6px;
        }
        
        .rank-bar {
          height: 6px;
          background-color: #f0f0f0;
          border-radius: 3px;
          overflow: hidden;
          
          .rank-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
            border-radius: 3px;
          }
        }
      }
    }
    
    .rank-amount {
      text-align: right;
      min-width: 80px;
      
      .amount-value {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
      }
      
      .amount-percent {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }
  }
}
</style>
