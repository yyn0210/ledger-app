<template>
  <div class="statistics">
    <n-card title="统计筛选">
      <n-space>
        <n-date-picker v-model:value="dateRange" type="monthrange" placeholder="选择月份范围" />
        <n-button type="primary">统计</n-button>
      </n-space>
    </n-card>
    <n-grid :cols="2" :x-gap="20" :y-gap="20">
      <n-grid-item><n-card title="收支对比"><div class="chart-placeholder">图表区域</div></n-card></n-grid-item>
      <n-grid-item><n-card title="支出分类"><div class="chart-placeholder">图表区域</div></n-card></n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const dateRange = ref(null)
</script>

<style scoped>
.statistics { display: flex; flex-direction: column; gap: 20px; }
.chart-placeholder {
  height: 300px; display: flex; justify-content: center; align-items: center;
  color: #999; background-color: #f5f7f9; border-radius: 4px;
}
</style>
