<template>
  <div class="ledger-selector">
    <n-select
      v-model:value="selectedId"
      :options="ledgerOptions"
      :placeholder="placeholder"
      :disabled="disabled"
      label-field="name"
      value-field="id"
      @update:value="$emit('update:modelValue', $event)"
    >
      <template #option="{ option }">
        <div class="ledger-option">
          <div class="ledger-icon" :style="{ backgroundColor: option.color || '#4F46E5' }">
            <n-icon :component="BookOutline" size="18" color="#fff" />
          </div>
          <div class="ledger-info">
            <n-text strong>{{ option.name }}</n-text>
            <n-text v-if="option.description" depth="3" style="font-size: 12px">
              {{ option.description }}
            </n-text>
          </div>
          <n-tag v-if="option.privacy === 'public'" size="small" type="success">
            公开
          </n-tag>
        </div>
      </template>

      <template #tag="{ option }">
        <div class="ledger-tag">
          <n-icon :component="BookOutline" size="16" :color="option.color || '#4F46E5'" />
          <n-text style="margin-left: 6px">{{ option.name }}</n-text>
        </div>
      </template>
    </n-select>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { BookOutline } from '@vicons/ionicons5'
import { useBookStore } from '@/stores/book'

const props = defineProps({
  modelValue: Number,
  placeholder: {
    type: String,
    default: '选择账本'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const bookStore = useBookStore()

const selectedId = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const ledgerOptions = computed(() => {
  return bookStore.bookList.map(book => ({
    id: book.id,
    name: book.name,
    description: book.description,
    privacy: book.privacy,
    color: book.color || '#4F46E5'
  }))
})
</script>

<style scoped>
.ledger-selector {
  width: 100%;
}

.ledger-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.ledger-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ledger-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ledger-tag {
  display: flex;
  align-items: center;
}
</style>
