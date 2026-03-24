import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取交易列表
export function getTransactionList(params) {
  if (USE_MOCK) {
    return mockApi.transactions.list(params)
  }
  return request.get('/transactions', { params })
}

// 创建交易
export function createTransaction(data) {
  if (USE_MOCK) {
    return mockApi.transactions.create(data)
  }
  return request.post('/transactions', data)
}

// 更新交易
export function updateTransaction(id, data) {
  if (USE_MOCK) {
    return mockApi.transactions.update(id, data)
  }
  return request.put(`/transactions/${id}`, data)
}

// 删除交易
export function deleteTransaction(id) {
  if (USE_MOCK) {
    return mockApi.transactions.delete(id)
  }
  return request.delete(`/transactions/${id}`)
}

// 获取交易详情
export function getTransactionDetail(id) {
  if (USE_MOCK) {
    const transaction = mockApi.transactions.list().then(res => 
      res.data.list.find(t => t.id === id)
    )
    return transaction || Promise.reject({ code: 404, message: '交易不存在' })
  }
  return request.get(`/transactions/${id}`)
}

// 获取交易统计
export function getTransactionStats(params) {
  if (USE_MOCK) {
    return mockApi.transactions.stats(params)
  }
  return request.get('/transactions/stats', { params })
}
