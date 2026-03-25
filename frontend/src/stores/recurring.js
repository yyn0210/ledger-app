import { defineStore } from 'pinia'
import {
  getRecurringBillList,
  createRecurringBill,
  getRecurringBillDetail,
  updateRecurringBill,
  deleteRecurringBill,
  executeRecurringBill,
  toggleRecurringBillStatus
} from '@/api/recurring'

export const useRecurringBillStore = defineStore('recurringBill', {
  state: () => ({
    recurringBillList: [],
    currentRecurringBill: null,
    loading: false,
    filterStatus: 'all', // 'all' | 'active' | 'paused' | 'completed'
    filterType: 'all' // 'all' | 'daily' | 'weekly' | 'monthly' | 'yearly'
  }),

  getters: {
    // 按状态和周期筛选后的周期账单列表
    filteredBills: state => {
      let bills = state.recurringBillList
      if (state.filterStatus !== 'all') {
        const statusMap = { active: 1, paused: 2, completed: 3 }
        bills = bills.filter(b => b.status === statusMap[state.filterStatus])
      }
      if (state.filterType !== 'all') {
        const typeMap = { daily: 1, weekly: 2, 'bi-weekly': 3, monthly: 4, quarterly: 5, yearly: 6 }
        bills = bills.filter(b => b.recurringType === typeMap[state.filterType])
      }
      return bills
    },

    // 周期账单总数
    billCount: state => state.recurringBillList.length,

    // 执行中数量
    activeCount: state => {
      return state.recurringBillList.filter(b => b.status === 1).length
    },

    // 已暂停数量
    pausedCount: state => {
      return state.recurringBillList.filter(b => b.status === 2).length
    },

    // 即将执行（7 天内）数量
    upcomingCount: state => {
      const now = new Date()
      const sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
      return state.recurringBillList.filter(b => {
        if (b.status !== 1) return false
        const nextDate = new Date(b.nextExecutionDate)
        return nextDate <= sevenDaysLater && nextDate >= now
      }).length
    }
  },

  actions: {
    /**
     * 获取周期账单列表
     * @param {Object} params - 查询参数
     */
    async fetchRecurringBillList(params = {}) {
      this.loading = true
      try {
        const data = await getRecurringBillList(params)
        this.recurringBillList = data.list || data.items || data || []
      } catch (error) {
        console.error('获取周期账单列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取周期账单详情
     * @param {number|string} id - 周期账单 ID
     */
    async fetchCurrentRecurringBill(id) {
      this.loading = true
      try {
        const data = await getRecurringBillDetail(id)
        this.currentRecurringBill = data
        return data
      } catch (error) {
        console.error('获取周期账单详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建周期账单
     * @param {Object} data - 周期账单数据
     */
    async createRecurringBill(data) {
      this.loading = true
      try {
        const result = await createRecurringBill(data)
        await this.fetchRecurringBillList()
        return result
      } catch (error) {
        console.error('创建周期账单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新周期账单
     * @param {number|string} id - 周期账单 ID
     * @param {Object} data - 周期账单数据
     */
    async updateRecurringBill(id, data) {
      this.loading = true
      try {
        const result = await updateRecurringBill(id, data)
        // 更新本地状态
        const index = this.recurringBillList.findIndex(b => b.id === id)
        if (index !== -1) {
          this.recurringBillList[index] = { ...this.recurringBillList[index], ...data }
        }
        if (this.currentRecurringBill?.id === id) {
          this.currentRecurringBill = { ...this.currentRecurringBill, ...data }
        }
        return result
      } catch (error) {
        console.error('更新周期账单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除周期账单
     * @param {number|string} id - 周期账单 ID
     */
    async deleteRecurringBill(id) {
      this.loading = true
      try {
        await deleteRecurringBill(id)
        // 从本地状态移除
        this.recurringBillList = this.recurringBillList.filter(b => b.id !== id)
        if (this.currentRecurringBill?.id === id) {
          this.currentRecurringBill = null
        }
      } catch (error) {
        console.error('删除周期账单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 手动执行周期账单
     * @param {number|string} id - 周期账单 ID
     */
    async executeRecurringBill(id) {
      this.loading = true
      try {
        const result = await executeRecurringBill(id)
        // 更新执行信息
        const index = this.recurringBillList.findIndex(b => b.id === id)
        if (index !== -1) {
          this.recurringBillList[index].executionCount = (this.recurringBillList[index].executionCount || 0) + 1
        }
        return result
      } catch (error) {
        console.error('执行周期账单失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 暂停/恢复周期账单
     * @param {number|string} id - 周期账单 ID
     * @param {boolean} pause - true=暂停，false=恢复
     */
    async toggleRecurringBillStatus(id, pause) {
      this.loading = true
      try {
        const result = await toggleRecurringBillStatus(id, pause)
        // 更新本地状态
        const index = this.recurringBillList.findIndex(b => b.id === id)
        if (index !== -1) {
          this.recurringBillList[index].status = pause ? 2 : 1
        }
        return result
      } catch (error) {
        console.error('切换周期账单状态失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前周期账单
     */
    clearCurrentRecurringBill() {
      this.currentRecurringBill = null
    },

    /**
     * 设置筛选状态
     * @param {'all'|'active'|'paused'|'completed'} status
     */
    setFilterStatus(status) {
      this.filterStatus = status
    },

    /**
     * 设置筛选周期类型
     * @param {'all'|'daily'|'weekly'|'monthly'|'yearly'} type
     */
    setFilterType(type) {
      this.filterType = type
    },

    /**
     * 重置筛选
     */
    resetFilters() {
      this.filterStatus = 'all'
      this.filterType = 'all'
    }
  }
})
