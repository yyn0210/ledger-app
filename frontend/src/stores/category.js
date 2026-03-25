import { defineStore } from 'pinia'
import {
  getCategoryList,
  createCategory,
  getCategoryDetail,
  updateCategory,
  deleteCategory
} from '@/api/category'

export const useCategoryStore = defineStore('category', {
  state: () => ({
    categoryList: [],
    currentCategory: null,
    loading: false,
    filterType: 'all' // 'all' | 'income' | 'expense'
  }),
  getters: {
    // 按类型筛选后的分类列表
    filteredCategories: state => {
      if (state.filterType === 'all') return state.categoryList
      return state.categoryList.filter(cat => cat.type === state.filterType)
    },
    // 一级分类
    rootCategories: state => {
      return state.categoryList.filter(cat => !cat.parentId)
    },
    // 获取某分类的子分类
    getChildCategories: state => parentId => {
      return state.categoryList.filter(cat => cat.parentId === parentId)
    },
    // 获取分类映射（用于快速查找）
    categoryMap: state => {
      const map = {}
      state.categoryList.forEach(cat => {
        map[cat.id] = cat
      })
      return map
    }
  },
  actions: {
    /**
     * 获取分类列表
     * @param {Object} params - 查询参数
     */
    async fetchCategoryList(params = {}) {
      this.loading = true
      try {
        const data = await getCategoryList(params)
        this.categoryList = data.list || data.items || data
        if (params.type) {
          this.filterType = params.type
        }
      } catch (error) {
        console.error('获取分类列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取分类详情
     * @param {number|string} id - 分类 ID
     */
    async fetchCurrentCategory(id) {
      this.loading = true
      try {
        const data = await getCategoryDetail(id)
        this.currentCategory = data
        return data
      } catch (error) {
        console.error('获取分类详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建分类
     * @param {Object} data - 分类数据
     */
    async createCategory(data) {
      this.loading = true
      try {
        const result = await createCategory(data)
        await this.fetchCategoryList()
        return result
      } catch (error) {
        console.error('创建分类失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新分类
     * @param {number|string} id - 分类 ID
     * @param {Object} data - 分类数据
     */
    async updateCategory(id, data) {
      this.loading = true
      try {
        const result = await updateCategory(id, data)
        // 更新本地状态
        const index = this.categoryList.findIndex(cat => cat.id === id)
        if (index !== -1) {
          this.categoryList[index] = { ...this.categoryList[index], ...data }
        }
        if (this.currentCategory?.id === id) {
          this.currentCategory = { ...this.currentCategory, ...data }
        }
        return result
      } catch (error) {
        console.error('更新分类失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除分类
     * @param {number|string} id - 分类 ID
     */
    async deleteCategory(id) {
      this.loading = true
      try {
        await deleteCategory(id)
        // 从本地状态移除
        this.categoryList = this.categoryList.filter(cat => cat.id !== id)
        // 同时移除子分类
        this.categoryList = this.categoryList.filter(cat => cat.parentId !== id)
        if (this.currentCategory?.id === id) {
          this.currentCategory = null
        }
      } catch (error) {
        console.error('删除分类失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前分类
     */
    clearCurrentCategory() {
      this.currentCategory = null
    },

    /**
     * 设置筛选类型
     * @param {'all'|'income'|'expense'} type
     */
    setFilterType(type) {
      this.filterType = type
    }
  }
})
