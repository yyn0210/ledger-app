import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    loading: false,
    theme: 'light'
  }),
  actions: {
    setSidebarCollapsed(collapsed) {
      this.sidebarCollapsed = collapsed
      localStorage.setItem('sidebarCollapsed', collapsed)
    },
    setLoading(loading) {
      this.loading = loading
    },
    setTheme(theme) {
      this.theme = theme
      localStorage.setItem('theme', theme)
    },
    init() {
      const collapsed = localStorage.getItem('sidebarCollapsed')
      if (collapsed) {
        this.sidebarCollapsed = collapsed === 'true'
      }
    }
  }
})
