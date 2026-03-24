import { defineStore } from 'pinia'
import { getTransactionList, createTransaction, updateTransaction, deleteTransaction } from '@/api/transaction'

export const useTransactionStore = defineStore('transaction', {
  state: () => ({
    transactions: [],
    loading: false,
    filter: {
      dateStart: null,
      dateEnd: null,
      bookId: null,
      categoryId: null,
      accountId: null,
      type: null,
      amountMin: null,
      amountMax: null
    },
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0
    }
  }),
  getters: {
    groupedTransactions: state => {
      // Group transactions by date
      const groups = {}
      state.transactions.forEach(transaction => {
        const date = transaction.date
        if (!groups[date]) {
          groups[date] = []
        }
        groups[date].push(transaction)
      })
      return groups
    },
    totalIncome: state => {
      return state.transactions
        .filter(t => t.type === 'income')
        .reduce((sum, t) => sum + (t.amount || 0), 0)
    },
    totalExpense: state => {
      return state.transactions
        .filter(t => t.type === 'expense')
        .reduce((sum, t) => sum + (t.amount || 0), 0)
    },
    balance: state => {
      return state.transactions.reduce((sum, t) => {
        return t.type === 'income' ? sum + (t.amount || 0) : sum - (t.amount || 0)
      }, 0)
    }
  },
  actions: {
    async fetchTransactions(params = {}) {
      this.loading = true
      try {
        const data = await getTransactionList({ ...this.pagination, ...this.filter, ...params })
        this.transactions = data.list || data || []
        this.pagination.total = data.total || this.transactions.length
      } catch (error) {
        console.error('获取交易列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    async addTransaction(transactionData) {
      const data = await createTransaction(transactionData)
      this.transactions.unshift(data)
      return data
    },
    async updateTransactionData(id, updates) {
      const data = await updateTransaction(id, updates)
      const index = this.transactions.findIndex(t => t.id === id)
      if (index > -1) {
        this.transactions[index] = data
      }
      return data
    },
    async removeTransaction(id) {
      await deleteTransaction(id)
      this.transactions = this.transactions.filter(t => t.id !== id)
    },
    setFilter(filter) {
      this.filter = { ...this.filter, ...filter }
    },
    resetFilter() {
      this.filter = {
        dateStart: null,
        dateEnd: null,
        bookId: null,
        categoryId: null,
        accountId: null,
        type: null,
        amountMin: null,
        amountMax: null
      }
    },
    setPage(page) {
      this.pagination.page = page
    }
  }
})
