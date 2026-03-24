import { defineStore } from 'pinia'
import { getStatistics, getTrendData, getCategoryStats, getMonthlyComparison } from '@/api/statistics'

export const useStatisticsStore = defineStore('statistics', {
  state: () => ({
    overview: {
      totalIncome: 0,
      totalExpense: 0,
      totalBalance: 0,
      transactionCount: 0,
      avgDailyExpense: 0
    },
    trendData: {
      dates: [],
      income: [],
      expense: []
    },
    categoryData: {
      expense: [],
      income: []
    },
    monthlyData: [],
    loading: false,
    filter: {
      startDate: null,
      endDate: null,
      range: 'month' // 'week' | 'month' | 'year' | 'custom'
    }
  }),
  getters: {
    incomeExpenseRatio: state => {
      if (state.overview.totalIncome === 0) return 0
      return ((state.overview.totalExpense / state.overview.totalIncome) * 100).toFixed(1)
    },
    topCategories: state => {
      return [...state.categoryData.expense]
        .sort((a, b) => b.value - a.value)
        .slice(0, 5)
    }
  },
  actions: {
    async fetchOverview(params = {}) {
      this.loading = true
      try {
        const data = await getStatistics({ ...this.filter, ...params })
        this.overview = data.overview || this.overview
      } catch (error) {
        console.error('获取统计概览失败:', error)
      } finally {
        this.loading = false
      }
    },
    async fetchTrendData(params = {}) {
      try {
        const data = await getTrendData({ ...this.filter, ...params })
        this.trendData = data || this.trendData
      } catch (error) {
        console.error('获取趋势数据失败:', error)
      }
    },
    async fetchCategoryStats(params = {}) {
      try {
        const data = await getCategoryStats({ ...this.filter, ...params })
        this.categoryData = data || this.categoryData
      } catch (error) {
        console.error('获取分类统计失败:', error)
      }
    },
    async fetchMonthlyComparison(params = {}) {
      try {
        const data = await getMonthlyComparison({ ...this.filter, ...params })
        this.monthlyData = data || this.monthlyData
      } catch (error) {
        console.error('获取月度对比失败:', error)
      }
    },
    async fetchAll(params = {}) {
      await Promise.all([
        this.fetchOverview(params),
        this.fetchTrendData(params),
        this.fetchCategoryStats(params),
        this.fetchMonthlyComparison(params)
      ])
    },
    setFilter(filter) {
      this.filter = { ...this.filter, ...filter }
    },
    setRange(range, startDate, endDate) {
      this.filter.range = range
      if (startDate) this.filter.startDate = startDate
      if (endDate) this.filter.endDate = endDate
    }
  }
})
