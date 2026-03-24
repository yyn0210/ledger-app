import request from './request'
<<<<<<< HEAD

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

export function getCurrentUser() {
  return request({ url: '/auth/me', method: 'get' })
}

export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
=======
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}
