import { defineStore } from 'pinia'
import {
  getAccountList,
  createAccount,
  getAccountDetail,
  updateAccount,
  deleteAccount
} from '@/api/account'

export const useAccountStore = defineStore('account', {
  state: () => ({
    accountList: [],
    currentAccount: null,
    loading: false,
    filterType: 'all',
    hideBalance: false // 全局余额隐藏状态
  }),
  getters: {
    // 按类型筛选后的账户列表
    filteredAccounts: state => {
      if (state.filterType === 'all') return state.accountList
      return state.accountList.filter(acc => acc.type === state.filterType)
    },
    // 账户总数
    accountCount: state => state.accountList.length,
    // 总资产（不隐藏时计算）
    totalAssets: state => {
      if (state.hideBalance) return 0
      return state.accountList
        .filter(acc => acc.type !== 'credit')
        .reduce((sum, acc) => sum + (acc.balance || 0), 0)
    },
    // 总负债（信用卡）
    totalLiabilities: state => {
      if (state.hideBalance) return 0
      return state.accountList
        .filter(acc => acc.type === 'credit')
        .reduce((sum, acc) => sum + Math.abs(acc.balance || 0), 0)
    },
    // 净资产
    netAssets: state => {
      if (state.hideBalance) return 0
      return state.totalAssets - state.totalLiabilities
    }
  },
  actions: {
    /**
     * 获取账户列表
     * @param {Object} params - 查询参数
     */
    async fetchAccountList(params = {}) {
      this.loading = true
      try {
        const data = await getAccountList(params)
        this.accountList = data.list || data.items || data
        if (params.type) {
          this.filterType = params.type
        }
      } catch (error) {
        console.error('获取账户列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取账户详情
     * @param {number|string} id - 账户 ID
     */
    async fetchCurrentAccount(id) {
      this.loading = true
      try {
        const data = await getAccountDetail(id)
        this.currentAccount = data
        return data
      } catch (error) {
        console.error('获取账户详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建账户
     * @param {Object} data - 账户数据
     */
    async createAccount(data) {
      this.loading = true
      try {
        const result = await createAccount(data)
        await this.fetchAccountList()
        return result
      } catch (error) {
        console.error('创建账户失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新账户
     * @param {number|string} id - 账户 ID
     * @param {Object} data - 账户数据
     */
    async updateAccount(id, data) {
      this.loading = true
      try {
        const result = await updateAccount(id, data)
        // 更新本地状态
        const index = this.accountList.findIndex(acc => acc.id === id)
        if (index !== -1) {
          this.accountList[index] = { ...this.accountList[index], ...data }
        }
        if (this.currentAccount?.id === id) {
          this.currentAccount = { ...this.currentAccount, ...data }
        }
        return result
      } catch (error) {
        console.error('更新账户失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除账户
     * @param {number|string} id - 账户 ID
     */
    async deleteAccount(id) {
      this.loading = true
      try {
        await deleteAccount(id)
        // 从本地状态移除
        this.accountList = this.accountList.filter(acc => acc.id !== id)
        if (this.currentAccount?.id === id) {
          this.currentAccount = null
        }
      } catch (error) {
        console.error('删除账户失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空当前账户
     */
    clearCurrentAccount() {
      this.currentAccount = null
    },

    /**
     * 设置筛选类型
     * @param {'all'|'cash'|'bank'|'credit'|'alipay'|'wechat'|'other'} type
     */
    setFilterType(type) {
      this.filterType = type
    },

    /**
     * 切换余额显示状态
     */
    toggleHideBalance() {
      this.hideBalance = !this.hideBalance
    },

    /**
     * 设置余额显示状态
     * @param {boolean} hide
     */
    setHideBalance(hide) {
      this.hideBalance = hide
    }
  }
})
