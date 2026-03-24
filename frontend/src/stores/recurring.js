import { defineStore } from 'pinia'
import { getRecurringList, createRecurring, updateRecurring, deleteRecurring } from '@/api/recurring'

export const useRecurringStore = defineStore('recurring', {
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
    activeRecurring: state => state.list.filter(r => r.status === 'active'),
    upcomingRecurring: state => state.list.filter(r => r.status === 'active').sort((a, b) => a.nextDate - b.nextDate)
  },
  actions: {
    async fetchList(params = {}) {
      this.loading = true
      this.error = null
      try {
        const res = await getRecurringList({ ...this.pagination, ...params })
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
      const res = await createRecurring(data)
      await this.fetchList()
      return res.data
    },
    async update(id, data) {
      const res = await updateRecurring(id, data)
      await this.fetchList()
      return res.data
    },
    async delete(id) {
      await deleteRecurring(id)
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
