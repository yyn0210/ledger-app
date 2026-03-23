import request from './request'

// 获取交易记录列表
export function getTransactionList(params) {
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
}

// 创建交易记录
export function createTransaction(data) {
  return request({
    url: '/transactions',
    method: 'post',
    data
  })
}

// 更新交易记录
export function updateTransaction(id, data) {
  return request({
    url: `/transactions/${id}`,
    method: 'put',
    data
  })
}

// 删除交易记录
export function deleteTransaction(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'delete'
  })
}

// 获取交易记录详情
export function getTransactionDetail(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'get'
  })
}

// 获取统计数据
export function getStats(params) {
  return request({
    url: '/transactions/stats',
    method: 'get',
    params
  })
}
