import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRecurringStore } from '@/stores/recurring'

describe('Recurring Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with default state', () => {
    const store = useRecurringStore()
    expect(store.list).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('should have correct getters', () => {
    const store = useRecurringStore()
    store.list = [
      { id: 1, name: '房租', status: 'active', nextDate: Date.now() + 86400000 },
      { id: 2, name: '工资', status: 'active', nextDate: Date.now() + 86400000 * 10 },
      { id: 3, name: '订阅', status: 'paused', nextDate: Date.now() }
    ]

    expect(store.activeRecurring).toHaveLength(2)
    expect(store.upcomingRecurring).toHaveLength(2)
  })

  it('should reset state', () => {
    const store = useRecurringStore()
    store.list = [{ id: 1 }]
    store.loading = true

    store.reset()

    expect(store.list).toEqual([])
    expect(store.loading).toBe(false)
  })
})
