import { defineStore } from 'pinia'
import { login, register, getCurrentUser, logout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: state => !!state.token,
    nickname: state => state.userInfo?.nickname || '用户'
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    
    // 登录
    async login(loginForm) {
      const data = await login(loginForm)
      this.setToken(data.token)
      this.setUserInfo(data.user)
      return data
    },
    
    // 注册
    async register(registerData) {
      return await register(registerData)
    },
    
    // 获取当前用户信息
    async fetchUserInfo() {
      const data = await getCurrentUser()
      this.setUserInfo(data)
      return data
    },
    
    // 退出登录
    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.logoutLocal()
      }
    },
    
    // 本地登出
    logoutLocal() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
