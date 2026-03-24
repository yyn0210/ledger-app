import request from './request'

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
}
