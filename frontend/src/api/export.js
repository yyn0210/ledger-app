import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 导出数据
export function exportData(params) {
  if (USE_MOCK) {
    return mockApi.export.download(params)
  }
  return request.get('/export/download', { params, responseType: 'blob' })
}

// 导入数据
export function importData(file) {
  if (USE_MOCK) {
    return mockApi.export.upload(file)
  }
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/export/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取导出进度
export function getExportProgress(taskId) {
  if (USE_MOCK) {
    return mockApi.export.getProgress(taskId)
  }
  return request.get(`/export/progress/${taskId}`)
}

// 备份数据
export function backupData() {
  if (USE_MOCK) {
    return mockApi.export.backup()
  }
  return request.post('/export/backup')
}

// 恢复数据
export function restoreData(file) {
  if (USE_MOCK) {
    return mockApi.export.restore(file)
  }
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/export/restore', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
