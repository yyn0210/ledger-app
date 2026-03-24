<template>
  <div class="export-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Download" size="28" color="#3b82f6" />
        数据导出
      </h1>
    </div>

    <!-- 导出选项 -->
    <n-grid :cols="2" :x-gap="16" class="options-grid">
      <n-grid-item>
        <n-card class="option-card">
          <template #header>
            <div class="card-header">
              <n-icon :component="FileTray" size="24" color="#3b82f6" />
              <span>导出数据</span>
            </div>
          </template>

          <n-form
            ref="exportFormRef"
            :model="exportForm"
            label-placement="top"
          >
            <n-form-item label="导出格式">
              <n-select
                v-model:value="exportForm.format"
                :options="formatOptions"
              />
            </n-form-item>

            <n-form-item label="数据类型">
              <n-checkbox-group v-model:value="exportForm.dataTypes">
                <n-space>
                  <n-checkbox value="transactions">交易记录</n-checkbox>
                  <n-checkbox value="categories">分类</n-checkbox>
                  <n-checkbox value="accounts">账户</n-checkbox>
                  <n-checkbox value="budgets">预算</n-checkbox>
                </n-space>
              </n-checkbox-group>
            </n-form-item>

            <n-form-item label="时间范围">
              <n-date-picker
                v-model:value="exportForm.dateRange"
                type="daterange"
                placeholder="选择日期范围"
                style="width: 100%"
                clearable
              />
            </n-form-item>

            <n-form-item label="账本">
              <n-select
                v-model:value="exportForm.bookId"
                :options="bookOptions"
                placeholder="选择账本（可选）"
                clearable
              />
            </n-form-item>

            <n-space style="width: 100%; margin-top: 16px;">
              <n-button
                type="primary"
                size="large"
                @click="handleExport"
                :loading="exporting"
                style="width: 100%;"
              >
                <template #icon>
                  <n-icon :component="Download" />
                </template>
                开始导出
              </n-button>
            </n-space>
          </n-form>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card class="option-card">
          <template #header>
            <div class="card-header">
              <n-icon :component="Upload" size="24" color="#10b981" />
              <span>导入数据</span>
            </div>
          </template>

          <n-upload
            :max="1"
            accept=".csv,.xlsx,.json"
            :show-file-list="false"
            @finish="handleImport"
          >
            <n-space vertical align="center" style="padding: 40px 20px;">
              <n-icon :component="CloudUpload" size="48" color="#10b981" />
              <div class="upload-text">
                <div class="upload-title">点击或拖拽上传文件</div>
                <div class="upload-desc">支持 CSV、Excel、JSON 格式</div>
              </div>
              <n-button type="success">
                <template #icon>
                  <n-icon :component="Upload" />
                </template>
                选择文件
              </n-button>
            </n-space>
          </n-upload>

          <n-divider>或</n-divider>

          <n-space vertical style="width: 100%;">
            <n-button
              type="success"
              size="large"
              @click="handleBackup"
              :loading="backingUp"
              style="width: 100%;"
            >
              <template #icon>
                <n-icon :component="Save" />
              </template>
              备份全部数据
            </n-button>

            <n-upload
              :max="1"
              accept=".json"
              :show-file-list="false"
              @finish="handleRestore"
            >
              <n-button
                type="warning"
                size="large"
                style="width: 100%;"
              >
                <template #icon>
                  <n-icon :component="Restore" />
                </template>
                恢复备份
              </n-button>
            </n-upload>
          </n-space>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 导出历史 -->
    <n-card class="history-card" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>导出历史</span>
          <n-button size="small" @click="clearHistory">
            <template #icon>
              <n-icon :component="Trash" />
            </template>
            清空
          </n-button>
        </div>
      </template>

      <n-data-table
        :columns="historyColumns"
        :data="exportHistory"
        :pagination="false"
        striped
      />

      <n-empty
        v-if="exportHistory.length === 0"
        description="暂无导出记录"
        size="large"
        style="padding: 40px 0"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, h } from 'vue'
import { useMessage, NTag, NButton, NIcon } from 'naive-ui'
import {
  Download, CloudUploadOutline as Upload, FileTray, CloudUpload, Save, Refresh as Restore, Trash, CheckmarkCircle, Time
} from '@vicons/ionicons5'
import { exportData, importData, backupData, restoreData } from '@/api/export'

const message = useMessage()

