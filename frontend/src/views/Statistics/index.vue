<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="TrendingUp" size="28" color="#10b981" />
        统计分析
      </h1>
      <n-space>
        <n-radio-group v-model:value="dateRange" @update:value="loadStatistics">
          <n-radio-button value="week">本周</n-radio-button>
          <n-radio-button value="month">本月</n-radio-button>
          <n-radio-button value="year">本年</n-radio-button>
          <n-radio-button value="custom">自定义</n-radio-button>
        </n-radio-group>
        <n-date-picker
          v-if="dateRange === 'custom'"
          v-model:value="customRange"
          type="daterange"
          placeholder="选择日期范围"
          @update:value="loadStatistics"
        />
        <n-button @click="exportData">
          <template #icon>
            <n-icon :component="Download" />
          </template>
          导出数据
        </n-button>
      </n-space>
    </div>

    <!-- 统计概览 -->
    <n-card class="overview-card">
      <n-grid :cols="5" :x-gap="16">
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-icon income">
              <n-icon :component="ArrowUpCircle" size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-label">总收入</div>
              <div class="stat-value income">¥{{ overview.totalIncome }}</div>
            </div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-icon expense">
              <n-icon :component="ArrowDownCircle" size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-label">总支出</div>
              <div class="stat-value expense">¥{{ overview.totalExpense }}</div>
            </div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-icon balance">
              <n-icon :component="Wallet" size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-label">总结余</div>
              <div class="stat-value balance">¥{{ overview.totalBalance }}</div>
            </div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-icon count">
              <n-icon :component="DocumentText" size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-label">交易笔数</div>
              <div class="stat-value">{{ overview.transactionCount }}</div>
            </div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="stat-item">
            <div class="stat-icon ratio">
              <n-icon :component="PieChart" size="24" />
            </div>
            <div class="stat-info">
              <div class="stat-label">收支比</div>
              <div class="stat-value">{{ incomeExpenseRatio }}%</div>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 收支趋势图 -->
    <n-grid :cols="2" :x-gap="16">
      <n-grid-item>
        <n-card class="chart-card" title="收支趋势">
          <div ref="trendChartRef" class="echart-chart" style="height: 300px;"></div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="chart-card" title="分类占比">
          <template #header-extra>
            <n-radio-group v-model:value="categoryType" size="small">
              <n-radio-button value="expense">支出</n-radio-button>
              <n-radio-button value="income">收入</n-radio-button>
            </n-radio-group>
          </template>
          <div ref="categoryChartRef" class="echart-chart" style="height: 300px;"></div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 月度对比图 -->
    <n-card class="chart-card" title="月度对比" style="margin-top: 16px;">
      <div ref="monthlyChartRef" class="echart-chart" style="height: 350px;"></div>
    </n-card>

    <!-- 分类排行 -->
    <n-card class="chart-card" title="支出排行 TOP5" style="margin-top: 16px;">
      <n-data-table
        :columns="rankingColumns"
        :data="topCategories"
        :pagination="false"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch, h } from 'vue'
import { useMessage, NProgress } from 'naive-ui'
import * as echarts from 'echarts'
import {
  TrendingUp, ArrowUpCircle, ArrowDownCircle, Wallet,
  DocumentText, PieChart, Download
} from '@vicons/ionicons5'
import { useStatisticsStore } from '@/stores/statistics'
import { getStatistics, getTrendData, getCategoryStats, getMonthlyComparison } from '@/api/statistics'

const message = useMessage()
const statisticsStore = useStatisticsStore()

const dateRange = ref('month')
const customRange = ref(null)
const categoryType = ref('expense')

const trendChartRef = ref(null)
const categoryChartRef = ref(null)
const monthlyChartRef = ref(null)

let trendChart = null
let categoryChart = null
let monthlyChart = null

const overview = computed(() => statisticsStore.overview)
const incomeExpenseRatio = computed(() => statisticsStore.incomeExpenseRatio)
const topCategories = computed(() => {
  const total = statisticsStore.categoryData.expense.reduce((sum, item) => sum + item.value, 0)
  return statisticsStore.topCategories.map(item => ({
    ...item,
    percent: total > 0 ? ((item.value / total) * 100).toFixed(1) : 0
  }))
})

