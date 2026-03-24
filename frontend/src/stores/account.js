import { defineStore } from 'pinia'
import { getAccountList } from '@/api/account'

export const useAccountStore = defineStore('account', {
  state: () => ({
    accounts: [],
    loading: false,
    hideBalance: false
  }),
  getters: {
    accountCount: state => state.accounts.length,
    totalBalance: state => state.accounts.reduce((sum, acc) => {
      return acc.type === 'credit' ? sum : sum + (acc.balance || 0)
    }, 0),
    totalCredit: state => state.accounts.reduce((sum, acc) => {
      return acc.type === 'credit' ? sum + Math.abs(acc.balance || 0) : sum
    }, 0),
    netAssets: state => state.accounts.reduce((sum, acc) => {
      return sum + (acc.balance || 0)
    }, 0)
  },
  actions: {
    async fetchAccounts() {
      this.loading = true
      try {
        const data = await getAccountList()
        this.accounts = data.list || data || []
      } catch (error) {
        console.error('获取账户列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    addAccount(account) {
      this.accounts.push(account)
    },
    updateAccount(id, updates) {
      const index = this.accounts.findIndex(a => a.id === id)
      if (index > -1) {
        this.accounts[index] = { ...this.accounts[index], ...updates }
      }
    },
    removeAccount(id) {
      this.accounts = this.accounts.filter(a => a.id !== id)
    },
    toggleHideBalance() {
      this.hideBalance = !this.hideBalance
      localStorage.setItem('hideBalance', this.hideBalance)
    },
    initHideBalance() {
      const saved = localStorage.getItem('hideBalance')
      if (saved) {
        this.hideBalance = saved === 'true'
      }
    }
  }
})