const exporting = ref(false)
const backingUp = ref(false)
const exportHistory = ref([
  { id: 1, filename: 'transactions_20260324.csv', format: 'CSV', size: '1.2 MB', date: Date.now() - 3600000, status: 'success' },
  { id: 2, filename: 'backup_20260323.json', format: 'JSON', size: '3.5 MB', date: Date.now() - 86400000, status: 'success' }
])

const exportForm = reactive({
  format: 'csv',
  dataTypes: ['transactions'],
  dateRange: null,
  bookId: null
})

const formatOptions = [
  { label: 'CSV', value: 'csv' },
  { label: 'Excel (.xlsx)', value: 'xlsx' },
  { label: 'JSON', value: 'json' }
]

const bookOptions = [
  { label: '日常账本', value: 1 },
  { label: '旅行账本', value: 2 },
  { label: '装修账本', value: 3 }
]

const historyColumns = [
  {
    title: '文件名',
    key: 'filename',
    width: 200
  },
  {
    title: '格式',
    key: 'format',
    width: 80,
    render: (row) => h(NTag, {
      type: row.format === 'CSV' ? 'info' : row.format === 'xlsx' ? 'success' : 'warning',
      size: 'small'
    }, { default: () => row.format.toUpperCase() })
  },
  {
    title: '大小',
    key: 'size',
    width: 80
  },
  {
    title: '导出时间',
    key: 'date',
    width: 150,
    render: (row) => new Date(row.date).toLocaleString('zh-CN')
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => h(NTag, {
      type: row.status === 'success' ? 'success' : 'error',
      size: 'small'
    }, {
      default: () => row.status === 'success' ? '成功' : '失败',
      icon: () => h(NIcon, { component: row.status === 'success' ? CheckmarkCircle : Time })
    })
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) => h(NButton, {
      size: 'small',
      type: 'primary',
      onClick: () => handleDownload(row)
    }, { default: () => '下载' })
  }
]

const handleExport = async () => {
  if (exportForm.dataTypes.length === 0) {
    message.warning('请至少选择一种数据类型')
    return
  }

  try {
    exporting.value = true
    
    // Mock 导出
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const filename = `ledger_export_${new Date().toISOString().split('T')[0]}.${exportForm.format}`
    
    // 创建 Mock 文件下载
    const content = exportForm.format === 'csv' 
      ? '日期，类型，金额，分类，备注\n2026-03-24,支出，35.00,餐饮，午餐'
      : JSON.stringify({ exported: true, date: new Date().toISOString() }, null, 2)
    
    const blob = new Blob([content], { 
      type: exportForm.format === 'csv' ? 'text/csv' : 'application/json' 
    })
    
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
    
    // 添加到历史记录
    exportHistory.value.unshift({
      id: Date.now(),
      filename,
      format: exportForm.format,
      size: '0.1 MB',
      date: Date.now(),
      status: 'success'
    })
    
    message.success('导出成功！')
  } catch (error) {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleImport = async ({ file }) => {
  try {
    // Mock 导入
    await new Promise(resolve => setTimeout(resolve, 1500))
    message.success('导入成功！')
  } catch (error) {
    message.error('导入失败')
  }
}

const handleBackup = async () => {
  try {
    backingUp.value = true
    
    // Mock 备份
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const filename = `backup_${new Date().toISOString().split('T')[0]}.json`
    const content = JSON.stringify({ backup: true, date: new Date().toISOString() }, null, 2)
    const blob = new Blob([content], { type: 'application/json' })
    
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
    
    message.success('备份成功！')
  } catch (error) {
    message.error('备份失败')
  } finally {
    backingUp.value = false
  }
}

const handleRestore = async ({ file }) => {
  try {
    // Mock 恢复
    await new Promise(resolve => setTimeout(resolve, 1500))
    message.success('恢复成功！')
  } catch (error) {
    message.error('恢复失败')
  }
}

const handleDownload = (row) => {
  message.info(`下载 ${row.filename}`)
}

const clearHistory = () => {
  exportHistory.value = []
  message.success('已清空历史记录')
}
</script>

<style scoped>
.export-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.options-grid {
  margin-bottom: 16px;
}

.option-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}

.upload-text {
  text-align: center;
}

.upload-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.upload-desc {
  font-size: 13px;
  color: #6b7280;
}

.history-card {
  margin-bottom: 16px;
}

:deep(.n-card__content) {
  padding: 20px;
}
</style>
