<template>
  <view class="statistics">
    <!-- 时间选择器 -->
    <view class="time-selector">
      <view class="selector-tabs">
        <view
          class="tab-item"
          :class="{ active: timeMode === 'month' }"
          @click="timeMode = 'month'"
        >
          月
        </view>
        <view
          class="tab-item"
          :class="{ active: timeMode === 'year' }"
          @click="timeMode = 'year'"
        >
          年
        </view>
      </view>
      <view class="selector-value" @click="showDatePicker = true">
        <text class="value-text">{{ currentTimeText }}</text>
        <u-icon name="arrow-down" size="16" color="#3385ff"></u-icon>
      </view>
    </view>

    <!-- 月度概览卡片 -->
    <view class="overview-card">
      <view class="card-header">
        <text class="card-month">{{ currentMonth }}</text>
      </view>
      <view class="card-stats">
        <view class="stat-item">
          <view class="stat-icon expense">
            <u-icon name="arrow-down" size="20" color="#fff"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-label">支出</text>
            <text class="stat-value expense">¥{{ totalExpense }}</text>
          </view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon income">
            <u-icon name="arrow-up" size="20" color="#fff"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-label">收入</text>
            <text class="stat-value income">¥{{ totalIncome }}</text>
          </view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon balance">
            <u-icon name="wallet" size="20" color="#fff"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-label">结余</text>
            <text class="stat-value balance">¥{{ balance }}</text>
          </view>
        </view>
      </view>
      <view class="card-footer">
        <text class="footer-text">日均支出 ¥{{ dailyExpense }} · 结余率 {{ balanceRate }}%</text>
      </view>
    </view>

    <!-- 支出趋势 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">支出趋势</text>
      </view>
      <view class="trend-chart">
        <view class="trend-bars">
          <view
            v-for="(item, index) in trendData"
            :key="index"
            class="trend-bar-wrapper"
          >
            <view
              class="trend-bar"
              :style="{ height: (item.value / maxTrendValue * 100) + '%' }"
              :class="{ 'is-max': item.value === maxTrendValue }"
            ></view>
            <text class="trend-label">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类排行 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">支出分类</text>
        <text class="section-total">总支出 ¥{{ totalExpense }}</text>
      </view>
      <view class="rank-list">
        <view
          v-for="(item, index) in categoryRank"
          :key="item.category"
          class="rank-item"
        >
          <view class="rank-index">{{ index + 1 }}</view>
          <view class="rank-icon" :style="{ backgroundColor: item.color }">
            <u-icon :name="item.iconName" size="20" color="#fff"></u-icon>
          </view>
          <view class="rank-info">
            <text class="rank-name">{{ item.name }}</text>
            <view class="rank-bar">
              <view
                class="rank-bar-fill"
                :style="{ width: item.percent + '%' }"
              ></view>
            </view>
          </view>
          <view class="rank-amount">
            <text class="amount-value">¥{{ item.amount }}</text>
            <text class="amount-percent">{{ item.percent }}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <u-popup v-model="showDatePicker" mode="center" :round="16">
      <picker
        v-if="timeMode === 'month'"
        mode="date"
        :value="selectedDate"
        fields="month"
        @change="onDateChange"
      >
        <view class="date-picker-content">
          <text>选择月份</text>
        </view>
      </picker>
      <picker
        v-else
        mode="date"
        :value="selectedDate"
        fields="year"
        @change="onDateChange"
      >
        <view class="date-picker-content">
          <text>选择年份</text>
        </view>
      </picker>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const timeMode = ref('month')
const selectedDate = ref(new Date().toISOString().split('T')[0])
const showDatePicker = ref(false)

// 模拟数据
const mockData = {
  expense: [120, 180, 95, 156, 220, 185, 145, 168, 192, 178, 156, 132],
  income: [8500, 0, 0, 0, 8500, 0, 0, 0, 8500, 0, 0, 0],
  categories: [
    { name: '餐饮', value: 2850, color: '#ff9900', iconName: 'food' },
    { name: '交通', value: 680, color: '#3385ff', iconName: 'car' },
    { name: '购物', value: 1560, color: '#ff6b6b', iconName: 'shopping-bag' },
    { name: '娱乐', value: 420, color: '#9b59b6', iconName: 'movie' },
    { name: '居住', value: 1200, color: '#2ecc71', iconName: 'home' },
    { name: '通讯', value: 180, color: '#16a085', iconName: 'call' },
    { name: '医疗', value: 350, color: '#e74c3c', iconName: 'heart' },
    { name: '教育', value: 680, color: '#3498db', iconName: 'book' },
    { name: '其他', value: 280, color: '#95a5a6', iconName: 'more' }
  ]
}

const currentMonth = computed(() => {
  const date = new Date(selectedDate.value)
  if (timeMode.value === 'month') {
    return `${date.getFullYear()}年${date.getMonth() + 1}月`
  } else {
    return `${date.getFullYear()}年`
  }
})

