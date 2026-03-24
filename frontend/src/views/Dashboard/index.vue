<template>
  <div class="dashboard">
    <n-grid :cols="4" :x-gap="20" class="stats-grid">
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">本月收入</div>
            <div class="stat-value income">¥12,345.67</div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">本月支出</div>
            <div class="stat-value expense">¥8,234.56</div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">本月结余</div>
            <div class="stat-value">¥4,111.11</div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">预算剩余</div>
            <div class="stat-value">¥6,765.44</div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

<<<<<<< HEAD
    <n-card title="最近交易">
      <n-data-table :columns="columns" :data="data" :pagination="pagination" />
=======
    <n-grid :cols="2" :x-gap="20" class="charts-grid">
      <n-grid-item>
        <n-card title="收支趋势">
          <div class="chart-placeholder">图表区域</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="分类统计">
          <div class="chart-placeholder">图表区域</div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="最近交易" class="recent-transactions">
      <n-data-table
        :columns="columns"
        :data="data"
        :pagination="pagination"
      />
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
    </n-card>
  </div>
</template>

<script setup>
import { h } from 'vue'
import { NTag } from 'naive-ui'

const renderTag = type => {
<<<<<<< HEAD
  const typeMap = { income: { type: 'success', content: '收入' }, expense: { type: 'error', content: '支出' } }
=======
  const typeMap = {
    income: { type: 'success', content: '收入' },
    expense: { type: 'error', content: '支出' }
  }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
  const config = typeMap[type] || { type: 'default', content: type }
  return h(NTag, { type: config.type }, { default: () => config.content })
}

const columns = [
<<<<<<< HEAD
  { title: '日期', key: 'date', width: 120 },
  { title: '类型', key: 'type', width: 80, render: row => renderTag(row.type) },
  { title: '分类', key: 'category', width: 100 },
  { title: '金额', key: 'amount', width: 100, render: row => `¥${row.amount}` },
  { title: '备注', key: 'note', ellipsis: { tooltip: true } }
]

const data = [
  { key: '1', date: '2026-03-22', type: 'expense', category: '餐饮', amount: '56.00', note: '午餐' },
  { key: '2', date: '2026-03-22', type: 'expense', category: '交通', amount: '20.00', note: '地铁' },
  { key: '3', date: '2026-03-21', type: 'income', category: '工资', amount: '10000.00', note: '3 月工资' }
]

const pagination = { pageSize: 5 }
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 20px; }
.stats-grid { margin-bottom: 20px; }
.stat-card { height: 120px; }
.stat-content { display: flex; flex-direction: column; justify-content: center; height: 100%; }
.stat-label { font-size: 14px; color: #666; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; color: #333; }
.stat-value.income { color: var(--income-color); }
.stat-value.expense { color: var(--expense-color); }
=======
  {
    title: '日期',
    key: 'date',
    width: 120
  },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: row => renderTag(row.type)
  },
  {
    title: '分类',
    key: 'category',
    width: 100
  },
  {
    title: '金额',
    key: 'amount',
    width: 100,
    render: row => `¥${row.amount}`
  },
  {
    title: '备注',
    key: 'note',
    ellipsis: {
      tooltip: true
    }
  }
]

const data = [
  {
    key: '1',
    date: '2026-03-22',
    type: 'expense',
    category: '餐饮',
    amount: '56.00',
    note: '午餐'
  },
  {
    key: '2',
    date: '2026-03-22',
    type: 'expense',
    category: '交通',
    amount: '20.00',
    note: '地铁'
  },
  {
    key: '3',
    date: '2026-03-21',
    type: 'income',
    category: '工资',
    amount: '10000.00',
    note: '3 月工资'
  },
  {
    key: '4',
    date: '2026-03-21',
    type: 'expense',
    category: '购物',
    amount: '299.00',
    note: '衣服'
  }
]

const pagination = {
  pageSize: 5
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-grid {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-value.income {
  color: var(--income-color);
}

.stat-value.expense {
  color: var(--expense-color);
}

.charts-grid {
  margin-bottom: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #999;
  background-color: #f5f7f9;
  border-radius: 4px;
}

.recent-transactions {
  min-height: 400px;
}
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
</style>
