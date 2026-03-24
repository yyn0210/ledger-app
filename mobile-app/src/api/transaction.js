import { mockDelay, addTransaction, getTransactions } from './mock'

const USE_MOCK = true

// 创建交易
export async function createTransaction(data) {
  if (USE_MOCK) {
    return await addTransaction(data)
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: '/api/transaction',
      method: 'POST',
      data,
      success: resolve,
      fail: reject
    })
  })
}

// 获取交易列表
export async function getTransactionList(params) {
  if (USE_MOCK) {
    return await getTransactions(params)
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: '/api/transaction/list',
      method: 'GET',
      data: params,
      success: resolve,
      fail: reject
    })
  })
}

// 获取交易详情
export async function getTransactionDetail(id) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, data: { id, name: 'Mock Transaction', amount: 100 } }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: `/api/transaction/${id}`,
      method: 'GET',
      success: resolve,
      fail: reject
    })
  })
}

// 更新交易
export async function updateTransaction(id, data) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, data: { id, ...data } }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: `/api/transaction/${id}`,
      method: 'PUT',
      data,
      success: resolve,
      fail: reject
    })
  })
}

// 删除交易
export async function deleteTransaction(id) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, message: '删除成功' }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: `/api/transaction/${id}`,
      method: 'DELETE',
      success: resolve,
      fail: reject
    })
  })
}
