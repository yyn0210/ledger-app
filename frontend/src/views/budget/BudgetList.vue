<template>
  <div class="budget-list">
    <!-- 头部操作区 -->
    <n-card>
      <n-space justify="space-between" align="center">
        <n-space>
          <h2 style="margin: 0">🎯 预算管理</h2>
          <n-space>
            <n-tag v-if="overBudgetCount > 0" type="error">
              ⚠️ {{ overBudgetCount }} 个超支
            </n-tag>
            <n-tag v-if="warningCount > 0" type="warning">
              ⚠️ {{ warningCount }} 个预警
            </n-tag>
          </n-space>
        </n-space>
        <n-button type="primary" @click="handleCreate">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新建预算
        </n-button>
      </n-space>

      <!-- 筛选条件 -->
      <n-space style="margin-top: 16px" wrap>
        <n-segmented
          v-model:value="filterType"
          :options="[
            { label: '全部', value: 'all' },
            { label: '分类预算', value: 'category' },
            { label: '账户预算', value: 'account' }
          ]"
          @update:value="handleFilterChange"
        />
        <n-segmented
          v-model:value="filterPeriod"
          :options="[
            { label: '全部', value: 'all' },
            { label: '每周', value: 'week' },
            { label: '每月', value: 'month' },
            { label: '每年', value: 'year' }
          ]"
          @update:value="handleFilterChange"
        />
      </n-space>
    </n-card>

    <!-- 预算概览 -->
    <n-card style="margin-top: 20px">
      <n-grid :cols="4" :x-gap="20">
        <n-grid-item>
          <n-statistic label="总预算数">
            <n-text strong>{{ budgetCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="正常">
            <n-text type="success" strong>{{ normalCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="预警">
            <n-text type="warning" strong>{{ warningCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="超支">
            <n-text type="error" strong>{{ overBudgetCount }}</n-text>
          </n-statistic>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 预算列表 -->
    <n-card style="margin-top: 20px">
      <template #header>
        <n-space justify="space-between" align="center">
          <span>预算列表</span>
          <n-tag v-if="filteredBudgets.length > 0" type="info">
            共 {{ filteredBudgets.length }} 个预算
          </n-tag>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-empty
          v-if="!loading && filteredBudgets.length === 0"
          description="暂无预算，点击右上角创建一个吧～"
          style="padding: 60px 0"
        />

        <n-grid
          v-else
          :cols="1"
          :x-gap="20"
          :y-gap="20"
          :breakpoints="{
            640: 2,
            1024: 3
          }"
        >
          <n-grid-item v-for="budget in filteredBudgets" :key="budget.id">
            <budget-card
              :budget="budget"
              :show-actions="true"
              @edit="handleEdit"
              @delete="handleDelete"
            />
          </n-grid-item>
        </n-grid>
      </n-spin>
    </n-card>

    <!-- 预算图表 -->
    <budget-chart
      v-if="chartData.length"
      :data="chartData"
      type="line"
      title="预算执行趋势"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { useBudgetStore } from '@/stores/budget'
import BudgetCard from '@/components/budget/BudgetCard.vue'
import BudgetChart from '@/components/budget/BudgetChart.vue'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const budgetStore = useBudgetStore()

const loading = computed(() => budgetStore.loading)
const filteredBudgets = computed(() => budgetStore.filteredBudgets)
const budgetCount = computed(() => budgetStore.budgetCount)
const overBudgetCount = computed(() => budgetStore.overBudgetCount)
const warningCount = computed(() => budgetStore.warningCount)
const normalCount = computed(() => {
  return budgetCount.value - overBudgetCount.value - warningCount.value
})

const filterType = ref(budgetStore.filterType)
const filterPeriod = ref(budgetStore.filterPeriod)

// 模拟图表数据（实际应从 API 获取）
const chartData = computed(() => {
  // 这里返回模拟数据，实际应从后端获取
  return [
    { date: '第 1 周', budget: 1000, actual: 800, remaining: 200 },
    { date: '第 2 周', budget: 1000, actual: 950, remaining: 50 },
    { date: '第 3 周', budget: 1000, actual: 1100, remaining: -100 },
    { date: '第 4 周', budget: 1000, actual: 700, remaining: 300 }
  ]
})

// 筛选变化
const handleFilterChange = () => {
  budgetStore.setFilterType(filterType.value)
  budgetStore.setFilterPeriod(filterPeriod.value)
}

// 新建预算
const handleCreate = () => {
  router.push('/budget/create')
}

// 编辑预算
const handleEdit = (budget) => {
  router.push(`/budget/${budget.id}/edit`)
}

// 删除预算
const handleDelete = (budget) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除预算「${budget.name}」吗？删除后无法恢复！`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await budgetStore.deleteBudget(budget.id)
        message.success('预算已删除')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

onMounted(() => {
  budgetStore.fetchBudgetList()
})
</script>

<style scoped>
.budget-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
