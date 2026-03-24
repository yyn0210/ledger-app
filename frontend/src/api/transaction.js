import request from './request'
<<<<<<< HEAD

export function getTransactionList(params) {
  return request({ url: '/transactions', method: 'get', params })
}

export function createTransaction(data) {
  return request({ url: '/transactions', method: 'post', data })
}

export function getTransactionDetail(id) {
  return request({ url: `/transactions/${id}`, method: 'get' })
}

export function updateTransaction(id, data) {
  return request({ url: `/transactions/${id}`, method: 'put', data })
}

export function deleteTransaction(id) {
  return request({ url: `/transactions/${id}`, method: 'delete' })
=======
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}
