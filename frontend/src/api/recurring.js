import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取周期账单列表
export function getRecurringList(params) {
  if (USE_MOCK) {
    return mockApi.recurring.getList(params)
  }
  return request.get('/recurring/list', { params })
}

// 创建周期账单
export function createRecurring(data) {
  if (USE_MOCK) {
    return mockApi.recurring.create(data)
  }
  return request.post('/recurring', data)
}

// 更新周期账单
export function updateRecurring(id, data) {
  if (USE_MOCK) {
    return mockApi.recurring.update(id, data)
  }
  return request.put(`/recurring/${id}`, data)
}

// 删除周期账单
export function deleteRecurring(id) {
  if (USE_MOCK) {
    return mockApi.recurring.delete(id)
  }
  return request.delete(`/recurring/${id}`)
}

// 获取执行记录
export function getExecutionHistory(id) {
  if (USE_MOCK) {
    return mockApi.recurring.getHistory(id)
  }
  return request.get(`/recurring/${id}/history`)
}

// 手动执行一次
export function executeRecurring(id) {
  if (USE_MOCK) {
    return mockApi.recurring.execute(id)
  }
  return request.post(`/recurring/${id}/execute`)
}
