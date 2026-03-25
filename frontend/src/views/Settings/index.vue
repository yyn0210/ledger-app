<template>
  <div class="settings">
    <n-card title="个人设置">
      <n-form label-placement="top" label-width="100">
        <n-form-item label="昵称"><n-input placeholder="请输入昵称" /></n-form-item>
        <n-form-item label="手机号"><n-input placeholder="请输入手机号" /></n-form-item>
        <n-form-item label="邮箱"><n-input placeholder="请输入邮箱" /></n-form-item>
        <n-form-item><n-button type="primary">保存</n-button></n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<style scoped>
.settings { display: flex; flex-direction: column; gap: 20px; max-width: 800px; }
</style>
