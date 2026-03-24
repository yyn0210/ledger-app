<template>
  <div class="statistics">
<<<<<<< HEAD
    <n-card title="统计筛选">
      <n-space>
        <n-date-picker v-model:value="dateRange" type="monthrange" placeholder="选择月份范围" />
        <n-button type="primary">统计</n-button>
      </n-space>
    </n-card>
    <n-grid :cols="2" :x-gap="20" :y-gap="20">
      <n-grid-item><n-card title="收支对比"><div class="chart-placeholder">图表区域</div></n-card></n-grid-item>
      <n-grid-item><n-card title="支出分类"><div class="chart-placeholder">图表区域</div></n-card></n-grid-item>
    </n-grid>
=======
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="PieChart" size="28" color="#3385ff" />
        统计分析
      </h1>
      <div class="date-selector">
        <n-radio-group v-model:value="timeMode" size="small">
          <n-radio-button value="month">月</n-radio-button>
          <n-radio-button value="year">年</n-radio-button>
        </n-radio-group>
        <n-date-picker
          v-model:value="selectedDate"
          :type="timeMode === 'month' ? 'month' : 'year'"
          style="width: 150px; margin-left: 12px;"
        />
      </div>
    </div>

    <!-- 月度概览 -->
    <div class="overview-cards">
      <n-card class="overview-card expense">
        <div class="card-header">
          <n-icon :component="ArrowDown" size="20" color="#ff6b6b" />
          <span class="card-label">总支出</span>
        </div>
        <div class="card-value">¥{{ totalExpense }}</div>
        <div class="card-trend">
          <span class="trend-text">日均 ¥{{ dailyExpense }}</span>
        </div>
      </n-card>

      <n-card class="overview-card income">
        <div class="card-header">
          <n-icon :component="ArrowUp" size="20" color="#52c41a" />
          <span class="card-label">总收入</span>
        </div>
        <div class="card-value">¥{{ totalIncome }}</div>
        <div class="card-trend">
          <span class="trend-text">日均 ¥{{ dailyIncome }}</span>
        </div>
      </n-card>

      <n-card class="overview-card balance">
        <div class="card-header">
          <n-icon :component="Wallet" size="20" color="#3385ff" />
          <span class="card-label">结余</span>
        </div>
        <div class="card-value">¥{{ balance }}</div>
        <div class="card-trend">
          <span class="trend-text">结余率 {{ balanceRate }}%</span>
        </div>
      </n-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 支出趋势图 -->
        <n-card class="chart-card" title="支出趋势">
          <v-chart class="chart" :option="trendChartOption" autoresize />
        </n-card>

        <!-- 分类统计饼图 -->
        <n-card class="chart-card" title="支出分类">
          <v-chart class="chart" :option="categoryPieOption" autoresize />
        </n-card>
      </div>

      <div class="chart-row">
        <!-- 收入支出对比 -->
        <n-card class="chart-card" title="收支对比">
          <v-chart class="chart" :option="comparisonBarOption" autoresize />
        </n-card>

        <!-- 分类排行 -->
        <n-card class="chart-card" title="支出排行">
          <div class="rank-list">
            <div
              v-for="(item, index) in categoryRank"
              :key="item.category"
              class="rank-item"
            >
              <div class="rank-index">{{ index + 1 }}</div>
              <div class="rank-icon" :style="{ backgroundColor: item.color }">
                <n-icon :component="item.icon" size="20" color="#fff" />
              </div>
              <div class="rank-info">
                <div class="rank-name">{{ item.name }}</div>
                <div class="rank-bar">
                  <div
                    class="rank-bar-fill"
                    :style="{ width: item.percent + '%' }"
                  ></div>
                </div>
              </div>
              <div class="rank-amount">
                <div class="amount-value">¥{{ item.amount }}</div>
                <div class="amount-percent">{{ item.percent }}%</div>
              </div>
            </div>
          </div>
        </n-card>
      </div>
    </div>
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
  </div>
</template>

<script setup>
<<<<<<< HEAD
import { ref } from 'vue'
const dateRange = ref(null)
</script>

