<template>
  <div class="bar-chart" ref="chartRef" style="width: 100%; height: 300px"></div>
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
    default: '分类统计'
  },
  horizontal: {
    type: Boolean,
    default: false
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

  resizeObserver = new ResizeObserver(() => {
    chartInstance?.resize()
  })
  resizeObserver.observe(chartRef.value)
}

// 获取图表配置
const getChartOption = () => {
  const names = props.data.map(item => item.name || item.categoryName)
  const values = props.data.map(item => item.value || item.amount)
  const colors = props.data.map(item => item.color || '#4F46E5')

  if (props.horizontal) {
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params) => {
          const p = params[0]
          return `${p.name}<br/>¥${Number(p.value).toFixed(2)}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      yAxis: {
        type: 'category',
        data: names
      },
      series: [
        {
          name: props.title,
          type: 'bar',
          data: values,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#4F46E5' },
              { offset: 1, color: '#6366F1' }
            ])
          },
          barWidth: '60%',
          label: {
            show: true,
            position: 'right',
            formatter: '¥{c}'
          }
        }
      ]
    }
  }

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: props.title,
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4F46E5' },
            { offset: 1, color: '#6366F1' }
          ])
        },
        barWidth: '60%',
        label: {
          show: true,
          position: 'top',
          formatter: '¥{c}'
        }
      }
    ]
  }
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
.bar-chart {
  width: 100%;
}
</style>