const currentTimeText = computed(() => {
  return currentMonth.value
})

const totalExpense = computed(() => {
  return mockData.expense.reduce((a, b) => a + b, 0).toFixed(2)
})

const totalIncome = computed(() => {
  return mockData.income.reduce((a, b) => a + b, 0).toFixed(2)
})

const balance = computed(() => {
  return (parseFloat(totalIncome.value) - parseFloat(totalExpense.value)).toFixed(2)
})

const dailyExpense = computed(() => {
  return (parseFloat(totalExpense.value) / 30).toFixed(2)
})

const balanceRate = computed(() => {
  if (parseFloat(totalIncome.value) === 0) return 0
  return ((parseFloat(balance.value) / parseFloat(totalIncome.value)) * 100).toFixed(1)
})

const trendData = computed(() => {
  const labels = ['1 日', '5 日', '10 日', '15 日', '20 日', '25 日', '30 日']
  const values = [120, 156, 185, 220, 192, 178, 132]
  return labels.map((label, index) => ({
    label,
    value: values[index]
  }))
})

const maxTrendValue = computed(() => {
  return Math.max(...trendData.value.map(item => item.value))
})

const categoryRank = computed(() => {
  const total = parseFloat(totalExpense.value)
  return mockData.categories
    .map(cat => ({
      ...cat,
      percent: ((cat.value / total) * 100).toFixed(1),
      amount: cat.value.toFixed(2)
    }))
    .sort((a, b) => b.value - a.value)
})

const onDateChange = (e) => {
  selectedDate.value = e.detail.value
  showDatePicker.value = false
}
</script>

<style lang="scss" scoped>
.statistics {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 20px;
}

.time-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  margin-bottom: 12px;

  .selector-tabs {
    display: flex;
    background: #f5f6f7;
    border-radius: 8px;
    padding: 4px;

    .tab-item {
      padding: 8px 20px;
      font-size: 14px;
      color: #666;
      border-radius: 6px;
      transition: all 0.3s;

      &.active {
        background: #fff;
        color: #3385ff;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .selector-value {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    color: #3385ff;
    font-weight: 600;
  }
}

.overview-card {
  margin: 0 16px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  color: #fff;

  .card-header {
    margin-bottom: 20px;

    .card-month {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .card-stats {
    display: flex;
    justify-content: space-around;
    margin-bottom: 16px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-icon {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        margin-bottom: 10px;

        &.expense {
          background: rgba(255, 107, 107, 0.3);
        }

        &.income {
          background: rgba(82, 196, 26, 0.3);
        }

        &.balance {
          background: rgba(255, 255, 255, 0.3);
        }
      }

      .stat-info {
        text-align: center;

        .stat-label {
          display: block;
          font-size: 13px;
          opacity: 0.8;
          margin-bottom: 6px;
        }

        .stat-value {
          display: block;
          font-size: 22px;
          font-weight: 700;

          &.expense {
            color: #ffcc80;
          }

          &.income {
            color: #a5d6a7;
          }

          &.balance {
            color: #fff;
          }
        }
      }
    }

    .stat-divider {
      width: 1px;
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .card-footer {
    text-align: center;
    padding-top: 16px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);

    .footer-text {
      font-size: 13px;
      opacity: 0.8;
    }
  }
}

.section-card {
  margin: 0 16px 12px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;

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

    .section-total {
      font-size: 14px;
      color: #ff6b6b;
      font-weight: 600;
    }
  }
}

.trend-chart {
  padding: 10px 0;

  .trend-bars {
    display: flex;
    justify-content: space-around;
    align-items: flex-end;
    height: 150px;
    padding: 0 10px;

    .trend-bar-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;
      margin: 0 4px;

      .trend-bar {
        width: 100%;
        max-width: 32px;
        background: linear-gradient(180deg, #ff6b6b 0%, #ff9999 100%);
        border-radius: 4px 4px 0 0;
        transition: height 0.5s ease;

        &.is-max {
          background: linear-gradient(180deg, #ff4757 0%, #ff6b6b 100%);
        }
      }

      .trend-label {
        margin-top: 8px;
        font-size: 11px;
        color: #999;
      }
    }
  }
}

.rank-list {
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
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 12px;
      color: #999;
      margin-right: 12px;
      flex-shrink: 0;
    }

    .rank-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      margin-right: 12px;
      flex-shrink: 0;
    }

    .rank-info {
      flex: 1;
      min-width: 0;

      .rank-name {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6px;
      }

      .rank-bar {
        height: 6px;
        background: #f0f0f0;
        border-radius: 3px;
        overflow: hidden;

        .rank-bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
          border-radius: 3px;
          transition: width 0.5s ease;
        }
      }
    }

    .rank-amount {
      text-align: right;
      min-width: 85px;
      margin-left: 12px;

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

.date-picker-content {
  padding: 30px;
  text-align: center;
  font-size: 16px;
  color: #333;
}
</style>
