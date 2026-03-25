<template>
  <div class="recurring-list">
    <!-- 头部操作区 -->
    <n-card>
      <n-space justify="space-between" align="center">
        <n-space>
          <h2 style="margin: 0">📅 周期账单</h2>
          <n-space>
            <n-tag v-if="upcomingCount > 0" type="warning">
              ⏰ {{ upcomingCount }} 个即将执行
            </n-tag>
            <n-tag v-if="activeCount > 0" type="success">
              ✅ {{ activeCount }} 个执行中
            </n-tag>
          </n-space>
        </n-space>
        <n-button type="primary" @click="handleCreate">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新建周期账单
        </n-button>
      </n-space>

      <!-- 筛选条件 -->
      <n-space style="margin-top: 16px" wrap>
        <n-segmented
          v-model:value="filterStatus"
          :options="[
            { label: '全部', value: 'all' },
            { label: '执行中', value: 'active' },
            { label: '已暂停', value: 'paused' },
            { label: '已完成', value: 'completed' }
          ]"
          @update:value="handleFilterChange"
        />
        <n-segmented
          v-model:value="filterType"
          :options="[
            { label: '全部', value: 'all' },
            { label: '每日', value: 'daily' },
            { label: '每周', value: 'weekly' },
            { label: '每月', value: 'monthly' },
            { label: '每年', value: 'yearly' }
          ]"
          @update:value="handleFilterChange"
        />
      </n-space>
    </n-card>

    <!-- 周期账单概览 -->
    <n-card style="margin-top: 20px">
      <n-grid :cols="4" :x-gap="20">
        <n-grid-item>
          <n-statistic label="总账单数">
            <n-text strong>{{ billCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="执行中">
            <n-text type="success" strong>{{ activeCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="已暂停">
            <n-text type="warning" strong>{{ pausedCount }}</n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="即将执行">
            <n-text type="info" strong>{{ upcomingCount }}</n-text>
          </n-statistic>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 周期账单列表 -->
    <n-card style="margin-top: 20px">
      <template #header>
        <n-space justify="space-between" align="center">
          <span>周期账单列表</span>
          <n-tag v-if="filteredBills.length > 0" type="info">
            共 {{ filteredBills.length }} 个账单
          </n-tag>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-empty
          v-if="!loading && filteredBills.length === 0"
          description="暂无周期账单，点击右上角创建一个吧～"
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
          <n-grid-item v-for="bill in filteredBills" :key="bill.id">
            <recurring-card
              :bill="bill"
              :show-actions="true"
              @edit="handleEdit"
              @delete="handleDelete"
              @execute="handleExecute"
              @toggle-status="handleToggleStatus"
            />
          </n-grid-item>
        </n-grid>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { useRecurringBillStore } from '@/stores/recurring'
import RecurringCard from '@/components/recurring/RecurringCard.vue'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const recurringBillStore = useRecurringBillStore()

const loading = computed(() => recurringBillStore.loading)
const filteredBills = computed(() => recurringBillStore.filteredBills)
const billCount = computed(() => recurringBillStore.billCount)
const activeCount = computed(() => recurringBillStore.activeCount)
const pausedCount = computed(() => recurringBillStore.pausedCount)
const upcomingCount = computed(() => recurringBillStore.upcomingCount)

const filterStatus = ref(recurringBillStore.filterStatus)
const filterType = ref(recurringBillStore.filterType)

// 筛选变化
const handleFilterChange = () => {
  recurringBillStore.setFilterStatus(filterStatus.value)
  recurringBillStore.setFilterType(filterType.value)
}

// 新建周期账单
const handleCreate = () => {
  router.push('/recurring/create')
}

// 编辑周期账单
const handleEdit = (bill) => {
  router.push(`/recurring/${bill.id}/edit`)
}

// 删除周期账单
const handleDelete = (bill) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除周期账单「${bill.name}」吗？删除后无法恢复！`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await recurringBillStore.deleteRecurringBill(bill.id)
        message.success('周期账单已删除')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 手动执行周期账单
const handleExecute = async (bill) => {
  try {
    await recurringBillStore.executeRecurringBill(bill.id)
    message.success('周期账单已执行')
  } catch (error) {
    message.error('执行失败：' + (error.message || '未知错误'))
  }
}

// 暂停/恢复周期账单
const handleToggleStatus = async (bill) => {
  const pause = bill.status === 1 // 当前是执行中则暂停
  try {
    await recurringBillStore.toggleRecurringBillStatus(bill.id, pause)
    message.success(pause ? '周期账单已暂停' : '周期账单已恢复')
  } catch (error) {
    message.error('操作失败：' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  recurringBillStore.fetchRecurringBillList()
})
</script>

<style scoped>
.recurring-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
