import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRecurringBillStore } from '../recurring'
import * as api from '@/api/recurring'

// Mock API
vi.mock('@/api/recurring', () => ({
  getRecurringBillList: vi.fn(),
  createRecurringBill: vi.fn(),
  getRecurringBillDetail: vi.fn(),
  updateRecurringBill: vi.fn(),
  deleteRecurringBill: vi.fn(),
  executeRecurringBill: vi.fn(),
  toggleRecurringBillStatus: vi.fn()
}))

describe('useRecurringBillStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mockBill = {
    id: 1,
    name: '房租',
    recurringType: 4,
    amount: 3000,
    status: 1,
    nextExecutionDate: '2026-04-01'
  }

  describe('state', () => {
    it('should initialize with default values', () => {
      const store = useRecurringBillStore()
      expect(store.recurringBillList).toEqual([])
      expect(store.currentRecurringBill).toBeNull()
      expect(store.loading).toBe(false)
      expect(store.filterStatus).toBe('all')
      expect(store.filterType).toBe('all')
    })
  })

  describe('getters', () => {
    it('should return filtered bills by status', () => {
      const store = useRecurringBillStore()
      store.recurringBillList = [
        { id: 1, status: 1 },
        { id: 2, status: 2 },
        { id: 3, status: 1 }
      ]
      store.filterStatus = 'active'
      
      expect(store.filteredBills.length).toBe(2)
      expect(store.filteredBills.every(b => b.status === 1)).toBe(true)
    })

    it('should return bill count', () => {
      const store = useRecurringBillStore()
      store.recurringBillList = [
        { id: 1 },
        { id: 2 },
        { id: 3 }
      ]
      expect(store.billCount).toBe(3)
    })

    it('should return active count', () => {
      const store = useRecurringBillStore()
      store.recurringBillList = [
        { id: 1, status: 1 },
        { id: 2, status: 2 },
        { id: 3, status: 1 }
      ]
      expect(store.activeCount).toBe(2)
    })

    it('should return paused count', () => {
      const store = useRecurringBillStore()
      store.recurringBillList = [
        { id: 1, status: 1 },
        { id: 2, status: 2 },
        { id: 3, status: 2 }
      ]
      expect(store.pausedCount).toBe(2)
    })
  })

  describe('actions', () => {
    describe('fetchRecurringBillList', () => {
      it('should fetch and set bill list', async () => {
        const store = useRecurringBillStore()
        const mockData = { list: [mockBill] }
        vi.mocked(api.getRecurringBillList).mockResolvedValue(mockData)

        await store.fetchRecurringBillList()

        expect(api.getRecurringBillList).toHaveBeenCalled()
        expect(store.recurringBillList).toEqual([mockBill])
        expect(store.loading).toBe(false)
      })

      it('should handle error', async () => {
        const store = useRecurringBillStore()
        vi.mocked(api.getRecurringBillList).mockRejectedValue(new Error('API Error'))

        await expect(store.fetchRecurringBillList()).rejects.toThrow('API Error')
        expect(store.loading).toBe(false)
      })
    })

    describe('fetchCurrentRecurringBill', () => {
      it('should fetch and set current bill', async () => {
        const store = useRecurringBillStore()
        vi.mocked(api.getRecurringBillDetail).mockResolvedValue(mockBill)

        const result = await store.fetchCurrentRecurringBill(1)

        expect(api.getRecurringBillDetail).toHaveBeenCalledWith(1)
        expect(store.currentRecurringBill).toEqual(mockBill)
        expect(result).toEqual(mockBill)
      })

      it('should handle error', async () => {
        const store = useRecurringBillStore()
        vi.mocked(api.getRecurringBillDetail).mockRejectedValue(new Error('Not found'))

        await expect(store.fetchCurrentRecurringBill(1)).rejects.toThrow('Not found')
      })
    })

    describe('createRecurringBill', () => {
      it('should create bill and refresh list', async () => {
        const store = useRecurringBillStore()
        const newBill = { name: '新账单', amount: 1000 }
        vi.mocked(api.createRecurringBill).mockResolvedValue({ id: 1 })
        vi.mocked(api.getRecurringBillList).mockResolvedValue({ list: [mockBill] })

        await store.createRecurringBill(newBill)

        expect(api.createRecurringBill).toHaveBeenCalledWith(newBill)
        expect(store.recurringBillList.length).toBe(1)
      })
    })

    describe('updateRecurringBill', () => {
      it('should update bill and update local state', async () => {
        const store = useRecurringBillStore()
        store.recurringBillList = [mockBill]
        const updatedData = { name: '更新后的名称' }
        vi.mocked(api.updateRecurringBill).mockResolvedValue({})

        await store.updateRecurringBill(1, updatedData)

        expect(api.updateRecurringBill).toHaveBeenCalledWith(1, updatedData)
        expect(store.recurringBillList[0].name).toBe('更新后的名称')
      })
    })

    describe('deleteRecurringBill', () => {
      it('should delete bill and remove from list', async () => {
        const store = useRecurringBillStore()
        store.recurringBillList = [mockBill]
        vi.mocked(api.deleteRecurringBill).mockResolvedValue({})

        await store.deleteRecurringBill(1)

        expect(api.deleteRecurringBill).toHaveBeenCalledWith(1)
        expect(store.recurringBillList.length).toBe(0)
      })
    })

    describe('executeRecurringBill', () => {
      it('should execute bill and increment count', async () => {
        const store = useRecurringBillStore()
        store.recurringBillList = [{ ...mockBill, executionCount: 2 }]
        vi.mocked(api.executeRecurringBill).mockResolvedValue({})

        await store.executeRecurringBill(1)

        expect(api.executeRecurringBill).toHaveBeenCalledWith(1)
        expect(store.recurringBillList[0].executionCount).toBe(3)
      })
    })

    describe('toggleRecurringBillStatus', () => {
      it('should pause bill', async () => {
        const store = useRecurringBillStore()
        store.recurringBillList = [{ ...mockBill, status: 1 }]
        vi.mocked(api.toggleRecurringBillStatus).mockResolvedValue({})

        await store.toggleRecurringBillStatus(1, true)

        expect(api.toggleRecurringBillStatus).toHaveBeenCalledWith(1, true)
        expect(store.recurringBillList[0].status).toBe(2)
      })

      it('should resume bill', async () => {
        const store = useRecurringBillStore()
        store.recurringBillList = [{ ...mockBill, status: 2 }]
        vi.mocked(api.toggleRecurringBillStatus).mockResolvedValue({})

        await store.toggleRecurringBillStatus(1, false)

        expect(api.toggleRecurringBillStatus).toHaveBeenCalledWith(1, false)
        expect(store.recurringBillList[0].status).toBe(1)
      })
    })

    describe('setFilterStatus', () => {
      it('should set filter status', () => {
        const store = useRecurringBillStore()
        store.setFilterStatus('active')
        expect(store.filterStatus).toBe('active')
      })
    })

    describe('setFilterType', () => {
      it('should set filter type', () => {
        const store = useRecurringBillStore()
        store.setFilterType('monthly')
        expect(store.filterType).toBe('monthly')
      })
    })

    describe('resetFilters', () => {
      it('should reset all filters', () => {
        const store = useRecurringBillStore()
        store.filterStatus = 'active'
        store.filterType = 'monthly'
        
        store.resetFilters()
        
        expect(store.filterStatus).toBe('all')
        expect(store.filterType).toBe('all')
      })
    })
  })
})
