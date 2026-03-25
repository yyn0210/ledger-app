<template>
  <div class="time-range-picker">
    <n-space wrap>
      <n-radio-group v-model:value="selectedPreset" @update:value="handlePresetChange">
        <n-radio-button
          v-for="option in presetOptions"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </n-radio-button>
      </n-radio-group>

      <n-popover trigger="click" placement="bottom-end">
        <template #trigger>
          <n-button>
            <template #icon>
              <n-icon :component="CalendarOutline" />
            </template>
            自定义
          </n-button>
        </template>

        <n-date-picker
          v-model:value="customRange"
          type="daterange"
          clearable
          @update:value="handleCustomChange"
        />
      </n-popover>
    </n-space>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { CalendarOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      startDate: null,
      endDate: null,
      preset: 'month'
    })
  }
})

const emit = defineEmits(['update:modelValue'])

const selectedPreset = ref(props.modelValue.preset || 'month')
const customRange = ref([
  props.modelValue.startDate ? new Date(props.modelValue.startDate) : null,
  props.modelValue.endDate ? new Date(props.modelValue.endDate) : null
])

const presetOptions = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' },
  { label: '全部', value: 'all' }
]

// 获取预设日期范围
const getPresetRange = (preset) => {
  const now = dayjs()
  const startOfDay = now.startOf('day').toISOString()
  
  switch (preset) {
    case 'week':
      return {
        startDate: now.startOf('week').toISOString(),
        endDate: startOfDay
      }
    case 'month':
      return {
        startDate: now.startOf('month').toISOString(),
        endDate: startOfDay
      }
    case 'year':
      return {
        startDate: now.startOf('year').toISOString(),
        endDate: startOfDay
      }
    case 'all':
      return {
        startDate: null,
        endDate: null
      }
    default:
      return {
        startDate: null,
        endDate: null
      }
  }
}

// 预设变化
const handlePresetChange = (preset) => {
  const range = getPresetRange(preset)
  emit('update:modelValue', {
    startDate: range.startDate,
    endDate: range.endDate,
    preset
  })
}

// 自定义变化
const handleCustomChange = (dates) => {
  if (dates && dates[0] && dates[1]) {
    emit('update:modelValue', {
      startDate: dates[0].toISOString(),
      endDate: dates[1].toISOString(),
      preset: 'custom'
    })
  }
}

// 监听外部变化
watch(() => props.modelValue, (val) => {
  if (val.preset !== selectedPreset.value) {
    selectedPreset.value = val.preset || 'month'
  }
}, { deep: true })
</script>

<style scoped>
.time-range-picker {
  display: inline-block;
}
</style>