const rankingColumns = [
  {
    title: '#',
    key: 'index',
    width: 60,
    render: (_row, index) => index + 1
  },
  {
    title: '分类',
    key: 'name',
    render: (row) => h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
      h('div', { style: `width: 12px; height: 12px; border-radius: 3px; background: ${row.color};` }),
      h('span', row.name)
    ])
  },
  {
    title: '金额',
    key: 'value',
    width: 120,
    align: 'right',
    render: (row) => h('span', { style: 'font-weight: 600;' }, `¥${row.value}`)
  },
  {
    title: '占比',
    key: 'percent',
    width: 200,
    align: 'right',
    render: (row) => {
      return h('div', { style: 'display: flex; align-items: center; justify-content: flex-end; gap: 8px;' }, [
        h(NProgress, {
          percentage: parseFloat(row.percent),
          status: parseFloat(row.percent) > 30 ? 'warning' : 'success',
          showIndicator: false,
          style: 'width: 100px;'
        }),
        h('span', `${row.percent}%`)
      ])
    }
  }
]

onMounted(() => {
  loadStatistics()
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

const loadStatistics = async () => {
  const now = new Date()
  let startDate, endDate

  switch (dateRange.value) {
    case 'week':
      startDate = new Date(now)
      startDate.setDate(now.getDate() - 7)
      endDate = now
      break
    case 'month':
      startDate = new Date(now.getFullYear(), now.getMonth(), 1)
      endDate = now
      break
    case 'year':
      startDate = new Date(now.getFullYear(), 0, 1)
      endDate = now
      break
    case 'custom':
      if (customRange.value) {
        startDate = new Date(customRange.value[0])
        endDate = new Date(customRange.value[1])
      } else {
        startDate = new Date(now.getFullYear(), now.getMonth(), 1)
        endDate = now
      }
      break
  }

  await statisticsStore.fetchAll({
    startDate: startDate?.toISOString().split('T')[0],
    endDate: endDate?.toISOString().split('T')[0]
  })

  updateCharts()
}

const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  if (monthlyChartRef.value) {
    monthlyChart = echarts.init(monthlyChartRef.value)
  }
}

const updateCharts = () => {
  updateTrendChart()
  updateCategoryChart()
  updateMonthlyChart()
}

const updateTrendChart = () => {
  if (!trendChart) return

  const { dates, income, expense } = statisticsStore.trendData

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['收入', '支出'],
      bottom: 0
    },
    grid: {
      left: '10%',
      right: '5%',
      top: '15%',
      bottom: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates || [],
      boundaryGap: false,
      axisLabel: {
        rotate: 45,
        interval: 'auto'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: value => '¥' + value
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: income || [],
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ])
        }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: expense || [],
        itemStyle: { color: '#ef4444' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
            { offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
          ])
        }
      }
    ]
  }

  trendChart.setOption(option)
}

const updateCategoryChart = () => {
  if (!categoryChart) return

  const data = categoryType.value === 'expense'
    ? statisticsStore.categoryData.expense
    : statisticsStore.categoryData.income

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    series: [
      {
        name: '分类',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
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
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: (data || []).map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  categoryChart.setOption(option)
}

const updateMonthlyChart = () => {
  if (!monthlyChart) return

  const monthlyData = statisticsStore.monthlyData

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['收入', '支出'],
      bottom: 0
    },
    grid: {
      left: '10%',
      right: '5%',
      top: '15%',
      bottom: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: monthlyData.map(item => item.month) || []
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: value => '¥' + value
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: monthlyData.map(item => item.income) || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#86efac' },
            { offset: 1, color: '#10b981' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '支出',
        type: 'bar',
        data: monthlyData.map(item => item.expense) || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#fca5a5' },
            { offset: 1, color: '#ef4444' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }

  monthlyChart.setOption(option)
}

const resizeCharts = () => {
  trendChart?.resize()
  categoryChart?.resize()
  monthlyChart?.resize()
}

const exportData = () => {
  message.info('数据导出功能开发中')
}

watch(() => categoryType.value, () => {
  updateCategoryChart()
})
</script>

<style scoped>
.statistics-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
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

.overview-card {
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;

  &.income {
    background: linear-gradient(135deg, #86efac 0%, #10b981 100%);
    color: #fff;
  }

  &.expense {
    background: linear-gradient(135deg, #fca5a5 0%, #ef4444 100%);
    color: #fff;
  }

  &.balance {
    background: linear-gradient(135deg, #93c5fd 0%, #3b82f6 100%);
    color: #fff;
  }

  &.count {
    background: linear-gradient(135deg, #c4b5fd 0%, #8b5cf6 100%);
    color: #fff;
  }

  &.ratio {
    background: linear-gradient(135deg, #fcd34d 0%, #f59e0b 100%);
    color: #fff;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;

  &.income {
    color: #10b981;
  }

  &.expense {
    color: #ef4444;
  }

  &.balance {
    color: #3b82f6;
  }
}

.chart-card {
  margin-bottom: 0;
}

.echart-chart {
  width: 100%;
}

:deep(.n-card__content) {
  padding: 16px 20px;
}

:deep(.n-radio-group) {
  display: flex;
  flex-wrap: wrap;
}
</style>
