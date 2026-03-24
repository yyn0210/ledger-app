import { defineStore } from 'pinia'
import { getBudgetList, createBudget, updateBudget, deleteBudget } from '@/api/budget'

export const useBudgetStore = defineStore('budget', {
  state: () => ({
    budgets: [],
    loading: false,
    currentMonth: new Date().toISOString().slice(0, 7), // YYYY-MM
    filter: {
      type: 'category', // 'category' | 'account'
      status: 'all' // 'all' | 'active' | 'exceeded'
    }
  }),
  getters: {
    totalBudget: state => {
      return state.budgets.reduce((sum, b) => sum + (b.amount || 0), 0)
    },
    totalUsed: state => {
      return state.budgets.reduce((sum, b) => sum + (b.used || 0), 0)
    },
    exceededBudgets: state => {
      return state.budgets.filter(b => (b.used || 0) > (b.amount || 0))
    },
    activeBudgets: state => {
      return state.budgets.filter(b => (b.used || 0) < (b.amount || 0))
    }
  },
  actions: {
    async fetchBudgets(params = {}) {
      this.loading = true
      try {
        const data = await getBudgetList({ ...this.filter, ...params })
        this.budgets = data.list || data || []
      } catch (error) {
        console.error('获取预算列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    async addBudget(budgetData) {
      const data = await createBudget(budgetData)
      this.budgets.push(data)
      return data
    },
    async updateBudgetData(id, updates) {
      const data = await updateBudget(id, updates)
      const index = this.budgets.findIndex(b => b.id === id)
      if (index > -1) {
        this.budgets[index] = data
      }
      return data
    },
    async removeBudget(id) {
      await deleteBudget(id)
      this.budgets = this.budgets.filter(b => b.id !== id)
    },
    setFilter(filter) {
      this.filter = { ...this.filter, ...filter }
    },
    setMonth(month) {
      this.currentMonth = month
    }
  }
})
