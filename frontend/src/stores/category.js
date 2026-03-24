import { defineStore } from 'pinia'
import { getCategoryList } from '@/api/category'

export const useCategoryStore = defineStore('category', {
  state: () => ({
    expenseCategories: [],
    incomeCategories: [],
    loading: false
  }),
  getters: {
    allCategories: state => [...state.expenseCategories, ...state.incomeCategories],
    expenseCount: state => state.expenseCategories.length,
    incomeCount: state => state.incomeCategories.length
  },
  actions: {
    async fetchCategories(type = 'all') {
      this.loading = true
      try {
        const data = await getCategoryList({ type })
        if (type === 'expense' || type === 'all') {
          this.expenseCategories = data.defaults || []
        }
        if (type === 'income' || type === 'all') {
          this.incomeCategories = data.defaults || []
        }
      } catch (error) {
        console.error('获取分类列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    addCategory(category) {
      if (category.type === 'expense') {
        this.expenseCategories.push(category)
      } else {
        this.incomeCategories.push(category)
      }
    },
    updateCategory(id, updates) {
      const categories = [...this.expenseCategories, ...this.incomeCategories]
      const index = categories.findIndex(c => c.id === id)
      if (index > -1) {
        categories[index] = { ...categories[index], ...updates }
      }
    },
    removeCategory(id) {
      this.expenseCategories = this.expenseCategories.filter(c => c.id !== id)
      this.incomeCategories = this.incomeCategories.filter(c => c.id !== id)
    }
  }
})
