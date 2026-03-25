import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useTransactionStore } from '../transaction'
import * as transactionApi from '@/api/transaction'

vi.mock('@/api/transaction', () => ({
  getTransactionList: vi.fn(),
  createTransaction: vi.fn(),
  getTransactionDetail: vi.fn(),
  updateTransaction: vi.fn(),
  deleteTransaction: vi.fn()
}))

describe('useTransactionStore', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useTransactionStore()
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should initialize with default values', () => {
      expect(store.transactions).toEqual([])
      expect(store.currentTransaction).toBeNull()
      expect(store.loading).toBe(false)
      expect(store.hasMore).toBe(true)
      expect(store.page).toBe(1)
      expect(store.pageSize).toBe(20)
      expect(store.filters).toEqual({
        ledgerId: null,
        categoryId: null,
        accountId: null,
        startDate: null,
        endDate: null,
        keyword: '',
        type: null,
        minAmount: null,
        maxAmount: null
      })
    })
  })

  describe('getters', () => {
    it('transactionCount should return the number of transactions', () => {
      store.transactions = [{ id: 1 }, { id: 2 }, { id: 3 }]
      expect(store.transactionCount).toBe(3)
    })

    it('groupedTransactions should group transactions by date', () => {
      store.transactions = [
        { id: 1, date: '2026-03-24T10:00:00Z' },
        { id: 2, date: '2026-03-24T11:00:00Z' },
        { id: 3, date: '2026-03-23T10:00:00Z' }
      ]
      const grouped = store.groupedTransactions
      expect(Object.keys(grouped)).toHaveLength(2)
      expect(grouped['2026-03-24']).toHaveLength(2)
      expect(grouped['2026-03-23']).toHaveLength(1)
    })

    it('groupedTransactions should be sorted by date descending', () => {
      store.transactions = [
        { id: 1, date: '2026-03-22T10:00:00Z' },
        { id: 2, date: '2026-03-24T10:00:00Z' },
        { id: 3, date: '2026-03-23T10:00:00Z' }
      ]
      const grouped = store.groupedTransactions
      const dates = Object.keys(grouped)
      expect(dates[0]).toBe('2026-03-24')
      expect(dates[1]).toBe('2026-03-23')
      expect(dates[2]).toBe('2026-03-22')
    })

    it('monthlyStats should calculate income and expense for current month', () => {
      const currentMonth = new Date().toISOString().slice(0, 7)
      store.transactions = [
        { id: 1, type: 'income', amount: 1000, date: `${currentMonth}-01` },
        { id: 2, type: 'income', amount: 500, date: `${currentMonth}-05` },
        { id: 3, type: 'expense', amount: 300, date: `${currentMonth}-10` },
        { id: 4, type: 'expense', amount: 200, date: `${currentMonth}-15` }
      ]
      const stats = store.monthlyStats
      expect(stats.income).toBe(1500)
      expect(stats.expense).toBe(500)
      expect(stats.balance).toBe(1000)
    })

    it('monthlyStats should only count current month transactions', () => {
      const currentMonth = new Date().toISOString().slice(0, 7)
      store.transactions = [
        { id: 1, type: 'income', amount: 1000, date: `${currentMonth}-01` },
        { id: 2, type: 'income', amount: 500, date: '2025-01-01' }
      ]
      const stats = store.monthlyStats
      expect(stats.income).toBe(1000)
    })
  })

  describe('actions', () => {
    describe('fetchTransactionList', () => {
      it('should fetch transaction list and update state', async () => {
        const mockData = { list: [{ id: 1, description: 'Test' }] }
        transactionApi.getTransactionList.mockResolvedValue(mockData)

        await store.fetchTransactionList()

        expect(transactionApi.getTransactionList).toHaveBeenCalled()
        expect(store.transactions).toEqual(mockData.list)
        expect(store.loading).toBe(false)
      })

      it('should append transactions when append is true', async () => {
        store.transactions = [{ id: 1 }]
        const mockData = { list: [{ id: 2 }, { id: 3 }] }
        transactionApi.getTransactionList.mockResolvedValue(mockData)

        await store.fetchTransactionList({}, true)

        expect(store.transactions).toHaveLength(3)
      })

      it('should replace transactions when append is false', async () => {
        store.transactions = [{ id: 1 }]
        const mockData = { list: [{ id: 2 }] }
        transactionApi.getTransactionList.mockResolvedValue(mockData)

        await store.fetchTransactionList({}, false)

        expect(store.transactions).toHaveLength(1)
        expect(store.transactions[0].id).toBe(2)
      })

      it('should merge filters with params', async () => {
        store.filters = { ledgerId: 1 }
        const mockData = { list: [] }
        transactionApi.getTransactionList.mockResolvedValue(mockData)

        await store.fetchTransactionList({ categoryId: 2 })

        const calledWith = transactionApi.getTransactionList.mock.calls[0][0]
        expect(calledWith.ledgerId).toBe(1)
        expect(calledWith.categoryId).toBe(2)
      })

      it('should handle error', async () => {
        transactionApi.getTransactionList.mockRejectedValue(new Error('API Error'))
        await expect(store.fetchTransactionList()).rejects.toThrow('API Error')
        expect(store.loading).toBe(false)
      })
    })

    describe('fetchCurrentTransaction', () => {
      it('should fetch current transaction and update state', async () => {
        const mockData = { id: 1, description: 'Test' }
        transactionApi.getTransactionDetail.mockResolvedValue(mockData)

        const result = await store.fetchCurrentTransaction(1)

        expect(store.currentTransaction).toEqual(mockData)
        expect(result).toEqual(mockData)
      })
    })

    describe('createTransaction', () => {
      it('should create transaction and refresh list', async () => {
        const newTx = { description: 'New Transaction', amount: 100 }
        const mockResult = { id: 1, ...newTx }
        const mockList = { list: [mockResult] }

        transactionApi.createTransaction.mockResolvedValue(mockResult)
        transactionApi.getTransactionList.mockResolvedValue(mockList)

        const result = await store.createTransaction(newTx)

        expect(transactionApi.createTransaction).toHaveBeenCalledWith(newTx)
        expect(transactionApi.getTransactionList).toHaveBeenCalled()
        expect(result).toEqual(mockResult)
      })
    })

    describe('updateTransaction', () => {
      it('should update transaction and update local state', async () => {
        store.transactions = [{ id: 1, description: 'Old', amount: 100 }]
        store.currentTransaction = { id: 1, description: 'Old', amount: 100 }
        
        const updates = { description: 'Updated' }
        transactionApi.updateTransaction.mockResolvedValue({ id: 1, ...updates })

        await store.updateTransaction(1, updates)

        expect(store.transactions[0].description).toBe('Updated')
        expect(store.currentTransaction.description).toBe('Updated')
      })
    })

    describe('deleteTransaction', () => {
      it('should delete transaction and remove from local state', async () => {
        store.transactions = [{ id: 1 }, { id: 2 }]
        store.currentTransaction = { id: 1 }

        transactionApi.deleteTransaction.mockResolvedValue()

        await store.deleteTransaction(1)

        expect(store.transactions).toHaveLength(1)
        expect(store.currentTransaction).toBeNull()
      })
    })

    describe('clearCurrentTransaction', () => {
      it('should clear current transaction', () => {
        store.currentTransaction = { id: 1 }
        store.clearCurrentTransaction()
        expect(store.currentTransaction).toBeNull()
      })
    })

    describe('setFilters', () => {
      it('should merge new filters with existing', () => {
        store.setFilters({ ledgerId: 1, keyword: 'test' })
        expect(store.filters.ledgerId).toBe(1)
        expect(store.filters.keyword).toBe('test')
      })
    })

    describe('resetFilters', () => {
      it('should reset all filters to default', () => {
        store.filters = { ledgerId: 1, keyword: 'test' }
        store.resetFilters()
        expect(store.filters.ledgerId).toBeNull()
        expect(store.filters.keyword).toBe('')
      })
    })

    describe('resetList', () => {
      it('should reset list state for reload', () => {
        store.transactions = [{ id: 1 }]
        store.page = 5
        store.hasMore = false

        store.resetList()

        expect(store.transactions).toEqual([])
        expect(store.page).toBe(1)
        expect(store.hasMore).toBe(true)
      })
    })
  })
})
