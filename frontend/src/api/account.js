import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取账户列表
export function getAccountList(params) {
  if (USE_MOCK) {
    return mockApi.accounts.list(params)
  }
  return request.get('/accounts', { params })
}

// 创建账户
export function createAccount(data) {
  if (USE_MOCK) {
    return mockApi.accounts.create(data)
  }
  return request.post('/accounts', data)
}

// 更新账户
export function updateAccount(id, data) {
  if (USE_MOCK) {
    return mockApi.accounts.update(id, data)
  }
  return request.put(`/accounts/${id}`, data)
}

// 删除账户
export function deleteAccount(id) {
  if (USE_MOCK) {
    return mockApi.accounts.delete(id)
  }
  return request.delete(`/accounts/${id}`)
}

// 获取账户详情
export function getAccountDetail(id) {
  if (USE_MOCK) {
    const account = mockApi.accounts.list().then(res => res.data.list.find(a => a.id === id))
    return account || Promise.reject({ code: 404, message: '账户不存在' })
  }
  return request.get(`/accounts/${id}`)
}
