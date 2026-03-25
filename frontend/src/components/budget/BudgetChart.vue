<template>
  <div class="budget-chart">
    <n-card :title="title">
      <div ref="chartRef" style="width: 100%; height: 300px"></div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: {
    type: String,
    default: '预算执行趋势'
  },
  data: {
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'line', // 'line' | 'bar' | 'pie'
    validator: value => ['line', 'bar', 'pie'].includes(value)
  }
})

const chartRef = ref(null)
let chartInstance = null

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return

  // 等待 DOM 更新
  await nextTick()

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  // 设置配置
  const option = getChartOption()
  chartInstance.setOption(option)

  // 响应式
  window.addEventListener('resize', handleResize)
}

// 处理窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 获取图表配置
const getChartOption = () => {
  if (props.type === 'pie') {
    return getPieOption()
  }
  if (props.type === 'bar') {
    return getBarOption()
  }
  return getLineOption()
}

// 折线图配置
const getLineOption = () => ({
  tooltip: {
    trigger: 'axis',
    formatter: '{b}<br/>{a}: ¥{c}'
  },
  legend: {
    data: ['预算', '实际', '剩余'],
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
    boundaryGap: false,
    data: props.data.map(item => item.date)
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '¥{value}'
    }
  },
  series: [
    {
      name: '预算',
      type: 'line',
      data: props.data.map(item => item.budget),
      itemStyle: { color: '#4F46E5' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79, 70, 229, 0.3)' },
          { offset: 1, color: 'rgba(79, 70, 229, 0.05)' }
        ])
      }
    },
    {
      name: '实际',
      type: 'line',
      data: props.data.map(item => item.actual),
      itemStyle: { color: '#10B981' }
    },
    {
      name: '剩余',
      type: 'line',
      data: props.data.map(item => item.remaining),
      itemStyle: { color: '#F59E0B' }
    }
  ]
})

// 柱状图配置
const getBarOption = () => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: '{b}<br/>{a}: ¥{c}'
  },
  legend: {
    data: ['预算', '实际', '剩余'],
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
    data: props.data.map(item => item.date)
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '¥{value}'
    }
  },
  series: [
    {
      name: '预算',
      type: 'bar',
      data: props.data.map(item => item.budget),
      itemStyle: { color: '#4F46E5' }
    },
    {
      name: '实际',
      type: 'bar',
      data: props.data.map(item => item.actual),
      itemStyle: { color: '#10B981' }
    },
    {
      name: '剩余',
      type: 'bar',
      data: props.data.map(item => item.remaining),
      itemStyle: { color: '#F59E0B' }
    }
  ]
})

// 饼图配置
const getPieOption = () => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: ¥{c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: ['已用', '剩余']
  },
  series: [
    {
      name: '预算执行',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: [
        {
          value: props.data[0]?.actual || 0,
          name: '已用',
          itemStyle: { color: '#10B981' }
        },
        {
          value: props.data[0]?.remaining || 0,
          name: '剩余',
          itemStyle: { color: '#F59E0B' }
        }
      ]
    }
  ]
})

// 监听数据变化
watch(() => props.data, () => {
  if (chartInstance) {
    chartInstance.setOption(getChartOption())
  }
}, { deep: true })

onMounted(() => {
  initChart()
})
</script>

<style scoped>
.budget-chart {
  width: 100%;
}
</style>
