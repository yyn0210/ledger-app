import { defineStore } from 'pinia'
import {
  getStatisticsOverview,
  getTrendData,
  getCategoryStats,
  getAccountStats,
  getRankings,
  getBudgetExecution
} from '@/api/statistics'

export const useStatisticsStore = defineStore('statistics', {
  state: () => ({
    overview: null,
    trendData: [],
    categoryData: [],
    accountData: [],
    rankings: {
      category: [],
      day: [],
      single: []
    },
    budgetExecution: [],
    loading: false,
    dateRange: {
      startDate: null,
      endDate: null
    }
  }),
  getters: {
    // 本月统计
    currentMonthStats: state => {
      if (!state.overview) return null
      return {
        income: state.overview.income || 0,
        expense: state.overview.expense || 0,
        balance: (state.overview.income || 0) - (state.overview.expense || 0)
      }
    },
    // 环比（与上月相比）
    monthOverMonth: state => {
      if (!state.overview) return null
      return {
        incomeChange: state.overview.incomeChange || 0,
        expenseChange: state.overview.expenseChange || 0
      }
    },
    // 分类占比（饼图数据）
    categoryPieData: state => {
      return state.categoryData.map(item => ({
        name: item.categoryName,
        value: item.amount,
        color: item.color
      }))
    },
    // 支出 TOP 分类
    topCategories: state => {
      return state.rankings.category?.slice(0, 5) || []
    },
    // 支出 TOP 日期
    topDays: state => {
      return state.rankings.day?.slice(0, 5) || []
    },
    // 单笔最大支出
    maxSingleExpense: state => {
      return state.rankings.single?.[0] || null
    }
  },
  actions: {
    /**
     * 设置日期范围
     */
    setDateRange(startDate, endDate) {
      this.dateRange = { startDate, endDate }
    },

    /**
     * 获取统计概览
     * @param {Object} params - 查询参数
     */
    async fetchOverview(params = {}) {
      this.loading = true
      try {
        const data = await getStatisticsOverview({
          ...this.dateRange,
          ...params
        })
        this.overview = data
        return data
      } catch (error) {
        console.error('获取概览数据失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取趋势数据
     * @param {Object} params - 查询参数
     */
    async fetchTrendData(params = {}) {
      this.loading = true
      try {
        const data = await getTrendData({
          ...this.dateRange,
          ...params
        })
        this.trendData = data.list || data.items || data
        return this.trendData
      } catch (error) {
        console.error('获取趋势数据失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取分类统计
     * @param {Object} params - 查询参数
     */
    async fetchCategoryStats(params = {}) {
      this.loading = true
      try {
        const data = await getCategoryStats({
          ...this.dateRange,
          ...params
        })
        this.categoryData = data.list || data.items || data
        return this.categoryData
      } catch (error) {
        console.error('获取分类统计失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取账户统计
     * @param {Object} params - 查询参数
     */
    async fetchAccountStats(params = {}) {
      this.loading = true
      try {
        const data = await getAccountStats({
          ...this.dateRange,
          ...params
        })
        this.accountData = data.list || data.items || data
        return this.accountData
      } catch (error) {
        console.error('获取账户统计失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取排行榜
     * @param {Object} params - 查询参数
     */
    async fetchRankings(params = {}) {
      this.loading = true
      try {
        const data = await getRankings({
          ...this.dateRange,
          ...params
        })
        this.rankings = data
        return data
      } catch (error) {
        console.error('获取排行榜失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取预算执行数据
     * @param {Object} params - 查询参数
     */
    async fetchBudgetExecution(params = {}) {
      this.loading = true
      try {
        const data = await getBudgetExecution({
          ...this.dateRange,
          ...params
        })
        this.budgetExecution = data
        return data
      } catch (error) {
        console.error('获取预算执行数据失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 刷新所有统计数据
     */
    async refreshAll(params = {}) {
      try {
        await Promise.all([
          this.fetchOverview(params),
          this.fetchTrendData(params),
          this.fetchCategoryStats(params),
          this.fetchRankings(params)
        ])
      } catch (error) {
        console.error('刷新统计数据失败:', error)
      }
    }
  }
})
