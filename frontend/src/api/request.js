import axios from 'axios'
import { createMessage } from 'naive-ui'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
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
    createMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, data, message } = response.data
    if (code === 200) {
      return data
    }
    createMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        createMessage.error('未授权，请重新登录')
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status === 403) {
        createMessage.error('无权访问')
      } else if (status === 404) {
        createMessage.error('请求资源不存在')
      } else if (status === 500) {
        createMessage.error('服务器错误')
      } else {
        createMessage.error(data?.message || error.message || '网络错误')
      }
    } else {
      createMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