<style scoped>
.statistics { display: flex; flex-direction: column; gap: 20px; }
.chart-placeholder {
  height: 300px; display: flex; justify-content: center; align-items: center;
  color: #999; background-color: #f5f7f9; border-radius: 4px;
=======
import { ref, computed, onMounted, watch } from 'vue'
import {
  PieChart, ArrowDown, ArrowUp, Wallet,
  FastFood, Car, Cart, Beer, Home, Phone, Medical, School, ellipsisHorizontal
} from '@vicons/ionicons5'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart as PieChartComponent, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  PieChartComponent,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const timeMode = ref('month')
const selectedDate = ref(new Date().getTime())

// 模拟数据
const mockData = {
  expense: [120, 180, 95, 156, 220, 185, 145, 168, 192, 178, 156, 132],
  income: [8500, 0, 0, 0, 8500, 0, 0, 0, 8500, 0, 0, 0],
  categories: [
    { name: '餐饮', value: 2850, color: '#ff9900', icon: FastFood },
    { name: '交通', value: 680, color: '#3385ff', icon: Car },
    { name: '购物', value: 1560, color: '#ff6b6b', icon: Cart },
    { name: '娱乐', value: 420, color: '#9b59b6', icon: Beer },
    { name: '居住', value: 1200, color: '#2ecc71', icon: Home },
    { name: '通讯', value: 180, color: '#16a085', icon: Phone },
    { name: '医疗', value: 350, color: '#e74c3c', icon: Medical },
    { name: '教育', value: 680, color: '#3498db', icon: School },
    { name: '其他', value: 280, color: '#95a5a6', icon: ellipsisHorizontal }
  ]
}

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

const dailyIncome = computed(() => {
  return (parseFloat(totalIncome.value) / 30).toFixed(2)
})

const balanceRate = computed(() => {
  if (parseFloat(totalIncome.value) === 0) return 0
  return ((parseFloat(balance.value) / parseFloat(totalIncome.value)) * 100).toFixed(1)
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

const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['支出', '收入']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['1 日', '5 日', '10 日', '15 日', '20 日', '25 日', '30 日']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '支出',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: 'rgba(255, 107, 107, 0.2)'
      },
      itemStyle: {
        color: '#ff6b6b'
      },
      data: [120, 156, 185, 220, 192, 178, 132]
    },
    {
      name: '收入',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: 'rgba(82, 196, 26, 0.2)'
      },
      itemStyle: {
        color: '#52c41a'
      },
      data: [8500, 0, 0, 0, 0, 0, 0]
    }
  ]
}))

const categoryPieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: ¥{c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: 10,
    top: 'center'
  },
  series: [
    {
      name: '支出分类',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: mockData.categories.map(cat => ({
        value: cat.value,
        name: cat.name,
        itemStyle: { color: cat.color }
      }))
    }
  ]
}))

const comparisonBarOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['支出', '收入']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['1 月', '2 月', '3 月', '4 月', '5 月', '6 月']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '支出',
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        color: '#ff6b6b',
        borderRadius: [4, 4, 0, 0]
      },
      data: [3200, 2850, 3560, 2980, 3120, 2890]
    },
    {
      name: '收入',
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        color: '#52c41a',
        borderRadius: [4, 4, 0, 0]
      },
      data: [8500, 8500, 8500, 8500, 8500, 8500]
    }
  ]
}))

watch(timeMode, (newMode) => {
  // 切换时间模式时更新日期选择器类型
  selectedDate.value = new Date().getTime()
})

onMounted(() => {
  // 可以在这里加载真实数据
})
</script>

<style scoped>
.statistics {
  max-width: 1400px;
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

.date-selector {
  display: flex;
  align-items: center;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }

  .card-label {
    font-size: 14px;
    color: #666;
  }

  .card-value {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 8px;

    &.expense {
      color: #ff6b6b;
    }

    &.income {
      color: #52c41a;
    }

    &.balance {
      color: #3385ff;
    }
  }

  .card-trend {
    .trend-text {
      font-size: 13px;
      color: #999;
    }
  }
}

.charts-section {
  .chart-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 16px;
  }

  .chart-card {
    :deep(.n-card__content) {
      padding: 16px;
    }
  }

  .chart {
    height: 350px;
    width: 100%;
  }
}

.rank-list {
  padding: 8px 0;
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
    min-width: 90px;
    margin-left: 16px;

    .amount-value {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }

    .amount-percent {
      font-size: 12px;
      color: #999;
    }
  }
}

@media (max-width: 1024px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }

  .chart-row {
    grid-template-columns: 1fr !important;
  }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}
</style>
