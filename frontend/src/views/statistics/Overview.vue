<template>
  <div class="statistics-overview">
    <!-- 时间范围选择 -->
    <n-card>
      <n-space justify="space-between" align="center">
        <h2 style="margin: 0">📊 统计概览</h2>
        <time-range-picker v-model="dateRange" />
      </n-space>
    </n-card>

    <!-- 汇总卡片 -->
    <n-card style="margin-top: 20px">
      <n-grid :cols="3" :x-gap="20">
        <n-grid-item>
          <summary-card
            title="总收入"
            :value="overview?.income || 0"
            :change="overview?.incomeChange"
            color="#10B981"
            :icon="ArrowUpOutline"
          />
        </n-grid-item>
        <n-grid-item>
          <summary-card
            title="总支出"
            :value="overview?.expense || 0"
            :change="overview?.expenseChange"
            color="#EF4444"
            :icon="ArrowDownOutline"
          />
        </n-grid-item>
        <n-grid-item>
          <summary-card
            title="结余"
            :value="balance"
            :extra="balance >= 0 ? '收支平衡' : '超支'"
            :color="balanceColor"
            :icon="WalletOutline"
          />
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 收支趋势 -->
    <n-card style="margin-top: 20px">
      <template #header>
        <n-space justify="space-between" align="center">
          <n-text strong>收支趋势</n-text>
          <n-segmented
            v-model:value="trendType"
            :options="[
              { label: '折线图', value: 'line' },
              { label: '柱状图', value: 'bar' },
              { label: '面积图', value: 'area' }
            ]"
            size="small"
          />
        </n-space>
      </template>
      <trend-chart :data="trendData" :type="trendType" />
    </n-card>

    <!-- 分类占比 -->
    <n-grid :cols="1" :x-gap="20" style="margin-top: 20px">
      <n-grid-item>
        <n-card>
          <template #header>
            <n-space justify="space-between" align="center">
              <n-text strong>支出分类占比</n-text>
              <n-tag type="info">TOP 8</n-tag>
            </n-space>
          </template>
          <n-grid :cols="1" :x-gap="20">
            <n-grid-item>
              <pie-chart :data="categoryPieData" />
            </n-grid-item>
          </n-grid>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 排行榜 -->
    <n-grid :cols="1" :x-gap="20" style="margin-top: 20px">
      <n-grid-item>
        <n-card>
          <template #header>
            <n-text strong>🏆 支出排行榜</n-text>
          </template>
          <n-tabs type="line">
            <n-tab-pane name="category" tab="分类 TOP5">
              <n-space vertical>
                <n-space
                  v-for="(item, index) in topCategories"
                  :key="item.categoryId"
                  justify="space-between"
                  align="center"
                  style="padding: 8px 0"
                >
                  <n-space align="center">
                    <n-text :type="index < 3 ? 'error' : 'default'" strong>
                      {{ index + 1 }}.
                    </n-text>
                    <n-text>{{ item.categoryName }}</n-text>
                  </n-space>
                  <n-text type="error" strong>¥{{ formatAmount(item.amount) }}</n-text>
                </n-space>
              </n-space>
            </n-tab-pane>

            <n-tab-pane name="day" tab="日期 TOP5">
              <n-space vertical>
                <n-space
                  v-for="(item, index) in topDays"
                  :key="item.date"
                  justify="space-between"
                  align="center"
                  style="padding: 8px 0"
                >
                  <n-space align="center">
                    <n-text :type="index < 3 ? 'error' : 'default'" strong>
                      {{ index + 1 }}.
                    </n-text>
                    <n-text>{{ formatDate(item.date) }}</n-text>
                  </n-space>
                  <n-text type="error" strong>¥{{ formatAmount(item.amount) }}</n-text>
                </n-space>
              </n-space>
            </n-tab-pane>

            <n-tab-pane name="single" tab="单笔 TOP5">
              <n-space vertical>
                <n-space
                  v-for="(item, index) in topSingleExpenses"
                  :key="item.id"
                  justify="space-between"
                  align="center"
                  style="padding: 8px 0"
                >
                  <n-space align="center">
                    <n-text :type="index < 3 ? 'error' : 'default'" strong>
                      {{ index + 1 }}.
                    </n-text>
                    <n-text depth="3">{{ item.note || '无备注' }}</n-text>
                  </n-space>
                  <n-text type="error" strong>¥{{ formatAmount(item.amount) }}</n-text>
                </n-space>
              </n-space>
            </n-tab-pane>
          </n-tabs>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import {
  ArrowUpOutline,
  ArrowDownOutline,
  WalletOutline
} from '@vicons/ionicons5'
import { useStatisticsStore } from '@/stores/statistics'
import TimeRangePicker from '@/components/statistics/TimeRangePicker.vue'
import SummaryCard from '@/components/statistics/SummaryCard.vue'
import TrendChart from '@/components/statistics/TrendChart.vue'
import PieChart from '@/components/statistics/PieChart.vue'

const statsStore = useStatisticsStore()

const dateRange = reactive({
  startDate: null,
  endDate: null,
  preset: 'month'
})

const trendType = ref('line')

const loading = computed(() => statsStore.loading)
const overview = computed(() => statsStore.overview)
const trendData = computed(() => statsStore.trendData)
const categoryPieData = computed(() => statsStore.categoryPieData)
const topCategories = computed(() => statsStore.topCategories)
const topDays = computed(() => statsStore.topDays)
const topSingleExpenses = computed(() => statsStore.rankings.single || [])

const balance = computed(() => {
  return (overview.value?.income || 0) - (overview.value?.expense || 0)
})

const balanceColor = computed(() => {
  return balance.value >= 0 ? '#10B981' : '#EF4444'
})

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 监听日期范围变化
watch(() => ({ ...dateRange }), () => {
  statsStore.setDateRange(dateRange.startDate, dateRange.endDate)
  statsStore.refreshAll()
}, { deep: true })

onMounted(() => {
  statsStore.refreshAll()
})
</script>

<style scoped>
.statistics-overview {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
