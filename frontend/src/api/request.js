import axios from 'axios'
import { useMessage } from 'naive-ui'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code !== 200) {
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
          message = error.response.data?.message || message
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    }
    
    console.error(message)
    return Promise.reject(new Error(message))
  }
)

// 添加便捷方法
request.get = (url, config) => request({ url, method: 'get', ...config })
request.post = (url, data, config) => request({ url, method: 'post', data, ...config })
request.put = (url, data, config) => request({ url, method: 'put', data, ...config })
request.delete = (url, config) => request({ url, method: 'delete', ...config })

export default request
