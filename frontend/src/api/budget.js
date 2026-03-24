import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取预算列表
export function getBudgetList(params) {
  if (USE_MOCK) {
    return mockApi.budgets.list(params)
  }
  return request.get('/budgets', { params })
}

// 创建预算
export function createBudget(data) {
  if (USE_MOCK) {
    return mockApi.budgets.create(data)
  }
  return request.post('/budgets', data)
}

// 更新预算
export function updateBudget(id, data) {
  if (USE_MOCK) {
    return mockApi.budgets.update(id, data)
  }
  return request.put(`/budgets/${id}`, data)
}

// 删除预算
export function deleteBudget(id) {
  if (USE_MOCK) {
    return mockApi.budgets.delete(id)
  }
  return request.delete(`/budgets/${id}`)
}

// 获取预算进度
export function getBudgetProgress(budgetId, month) {
  if (USE_MOCK) {
    return mockApi.budgets.progress(budgetId, month)
  }
  return request.get(`/budgets/${budgetId}/progress`, { params: { month } })
}
