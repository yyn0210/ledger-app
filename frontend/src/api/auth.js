import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 用户登录
export function login(data) {
  if (USE_MOCK) {
    return mockApi.auth.login(data)
  }
  return request.post('/auth/login', data)
}

// 用户注册
export function register(data) {
  if (USE_MOCK) {
    return mockApi.auth.register(data)
  }
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  if (USE_MOCK) {
    return mockApi.auth.me()
  }
  return request.get('/auth/me')
}

// 退出登录
export function logout() {
  if (USE_MOCK) {
    return mockApi.auth.logout()
  }
  return request.post('/auth/logout')
}
