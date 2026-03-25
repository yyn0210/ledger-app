<template>
  <div class="trend-chart" ref="chartRef" style="width: 100%; height: 300px"></div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: '收支趋势'
  },
  type: {
    type: String,
    default: 'line',
    validator: value => ['line', 'bar', 'area'].includes(value)
  }
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return
  await nextTick()

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(getChartOption())

  // 响应式
  resizeObserver = new ResizeObserver(() => {
    chartInstance?.resize()
  })
  resizeObserver.observe(chartRef.value)
}

// 获取图表配置
const getChartOption = () => {
  const dates = props.data.map(item => item.date)
  const income = props.data.map(item => item.income || 0)
  const expense = props.data.map(item => item.expense || 0)

  const commonOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const date = params[0]?.name
        let html = `<div style="font-weight:600">${date}</div>`
        params.forEach(p => {
          const color = p.color
          const value = p.value
          html += `<div style="display:flex;justify-content:space-between;margin-top:4px">
            <span style="display:inline-block;width:10px;height:10px;background:${color};border-radius:50%;margin-right:8px"></span>
            <span>${p.seriesName}: ¥${Number(value).toFixed(2)}</span>
          </div>`
        })
        return html
      }
    },
    legend: {
      data: ['收入', '支出'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: props.type === 'bar',
      data: dates
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    }
  }

  // 系列配置
  const series = [
    {
      name: '收入',
      type: props.type === 'area' ? 'line' : props.type,
      data: income,
      itemStyle: { color: '#10B981' },
      areaStyle: props.type === 'area' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
          { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
        ])
      } : undefined,
      smooth: true
    },
    {
      name: '支出',
      type: props.type === 'area' ? 'line' : props.type,
      data: expense,
      itemStyle: { color: '#EF4444' },
      areaStyle: props.type === 'area' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
          { offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
        ])
      } : undefined,
      smooth: true
    }
  ]

  return { ...commonOption, series }
}

// 监听数据变化
watch(() => props.data, () => {
  if (chartInstance) {
    chartInstance.setOption(getChartOption())
  }
}, { deep: true })

onMounted(() => {
  initChart()
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<style scoped>
.trend-chart {
  width: 100%;
}
</style>
