<template>
  <div class="balance-display" @click="handleClick">
    <n-space align="center">
      <n-text :class="['balance-text', { 'blur': hideBalance && !show }]">
        <template v-if="hideBalance && !show">
          <n-text depth="3">****</n-text>
        </template>
        <template v-else>
          <n-text :type="balance >= 0 ? 'default' : 'error'">
            {{ prefix }}{{ formatAmount(balance) }}
          </n-text>
        </template>
      </n-text>

      <n-icon
        v-if="showToggle"
        :component="hideBalance && !show ? EyeOffOutline : EyeOutline"
        size="16"
        color="#9ca3af"
        @click.stop="toggleShow"
        style="cursor: pointer"
      />
    </n-space>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { EyeOutline, EyeOffOutline } from '@vicons/ionicons5'
import { useAccountStore } from '@/stores/account'

const props = defineProps({
  balance: {
    type: Number,
    default: 0
  },
  prefix: {
    type: String,
    default: '¥'
  },
  showToggle: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:show'])

const accountStore = useAccountStore()
const localShow = ref(true)

const hideBalance = computed(() => accountStore.hideBalance)
const show = computed(() => localShow.value)

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 切换显示状态
const toggleShow = () => {
  localShow.value = !localShow.value
  emit('update:show', localShow.value)
}

// 点击事件
const handleClick = () => {
  if (props.showToggle) {
    toggleShow()
  }
}
</script>

<style scoped>
.balance-display {
  display: inline-block;
}

.balance-text {
  font-weight: 600;
  font-size: 18px;
  transition: filter 0.2s;
}

.balance-text.blur {
  filter: blur(4px);
  user-select: none;
}
</style>
