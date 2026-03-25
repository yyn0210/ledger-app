<template>
  <div class="transaction-list">
    <!-- 头部筛选区 -->
    <n-card>
      <n-space vertical>
        <n-space justify="space-between" align="center">
          <h2 style="margin: 0">📝 交易记录</h2>
          <n-button type="primary" circle @click="handleQuickAdd">
            <template #icon>
              <n-icon :component="AddOutline" size="24" />
            </template>
          </n-button>
        </n-space>

        <!-- 筛选条件 -->
        <n-collapse :default-expanded-keys="['filters']">
          <n-collapse-item title="筛选条件" name="filters">
            <n-space vertical>
              <n-grid :cols="2" :x-gap="16" :y-gap="12">
                <n-grid-item>
                  <n-form-item label="账本">
                    <ledger-selector v-model="filters.ledgerId" clearable />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="分类">
                    <category-selector v-model="filters.categoryId" />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="账户">
                    <account-selector v-model="filters.accountId" clearable />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="类型">
                    <n-select
                      v-model:value="filters.type"
                      :options="[
                        { label: '全部', value: null },
                        { label: '支出', value: 'expense' },
                        { label: '收入', value: 'income' },
                        { label: '转账', value: 'transfer' }
                      ]"
                      clearable
                    />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="开始日期">
                    <n-date-picker
                      v-model:value="filters.startDate"
                      type="date"
                      placeholder="选择开始日期"
                      style="width: 100%"
                    />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="结束日期">
                    <n-date-picker
                      v-model:value="filters.endDate"
                      type="date"
                      placeholder="选择结束日期"
                      style="width: 100%"
                    />
                  </n-form-item>
                </n-grid-item>
              </n-grid>

              <n-space justify="end">
                <n-button @click="handleResetFilters">重置</n-button>
                <n-button type="primary" @click="handleApplyFilters">应用</n-button>
              </n-space>
            </n-space>
          </n-collapse-item>
        </n-collapse>
      </n-space>
    </n-card>

    <!-- 月度统计 -->
    <n-card style="margin-top: 20px">
      <n-grid :cols="3" :x-gap="20">
        <n-grid-item>
          <n-statistic label="本月收入">
            <n-text type="success">¥{{ formatAmount(monthlyStats.income) }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="本月支出">
            <n-text type="error">¥{{ formatAmount(monthlyStats.expense) }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="本月结余">
            <n-text :type="monthlyStats.balance >= 0 ? 'success' : 'error'">
              ¥{{ formatAmount(monthlyStats.balance) }}
            </n-text>
          </n-statistic>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 交易列表 -->
    <n-card style="margin-top: 20px">
      <n-spin :show="loading">
        <n-empty
          v-if="!loading && groupedTransactions.length === 0"
          description="暂无交易记录"
          style="padding: 60px 0"
        >
          <template #extra>
            <n-button type="primary" @click="handleQuickAdd">记一笔</n-button>
          </template>
        </n-empty>

        <template v-else>
          <transaction-group
            v-for="group in groupedTransactions"
            :key="group.date"
            :date="group.date"
            :transactions="group.items"
            :show-category="true"
            :hide-balance="hideBalance"
            @click="handleViewDetail"
          />

          <!-- 加载更多 -->
          <n-infinite-scroll
            v-if="hasMore"
            :distance="10"
            @load="handleLoadMore"
          >
            <div style="padding: 20px; text-align: center">
              <n-spin size="small" :show="loadingMore" />
              <n-text v-if="!loadingMore" depth="3">上拉加载更多</n-text>
            </div>
          </n-infinite-scroll>

          <n-divider v-else>没有更多了</n-divider>
        </template>
      </n-spin>
    </n-card>

    <!-- 快速记账悬浮按钮 -->
    <n-float-button
      position="bottom-right"
      :right="24"
      :bottom="80"
      type="primary"
      :size="56"
      @click="handleQuickAdd"
    >
      <template #icon>
        <n-icon :component="AddOutline" size="28" />
      </template>
    </n-float-button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { useTransactionStore } from '@/stores/transaction'
import { useAccountStore } from '@/stores/account'
import { useCategoryStore } from '@/stores/category'
import { useBookStore } from '@/stores/book'
import TransactionGroup from '@/components/transaction/TransactionGroup.vue'
import LedgerSelector from '@/components/transaction/LedgerSelector.vue'
import CategorySelector from '@/components/transaction/CategorySelector.vue'
import AccountSelector from '@/components/transaction/AccountSelector.vue'

const router = useRouter()
const message = useMessage()
const txStore = useTransactionStore()
const accountStore = useAccountStore()
const categoryStore = useCategoryStore()
const bookStore = useBookStore()

const loading = computed(() => txStore.loading)
const loadingMore = ref(false)
const hasMore = computed(() => txStore.hasMore)
const hideBalance = computed(() => accountStore.hideBalance)

const filters = reactive({
  ledgerId: null,
  categoryId: null,
  accountId: null,
  startDate: null,
  endDate: null,
  type: null,
  keyword: '',
  minAmount: null,
  maxAmount: null
})

const groupedTransactions = computed(() => {
  const groups = txStore.groupedTransactions
  return Object.entries(groups).map(([date, items]) => ({
    date,
    items
  }))
})

const monthlyStats = computed(() => txStore.monthlyStats)

// 快速记账
const handleQuickAdd = () => {
  router.push('/transaction/create')
}

// 查看详情
const handleViewDetail = (transaction) => {
  router.push(`/transaction/${transaction.id}`)
}

// 应用筛选
const handleApplyFilters = async () => {
  txStore.setFilters(filters)
  txStore.resetList()
  try {
    await txStore.fetchTransactionList()
  } catch (error) {
    message.error('加载失败：' + (error.message || '未知错误'))
  }
}

// 重置筛选
const handleResetFilters = () => {
  filters.ledgerId = null
  filters.categoryId = null
  filters.accountId = null
  filters.startDate = null
  filters.endDate = null
  filters.type = null
  filters.keyword = ''
  filters.minAmount = null
  filters.maxAmount = null
  
  txStore.resetFilters()
  txStore.resetList()
  txStore.fetchTransactionList()
}

// 加载更多
const handleLoadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  try {
    await txStore.fetchTransactionList({ page: txStore.page + 1 }, true)
  } catch (error) {
    message.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loadingMore.value = false
  }
}

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

onMounted(() => {
  // 并行加载基础数据
  Promise.all([
    txStore.fetchTransactionList(),
    accountStore.fetchAccountList(),
    categoryStore.fetchCategoryList(),
    bookStore.fetchBookList()
  ]).catch(error => {
    message.error('加载失败：' + (error.message || '未知错误'))
  })
})
</script>

<style scoped>
.transaction-list {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
