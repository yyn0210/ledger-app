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
    }
  }
})
