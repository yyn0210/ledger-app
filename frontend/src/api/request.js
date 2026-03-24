import axios from 'axios'
<<<<<<< HEAD

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(
  response => {
    const { code, data, message } = response.data
    if (code === 200) return data
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
=======
import { mockApi } from '@/mock'

// 是否启用 Mock 模式
const USE_MOCK = true

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
    return Promise.reject(error)
  }
)

<<<<<<< HEAD
export default request
=======
// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是 200，说明接口有错误
    if (res.code !== 200) {
      // 401: 未授权，跳转登录
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else if (error.request) {
      message = '无法连接到服务器'
    }
    
    return Promise.reject(new Error(message))
  }
)

// Mock 包装器
const mockWrapper = {
  get: (apiFunc, params) => {
    if (USE_MOCK) {
      console.log('[Mock] GET', apiFunc.name, params)
      return apiFunc(params)
    }
    return null
  },
  post: (apiFunc, data) => {
    if (USE_MOCK) {
      console.log('[Mock] POST', apiFunc.name, data)
      return apiFunc(data)
    }
    return null
  },
  put: (apiFunc, id, data) => {
    if (USE_MOCK) {
      console.log('[Mock] PUT', apiFunc.name, id, data)
      return apiFunc(id, data)
    }
    return null
  },
  delete: (apiFunc, id) => {
    if (USE_MOCK) {
      console.log('[Mock] DELETE', apiFunc.name, id)
      return apiFunc(id)
    }
    return null
  }
}

// 导出请求方法
export default {
  get: (url, params) => {
    if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
    return request.get(url, { params })
  },
  post: (url, data) => {
    if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
    return request.post(url, data)
  },
  put: (url, data) => {
    if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
    return request.put(url, data)
  },
  delete: (url) => {
    if (USE_MOCK) return Promise.resolve({ code: 200, data: null })
    return request.delete(url)
  },
  // Mock 包装器
  mock: mockWrapper
}
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
