import { defineStore } from 'pinia'
import { getBudgetList, createBudget, updateBudget, deleteBudget } from '@/api/budget'

export const useBudgetStore = defineStore('budget', {
  state: () => ({
    list: [],
    loading: false,
    error: null,
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0
    }
  }),
  getters: {
    activeBudgets: state => state.list.filter(b => b.status === 'active'),
    overspentBudgets: state => state.list.filter(b => b.spent > b.amount),
    totalBudget: state => state.list.reduce((sum, b) => sum + b.amount, 0),
    totalSpent: state => state.list.reduce((sum, b) => sum + (b.spent || 0), 0)
  },
  actions: {
    async fetchList(params = {}) {
      this.loading = true
      this.error = null
      try {
        const res = await getBudgetList({ ...this.pagination, ...params })
        this.list = res.data.list || []
        this.pagination.total = res.data.total || 0
        return res.data
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      const res = await createBudget(data)
      await this.fetchList()
      return res.data
    },
    async update(id, data) {
      const res = await updateBudget(id, data)
      await this.fetchList()
      return res.data
    },
    async delete(id) {
      await deleteBudget(id)
      await this.fetchList()
    },
    reset() {
      this.list = []
      this.loading = false
      this.error = null
      this.pagination = { page: 1, pageSize: 20, total: 0 }
    }
  }
})
