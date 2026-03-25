<template>
  <div class="transaction-item" @click="$emit('click', transaction)">
    <n-space align="center" justify="space-between">
      <n-space align="center">
        <!-- 分类图标 -->
        <div
          v-if="transaction.type !== 'transfer'"
          class="category-icon"
          :style="{ backgroundColor: categoryColor }"
        >
          <n-icon :component="categoryIcon" size="20" color="#fff" />
        </div>
        <!-- 转账图标 -->
        <div v-else class="category-icon" :style="{ backgroundColor: '#4F46E5' }">
          <n-icon :component="SwapHorizontalOutline" size="20" color="#fff" />
        </div>

        <!-- 信息 -->
        <div>
          <n-space align="center" :size="8">
            <n-text strong>{{ transaction.note || categoryName }}</n-text>
            <n-tag
              v-if="transaction.type === 'transfer'"
              size="small"
              type="info"
            >
              转账
            </n-tag>
          </n-space>
          <n-text depth="3" style="font-size: 12px; display: block">
            {{ accountName }}
            <span v-if="showCategory && categoryName && transaction.type !== 'transfer'">
              · {{ categoryName }}
            </span>
          </n-text>
        </div>
      </n-space>

      <!-- 金额 -->
      <div class="amount" :class="[transaction.type]">
        <template v-if="hideBalance">
          <n-text depth="3">****</n-text>
        </template>
        <template v-else>
          <span v-if="transaction.type === 'income'">+</span>
          <span v-else-if="transaction.type === 'expense'">-</span>
          ¥{{ formatAmount(transaction.amount) }}
        </template>
      </div>
    </n-space>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  RestaurantOutline,
  WalletOutline,
  CarOutline,
  CartOutline,
  FilmOutline,
  TrendingUpOutline,
  SwapHorizontalOutline,
  BoxOutline
} from '@vicons/ionicons5'
import { useAccountStore } from '@/stores/account'
import { useCategoryStore } from '@/stores/category'
import { useBookStore } from '@/stores/book'

const props = defineProps({
  transaction: {
    type: Object,
    required: true
  },
  showCategory: {
    type: Boolean,
    default: true
  },
  hideBalance: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])

const accountStore = useAccountStore()
const categoryStore = useCategoryStore()
const bookStore = useBookStore()

// 分类图标映射
const iconMap = {
  restaurant: RestaurantOutline,
  coffee: () => import('@vicons/ionicons5').then(m => m.CoffeeOutline),
  car: CarOutline,
  cart: CartOutline,
  film: FilmOutline,
  wallet: WalletOutline,
  trending: TrendingUpOutline,
  default: BoxOutline
}

// 获取分类信息
const category = computed(() => {
  if (props.transaction.type === 'transfer') return null
  return categoryStore.categoryList.find(
    cat => cat.id === props.transaction.categoryId
  )
})

// 分类名称
const categoryName = computed(() => {
  return category.value?.name || ''
})

// 分类图标
const categoryIcon = computed(() => {
  const icon = category.value?.icon
  return iconMap[icon] || iconMap.default
})

// 分类颜色
const categoryColor = computed(() => {
  return category.value?.color || '#18a058'
})

// 账户名称
const accountName = computed(() => {
  const account = accountStore.accountList.find(
    acc => acc.id === props.transaction.accountId
  )
  return account?.name || ''
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}
</script>

<style scoped>
.transaction-item {
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.transaction-item:hover {
  background-color: #f9fafb;
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.amount {
  font-weight: 600;
  font-size: 16px;
  text-align: right;
}

.amount.income {
  color: #10B981;
}

.amount.expense {
  color: #EF4444;
}

.amount.transfer {
  color: #4F46E5;
}
</style>
