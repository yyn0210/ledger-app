import { defineStore } from 'pinia'
<<<<<<< HEAD
=======
import { login, register, getCurrentUser, logout } from '@/api/auth'
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3

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
<<<<<<< HEAD
    logout() {
=======
    
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
