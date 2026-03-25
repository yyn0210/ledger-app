import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useBudgetStore } from '../budget'
import * as budgetApi from '@/api/budget'

// Mock API 模块
vi.mock('@/api/budget', () => ({
  getBudgetList: vi.fn(),
  createBudget: vi.fn(),
  getBudgetDetail: vi.fn(),
  getBudgetExecution: vi.fn(),
  updateBudget: vi.fn(),
  deleteBudget: vi.fn()
}))

describe('useBudgetStore', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useBudgetStore()
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should initialize with default values', () => {
      expect(store.budgetList).toEqual([])
      expect(store.currentBudget).toBeNull()
      expect(store.executionData).toBeNull()
      expect(store.loading).toBe(false)
      expect(store.filterType).toBe('all')
      expect(store.filterPeriod).toBe('all')
    })
  })

  describe('getters', () => {
    it('filteredBudgets should return all budgets when filters are all', () => {
      store.budgetList = [
        { id: 1, type: 'category', period: 'month' },
        { id: 2, type: 'account', period: 'week' }
      ]
      expect(store.filteredBudgets).toHaveLength(2)
    })

    it('filteredBudgets should filter by type', () => {
      store.budgetList = [
        { id: 1, type: 'category', period: 'month' },
        { id: 2, type: 'account', period: 'week' },
        { id: 3, type: 'category', period: 'year' }
      ]
      store.filterType = 'category'
      expect(store.filteredBudgets).toHaveLength(2)
      expect(store.filteredBudgets.every(b => b.type === 'category')).toBe(true)
    })

    it('filteredBudgets should filter by period', () => {
      store.budgetList = [
        { id: 1, type: 'category', period: 'month' },
        { id: 2, type: 'account', period: 'week' },
        { id: 3, type: 'category', period: 'month' }
      ]
      store.filterPeriod = 'month'
      expect(store.filteredBudgets).toHaveLength(2)
      expect(store.filteredBudgets.every(b => b.period === 'month')).toBe(true)
    })

    it('budgetCount should return the number of budgets', () => {
      store.budgetList = [{ id: 1 }, { id: 2 }, { id: 3 }]
      expect(store.budgetCount).toBe(3)
    })

    it('overBudgetCount should count budgets with progress > 100', () => {
      store.budgetList = [
        { id: 1, progress: 80 },
        { id: 2, progress: 120 },
        { id: 3, progress: 150 }
      ]
      expect(store.overBudgetCount).toBe(2)
    })

    it('warningCount should count budgets with progress between 90 and 100', () => {
      store.budgetList = [
        { id: 1, progress: 80 },
        { id: 2, progress: 90 },
        { id: 3, progress: 95 },
        { id: 4, progress: 100 },
        { id: 5, progress: 110 }
      ]
      expect(store.warningCount).toBe(3)
    })
  })

  describe('actions', () => {
    describe('fetchBudgetList', () => {
      it('should fetch budget list and update state', async () => {
        const mockData = { list: [{ id: 1, name: 'Test Budget' }] }
        budgetApi.getBudgetList.mockResolvedValue(mockData)

        await store.fetchBudgetList()

        expect(budgetApi.getBudgetList).toHaveBeenCalledWith({})
        expect(store.budgetList).toEqual(mockData.list)
        expect(store.loading).toBe(false)
      })

      it('should handle items field in response', async () => {
        const mockData = { items: [{ id: 1, name: 'Test' }] }
        budgetApi.getBudgetList.mockResolvedValue(mockData)

        await store.fetchBudgetList()

        expect(store.budgetList).toEqual(mockData.items)
      })

      it('should handle array response', async () => {
        const mockData = [{ id: 1, name: 'Test' }]
        budgetApi.getBudgetList.mockResolvedValue(mockData)

        await store.fetchBudgetList()

        expect(store.budgetList).toEqual(mockData)
      })

      it('should set loading to false on error', async () => {
        budgetApi.getBudgetList.mockRejectedValue(new Error('API Error'))

        await expect(store.fetchBudgetList()).rejects.toThrow('API Error')
        expect(store.loading).toBe(false)
      })
    })

    describe('fetchCurrentBudget', () => {
      it('should fetch current budget and update state', async () => {
        const mockData = { id: 1, name: 'Test Budget' }
        budgetApi.getBudgetDetail.mockResolvedValue(mockData)

        const result = await store.fetchCurrentBudget(1)

        expect(budgetApi.getBudgetDetail).toHaveBeenCalledWith(1)
        expect(store.currentBudget).toEqual(mockData)
        expect(result).toEqual(mockData)
        expect(store.loading).toBe(false)
      })

      it('should handle error', async () => {
        budgetApi.getBudgetDetail.mockRejectedValue(new Error('Not found'))

        await expect(store.fetchCurrentBudget(1)).rejects.toThrow('Not found')
        expect(store.loading).toBe(false)
      })
    })

    describe('fetchExecutionData', () => {
      it('should fetch execution data and update state', async () => {
        const mockData = { spent: 500, budget: 1000, progress: 50 }
        budgetApi.getBudgetExecution.mockResolvedValue(mockData)

        const result = await store.fetchExecutionData(1)

        expect(budgetApi.getBudgetExecution).toHaveBeenCalledWith(1)
        expect(store.executionData).toEqual(mockData)
        expect(result).toEqual(mockData)
      })
    })

    describe('createBudget', () => {
      it('should create budget and refresh list', async () => {
        const newBudget = { name: 'New Budget', amount: 1000 }
        const mockResult = { id: 1, ...newBudget }
        const mockList = { list: [mockResult] }

        budgetApi.createBudget.mockResolvedValue(mockResult)
        budgetApi.getBudgetList.mockResolvedValue(mockList)

        const result = await store.createBudget(newBudget)

        expect(budgetApi.createBudget).toHaveBeenCalledWith(newBudget)
        expect(budgetApi.getBudgetList).toHaveBeenCalled()
        expect(store.budgetList).toEqual(mockList.list)
        expect(result).toEqual(mockResult)
      })
    })

    describe('updateBudget', () => {
      it('should update budget and update local state', async () => {
        store.budgetList = [{ id: 1, name: 'Old Name', amount: 1000 }]
        store.currentBudget = { id: 1, name: 'Old Name', amount: 1000 }
        
        const updates = { name: 'New Name' }
        const mockResult = { id: 1, ...updates }

        budgetApi.updateBudget.mockResolvedValue(mockResult)

        await store.updateBudget(1, updates)

        expect(budgetApi.updateBudget).toHaveBeenCalledWith(1, updates)
        expect(store.budgetList[0].name).toBe('New Name')
        expect(store.currentBudget.name).toBe('New Name')
      })

      it('should handle non-existent budget', async () => {
        budgetApi.updateBudget.mockResolvedValue({ id: 999 })

        await store.updateBudget(999, { name: 'Test' })

        expect(store.budgetList).toEqual([])
      })
    })

    describe('deleteBudget', () => {
      it('should delete budget and remove from local state', async () => {
        store.budgetList = [{ id: 1, name: 'Test' }, { id: 2, name: 'Test2' }]
        store.currentBudget = { id: 1, name: 'Test' }

        budgetApi.deleteBudget.mockResolvedValue()

        await store.deleteBudget(1)

        expect(budgetApi.deleteBudget).toHaveBeenCalledWith(1)
        expect(store.budgetList).toHaveLength(1)
        expect(store.budgetList[0].id).toBe(2)
        expect(store.currentBudget).toBeNull()
      })
    })

    describe('clearCurrentBudget', () => {
      it('should clear current budget and execution data', () => {
        store.currentBudget = { id: 1 }
        store.executionData = { spent: 500 }

        store.clearCurrentBudget()

        expect(store.currentBudget).toBeNull()
        expect(store.executionData).toBeNull()
      })
    })

    describe('setFilterType', () => {
      it('should set filter type', () => {
        store.setFilterType('category')
        expect(store.filterType).toBe('category')
      })
    })

    describe('setFilterPeriod', () => {
      it('should set filter period', () => {
        store.setFilterPeriod('month')
        expect(store.filterPeriod).toBe('month')
      })
    })

    describe('resetFilters', () => {
      it('should reset all filters to default', () => {
        store.filterType = 'category'
        store.filterPeriod = 'month'

        store.resetFilters()

        expect(store.filterType).toBe('all')
        expect(store.filterPeriod).toBe('all')
      })
    })
  })
})
