import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: null,
    isLoggedIn: false
  }),
  
  getters: {
    userId: (state) => state.userInfo?.id,
    userName: (state) => state.userInfo?.name,
    userAvatar: (state) => state.userInfo?.avatar
  },
  
  actions: {
    // 登录
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        this.token = res.data.token
        uni.setStorageSync('token', res.data.token)
        await this.getUserInfo()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 获取用户信息
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        this.isLoggedIn = true
        return res
      } catch (error) {
        this.logout()
        return Promise.reject(error)
      }
    },
    
    // 退出登录
    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        this.isLoggedIn = false
        uni.removeStorageSync('token')
      }
    }
  }
})
