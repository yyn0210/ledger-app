import request from './request'

// 获取交易列表
export function getTransactionList(params) {
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
}

// 创建交易
export function createTransaction(data) {
  return request({
    url: '/transactions',
    method: 'post',
    data
  })
}

// 获取交易详情
export function getTransactionDetail(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'get'
  })
}

// 更新交易
export function updateTransaction(id, data) {
  return request({
    url: `/transactions/${id}`,
    method: 'put',
    data
  })
}

// 删除交易
export function deleteTransaction(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'delete'
  })
}

// 批量导入交易
export function importTransactions(data) {
  return request({
    url: '/transactions/import',
    method: 'post',
    data
  })
}
