<template>
  <div class="dashboard">
    <n-grid :cols="4" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card title="本月收入">
          <div class="stat-value income">¥8,500.00</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="本月支出">
          <div class="stat-value expense">¥3,280.50</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="本月结余">
          <div class="stat-value balance">¥5,219.50</div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="预算进度">
          <div class="stat-value">61.4%</div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="最近交易" style="margin-top: 16px">
      <n-empty description="暂无交易记录" />
    </n-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;

  &.income {
    color: #52c41a;
  }

  &.expense {
    color: #ff6b6b;
  }

  &.balance {
    color: #3385ff;
  }
}
</style>
