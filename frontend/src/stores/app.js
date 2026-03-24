import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    theme: 'light',
    loading: false
  }),
  getters: {
    sidebarWidth: state => (state.sidebarCollapsed ? 64 : 240)
  },
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setSidebarCollapsed(collapsed) {
      this.sidebarCollapsed = collapsed
<<<<<<< HEAD
=======
    },
    setTheme(theme) {
      this.theme = theme
    },
    setLoading(loading) {
      this.loading = loading
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
    }
  }
})
