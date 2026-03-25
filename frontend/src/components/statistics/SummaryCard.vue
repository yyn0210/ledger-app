<template>
  <n-card class="summary-card">
    <template #header>
      <n-space justify="space-between" align="center">
        <n-text strong>{{ title }}</n-text>
        <n-icon :component="icon" :color="color" size="24" />
      </n-space>
    </template>

    <n-space vertical>
      <!-- 主数值 -->
      <div class="main-value" :style="{ color: color }">
        {{ prefix }}{{ formatValue(value) }}
      </div>

      <!-- 变化率 -->
      <n-space v-if="change !== undefined" align="center" justify="space-between">
        <n-text depth="3" style="font-size: 12px">较上期</n-text>
        <n-tag :type="changeType" size="small">
          {{ change > 0 ? '+' : '' }}{{ change }}%
          <template #icon>
            <n-icon :component="change > 0 ? TrendingUpOutline : TrendingDownOutline" />
          </template>
        </n-tag>
      </n-space>

      <!-- 额外信息 -->
      <n-text v-if="extra" depth="3" style="font-size: 12px">
        {{ extra }}
      </n-text>
    </n-space>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { TrendingUpOutline, TrendingDownOutline } from '@vicons/ionicons5'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: Number,
    default: 0
  },
  change: {
    type: Number,
    default: undefined
  },
  extra: {
    type: String,
    default: ''
  },
  prefix: {
    type: String,
    default: '¥'
  },
  color: {
    type: String,
    default: '#4F46E5'
  },
  icon: {
    type: Object,
    default: () => TrendingUpOutline
  }
})

// 格式化数值
const formatValue = (value) => {
  if (typeof value !== 'number') return value
  return Number(value).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 变化类型
const changeType = computed(() => {
  if (!props.change) return 'default'
  return props.change > 0 ? 'success' : 'error'
})
</script>

<style scoped>
.summary-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.main-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}
</style>
