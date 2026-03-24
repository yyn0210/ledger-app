import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取预算列表
export function getBudgetList(params) {
  if (USE_MOCK) {
    return mockApi.budget.getList(params)
  }
  return request.get('/budget/list', { params })
}

// 创建预算
export function createBudget(data) {
  if (USE_MOCK) {
    return mockApi.budget.create(data)
  }
  return request.post('/budget', data)
}

// 更新预算
export function updateBudget(id, data) {
  if (USE_MOCK) {
    return mockApi.budget.update(id, data)
  }
  return request.put(`/budget/${id}`, data)
}

// 删除预算
export function deleteBudget(id) {
  if (USE_MOCK) {
    return mockApi.budget.delete(id)
  }
  return request.delete(`/budget/${id}`)
}

// 获取预算进度
export function getBudgetProgress(id) {
  if (USE_MOCK) {
    return mockApi.budget.getProgress(id)
  }
  return request.get(`/budget/${id}/progress`)
}
