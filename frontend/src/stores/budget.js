import { defineStore } from 'pinia'
import {
  getBudgetList,
  createBudget,
  getBudgetDetail,
  getBudgetExecution,
  updateBudget,
  deleteBudget
} from '@/api/budget'

export const useBudgetStore = defineStore('budget', {
  state: () => ({
    budgetList: [],
    currentBudget: null,
    executionData: null,
    loading: false,
    filterType: 'all', // 'all' | 'category' | 'account'
    filterPeriod: 'all' // 'all' | 'week' | 'month' | 'year'
  }),
  getters: {
    // 按类型和周期筛选后的预算列表
    filteredBudgets: state => {
      let budgets = state.budgetList
      if (state.filterType !== 'all') {
        budgets = budgets.filter(b => b.type === state.filterType)
      }
      if (state.filterPeriod !== 'all') {
        budgets = budgets.filter(b => b.period === state.filterPeriod)
      }
      return budgets
    },
    // 预算总数
    budgetCount: state => state.budgetList.length,
    // 超支预算数量
    overBudgetCount: state => {
      return state.budgetList.filter(b => b.progress > 100).length
    },
    // 即将超支预算数量（>90%）
    warningCount: state => {
      return state.budgetList.filter(b => b.progress >= 90 && b.progress <= 100).length
    }
  },
  actions: {
    /**
     * 获取预算列表
     * @param {Object} params - 查询参数
     */
    async fetchBudgetList(params = {}) {
      this.loading = true
      try {
        const data = await getBudgetList(params)
        this.budgetList = data.list || data.items || data
      } catch (error) {
        console.error('获取预算列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取预算详情
     * @param {number|string} id - 预算 ID
     */
    async fetchCurrentBudget(id) {
      this.loading = true
      try {
        const data = await getBudgetDetail(id)
        this.currentBudget = data
        return data
      } catch (error) {
        console.error('获取预算详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取预算执行数据
     * @param {number|string} id - 预算 ID
     */
    async fetchExecutionData(id) {
      this.loading = true
      try {
        const data = await getBudgetExecution(id)
        this.executionData = data
        return data
      } catch (error) {
        console.error('获取执行数据失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建预算
     * @param {Object} data - 预算数据
     */
    async createBudget(data) {
      this.loading = true
      try {
        const result = await createBudget(data)
        await this.fetchBudgetList()
        return result
      } catch (error) {
        console.error('创建预算失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新预算
     * @param {number|string} id - 预算 ID
     * @param {Object} data - 预算数据
     */
    async updateBudget(id, data) {
      this.loading = true
      try {
        const result = await updateBudget(id, data)
        // 更新本地状态
        const index = this.budgetList.findIndex(b => b.id === id)
        if (index !== -1) {
          this.budgetList[index] = { ...this.budgetList[index], ...data }
        }
        if (this.currentBudget?.id === id) {
          this.currentBudget = { ...this.currentBudget, ...data }
        }
        return result
      } catch (error) {
        console.error('更新预算失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除预算
     * @param {number|string} id - 预算 ID
     */
    async deleteBudget(id) {
      this.loading = true
      try {
        await deleteBudget(id)
        // 从本地状态移除
        this.budgetList = this.budgetList.filter(b => b.id !== id)
        if (this.currentBudget?.id === id) {
          this.currentBudget = null
        }
      } catch (error) {
        console.error('删除预算失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前预算
     */
    clearCurrentBudget() {
      this.currentBudget = null
      this.executionData = null
    },

    /**
     * 设置筛选类型
     * @param {'all'|'category'|'account'} type
     */
    setFilterType(type) {
      this.filterType = type
    },

    /**
     * 设置筛选周期
     * @param {'all'|'week'|'month'|'year'} period
     */
    setFilterPeriod(period) {
      this.filterPeriod = period
    },

    /**
     * 重置筛选
     */
    resetFilters() {
      this.filterType = 'all'
      this.filterPeriod = 'all'
    }
  }
})
