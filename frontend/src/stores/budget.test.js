import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useBudgetStore } from '@/stores/budget'

describe('Budget Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with default state', () => {
    const store = useBudgetStore()
    expect(store.list).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.pagination).toEqual({
      page: 1,
      pageSize: 20,
      total: 0
    })
  })

  it('should have correct getters', () => {
    const store = useBudgetStore()
    store.list = [
      { id: 1, name: '餐饮', amount: 3000, spent: 2500, status: 'active' },
      { id: 2, name: '交通', amount: 500, spent: 600, status: 'active' },
      { id: 3, name: '购物', amount: 2000, spent: 1000, status: 'paused' }
    ]

    expect(store.activeBudgets).toHaveLength(2)
    expect(store.overspentBudgets).toHaveLength(1)
    expect(store.totalBudget).toBe(5500)
    expect(store.totalSpent).toBe(4100)
  })

  it('should reset state', () => {
    const store = useBudgetStore()
    store.list = [{ id: 1, name: 'Test' }]
    store.loading = true
    store.error = 'Test error'

    store.reset()

    expect(store.list).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })
})
