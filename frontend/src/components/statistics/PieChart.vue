<template>
  <div class="pie-chart" ref="chartRef" style="width: 100%; height: 300px"></div>
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
    default: '分类占比'
  },
  showLegend: {
    type: Boolean,
    default: true
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
  const chartData = props.data.map(item => ({
    name: item.name || item.categoryName,
    value: item.value || item.amount,
    itemStyle: {
      color: item.color || undefined
    }
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: props.showLegend ? {
      orient: 'vertical',
      left: 'left',
      type: 'scroll'
    } : undefined,
    series: [
      {
        name: props.title,
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
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: chartData
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
.pie-chart {
  width: 100%;
}
</style>
