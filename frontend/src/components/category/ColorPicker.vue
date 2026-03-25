<template>
  <div class="color-picker">
    <n-popover trigger="click" placement="bottom-start" :width="280">
      <template #trigger>
        <div
          class="color-preview"
          :style="{ backgroundColor: modelValue || '#18a058' }"
        >
          <n-text style="color: #fff; font-size: 12px">
            {{ modelValue || '选择颜色' }}
          </n-text>
        </div>
      </template>

      <div>
        <n-space vertical>
          <div>
            <n-text style="font-size: 13px; margin-bottom: 8px; display: block">
              预设颜色
            </n-text>
            <n-grid :cols="8" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="color in presetColors"
                :key="color"
                @click="handleSelect(color)"
              >
                <div
                  class="color-item"
                  :class="{ selected: modelValue === color }"
                  :style="{ backgroundColor: color }"
                />
              </n-grid-item>
            </n-grid>
          </div>

          <n-divider style="margin: 12px 0" />

          <div>
            <n-text style="font-size: 13px; margin-bottom: 8px; display: block">
              自定义颜色
            </n-text>
            <n-color-picker
              v-model:value="customColor"
              :modes="['hex']"
              show-alpha
              @complete="handleCustomSelect"
            />
          </div>
        </n-space>
      </div>
    </n-popover>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: String
})

const emit = defineEmits(['update:modelValue'])

const customColor = ref(props.modelValue || '#18a058')

// 预设颜色（符合设计规范）
const presetColors = [
  '#4F46E5', // Primary Indigo (品牌色)
  '#10B981', // Success Green (收入)
  '#EF4444', // Error Red (支出)
  '#F59E0B', // Warning Amber
  '#3B82F6', // Info Blue
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#06B6D4', // Cyan
  '#F97316', // Orange
  '#64748B', // Slate Gray
]

watch(() => props.modelValue, (val) => {
  if (val) customColor.value = val
})

const handleSelect = (color) => {
  emit('update:modelValue', color)
}

const handleCustomSelect = (color) => {
  if (color) {
    emit('update:modelValue', color)
  }
}
</script>

<style scoped>
.color-picker {
  display: inline-block;
}

.color-preview {
  width: 120px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid #e5e7eb;
  transition: border-color 0.2s;
}

.color-preview:hover {
  border-color: #18a058;
}

.color-item {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.color-item:hover {
  transform: scale(1.1);
}

.color-item.selected {
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.2);
}
</style>
