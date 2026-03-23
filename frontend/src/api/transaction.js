import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取交易记录列表
export function getTransactionList(params) {
  if (USE_MOCK) {
    return mockApi.transactions.list(params)
  }
  return request.get('/transactions', { params })
}

// 创建交易记录
export function createTransaction(data) {
  if (USE_MOCK) {
    return mockApi.transactions.create(data)
  }
  return request.post('/transactions', data)
}

// 更新交易记录
export function updateTransaction(id, data) {
  if (USE_MOCK) {
    return mockApi.transactions.update(id, data)
  }
  return request.put(`/transactions/${id}`, data)
}

// 删除交易记录
export function deleteTransaction(id) {
  if (USE_MOCK) {
    return mockApi.transactions.delete(id)
  }
  return request.delete(`/transactions/${id}`)
}

// 获取交易记录详情
export function getTransactionDetail(id) {
  if (USE_MOCK) {
    return mockApi.transactions.list().then(res => {
      const transaction = res.data.list.find(t => t.id === id)
      if (!transaction) throw { code: 404, message: '交易记录不存在' }
      return { code: 200, data: transaction }
    })
  }
  return request.get(`/transactions/${id}`)
}

// 获取统计数据
export function getStats(params) {
  if (USE_MOCK) {
    return mockApi.stats.monthly(params)
  }
  return request.get('/transactions/stats', { params })
}
