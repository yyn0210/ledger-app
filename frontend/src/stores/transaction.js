import { defineStore } from 'pinia'
import {
  getTransactionList,
  createTransaction,
  getTransactionDetail,
  updateTransaction,
  deleteTransaction
} from '@/api/transaction'

export const useTransactionStore = defineStore('transaction', {
  state: () => ({
    transactions: [],
    currentTransaction: null,
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 20,
    filters: {
      ledgerId: null,
      categoryId: null,
      accountId: null,
      startDate: null,
      endDate: null,
      keyword: '',
      type: null,
      minAmount: null,
      maxAmount: null
    }
  }),
  getters: {
    // 交易总数
    transactionCount: state => state.transactions.length,
    // 按日期分组的交易
    groupedTransactions: state => {
      const groups = {}
      state.transactions.forEach(tx => {
        const date = tx.date.split('T')[0]
        if (!groups[date]) {
          groups[date] = []
        }
        groups[date].push(tx)
      })
      // 按日期倒序排列
      return Object.entries(groups)
        .sort((a, b) => new Date(b[0]) - new Date(a[0]))
        .reduce((acc, [date, items]) => {
          acc[date] = items
          return acc
        }, {})
    },
    // 月度统计
    monthlyStats: state => {
      const now = new Date()
      const currentMonth = now.toISOString().slice(0, 7) // YYYY-MM
      const transactions = state.transactions.filter(tx => 
        tx.date.startsWith(currentMonth)
      )
      
      const income = transactions
        .filter(tx => tx.type === 'income')
        .reduce((sum, tx) => sum + tx.amount, 0)
      
      const expense = transactions
        .filter(tx => tx.type === 'expense')
        .reduce((sum, tx) => sum + tx.amount, 0)
      
      return { income, expense, balance: income - expense }
    }
  },
  actions: {
    /**
     * 获取交易列表
     * @param {Object} params - 查询参数
     * @param {boolean} append - 是否追加（分页用）
     */
    async fetchTransactionList(params = {}, append = false) {
      this.loading = true
      try {
        const queryParams = {
          page: params.page || this.page,
          pageSize: params.pageSize || this.pageSize,
          ...this.filters,
          ...params
        }
        
        const data = await getTransactionList(queryParams)
        
        if (append) {
          this.transactions = [...this.transactions, ...(data.list || data.items || data)]
        } else {
          this.transactions = data.list || data.items || data
        }
        
        this.hasMore = data.hasMore !== false
        this.page = queryParams.page
        
        return data
      } catch (error) {
        console.error('获取交易列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取交易详情
     * @param {number|string} id - 交易 ID
     */
    async fetchCurrentTransaction(id) {
      this.loading = true
      try {
        const data = await getTransactionDetail(id)
        this.currentTransaction = data
        return data
      } catch (error) {
        console.error('获取交易详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建交易
     * @param {Object} data - 交易数据
     */
    async createTransaction(data) {
      this.loading = true
      try {
        const result = await createTransaction(data)
        // 创建成功后刷新列表
        await this.fetchTransactionList({}, false)
        return result
      } catch (error) {
        console.error('创建交易失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新交易
     * @param {number|string} id - 交易 ID
     * @param {Object} data - 交易数据
     */
    async updateTransaction(id, data) {
      this.loading = true
      try {
        const result = await updateTransaction(id, data)
        // 更新本地状态
        const index = this.transactions.findIndex(tx => tx.id === id)
        if (index !== -1) {
          this.transactions[index] = { ...this.transactions[index], ...data }
        }
        if (this.currentTransaction?.id === id) {
          this.currentTransaction = { ...this.currentTransaction, ...data }
        }
        return result
      } catch (error) {
        console.error('更新交易失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除交易
     * @param {number|string} id - 交易 ID
     */
    async deleteTransaction(id) {
      this.loading = true
      try {
        await deleteTransaction(id)
        // 从本地状态移除
        this.transactions = this.transactions.filter(tx => tx.id !== id)
        if (this.currentTransaction?.id === id) {
          this.currentTransaction = null
        }
      } catch (error) {
        console.error('删除交易失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前交易
     */
    clearCurrentTransaction() {
      this.currentTransaction = null
    },

    /**
     * 设置筛选条件
     * @param {Object} filters - 筛选条件
     */
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },

    /**
     * 重置筛选条件
     */
    resetFilters() {
      this.filters = {
        ledgerId: null,
        categoryId: null,
        accountId: null,
        startDate: null,
        endDate: null,
        keyword: '',
        type: null,
        minAmount: null,
        maxAmount: null
      }
    },

    /**
     * 重置列表状态（用于重新加载）
     */
    resetList() {
      this.transactions = []
      this.page = 1
      this.hasMore = true
    }
  }
})
