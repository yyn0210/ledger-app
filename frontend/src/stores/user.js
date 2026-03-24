import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    loading: false
  }),
  getters: {
    isLoggedIn: state => !!state.user,
    username: state => state.user?.username || '',
    nickname: state => state.user?.nickname || ''
  },
  actions: {
    async fetchUser() {
      this.loading = true
      try {
        const { data } = await getCurrentUser()
        this.user = data
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.user = null
      } finally {
        this.loading = false
      }
    },
    setUser(user) {
      this.user = user
    },
    clearUser() {
      this.user = null
    }
  }
})
