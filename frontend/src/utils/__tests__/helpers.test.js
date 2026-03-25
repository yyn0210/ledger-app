import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  formatAmount,
  formatDate,
  calculatePercentage,
  generateId,
  deepClone,
  debounce,
  throttle,
  isEmpty,
  getTransactionTypeLabel,
  sumBy
} from '../helpers'

describe('helpers', () => {
  describe('formatAmount', () => {
    it('should format positive amount with default currency', () => {
      expect(formatAmount(1234.56)).toBe('¥1,234.56')
    })

    it('should format negative amount', () => {
      expect(formatAmount(-500.5)).toBe('¥-500.50')
    })

    it('should format zero', () => {
      expect(formatAmount(0)).toBe('¥0.00')
    })

    it('should handle null and undefined', () => {
      expect(formatAmount(null)).toBe('¥0.00')
      expect(formatAmount(undefined)).toBe('¥0.00')
    })

    it('should handle NaN', () => {
      expect(formatAmount(NaN)).toBe('¥0.00')
    })

    it('should use custom currency', () => {
      expect(formatAmount(100, '$')).toBe('$100.00')
      expect(formatAmount(100, '€')).toBe('€100.00')
    })

    it('should format large numbers with commas', () => {
      expect(formatAmount(1234567.89)).toBe('¥1,234,567.89')
    })
  })

  describe('formatDate', () => {
    it('should format date with default format', () => {
      const result = formatDate('2026-03-24T10:30:00Z')
      expect(result).toBe('2026-03-24')
    })

    it('should format with custom format', () => {
      const result = formatDate('2026-03-24T10:30:00Z', 'YYYY/MM/DD')
      expect(result).toBe('2026/03/24')
    })

    it('should format with time', () => {
      const result = formatDate('2026-03-24T10:30:45Z', 'YYYY-MM-DD HH:mm:ss')
      expect(result).toMatch(/2026-03-24 \d{2}:30:45/)
    })

    it('should return empty string for null', () => {
      expect(formatDate(null)).toBe('')
    })

    it('should return empty string for invalid date', () => {
      expect(formatDate('invalid')).toBe('')
    })

    it('should handle Date object', () => {
      const date = new Date('2026-03-24')
      const result = formatDate(date)
      expect(result).toBe('2026-03-24')
    })
  })

  describe('calculatePercentage', () => {
    it('should calculate percentage correctly', () => {
      expect(calculatePercentage(50, 100)).toBe(50)
      expect(calculatePercentage(25, 100)).toBe(25)
      expect(calculatePercentage(75, 200)).toBe(38)
    })

    it('should return 0 when total is 0', () => {
      expect(calculatePercentage(50, 0)).toBe(0)
    })

    it('should return 0 when total is null or undefined', () => {
      expect(calculatePercentage(50, null)).toBe(0)
      expect(calculatePercentage(50, undefined)).toBe(0)
    })

    it('should round to nearest integer', () => {
      expect(calculatePercentage(33.333, 100)).toBe(33)
      expect(calculatePercentage(66.666, 100)).toBe(67)
    })
  })

  describe('generateId', () => {
    it('should generate a string', () => {
      const id = generateId()
      expect(typeof id).toBe('string')
    })

    it('should generate unique IDs', () => {
      const ids = new Set()
      for (let i = 0; i < 100; i++) {
        ids.add(generateId())
      }
      expect(ids.size).toBe(100)
    })
  })

  describe('deepClone', () => {
    it('should clone primitive values', () => {
      expect(deepClone(42)).toBe(42)
      expect(deepClone('test')).toBe('test')
      expect(deepClone(null)).toBe(null)
    })

    it('should clone objects', () => {
      const obj = { a: 1, b: { c: 2 } }
      const clone = deepClone(obj)
      expect(clone).toEqual(obj)
      expect(clone).not.toBe(obj)
      expect(clone.b).not.toBe(obj.b)
    })

    it('should clone arrays', () => {
      const arr = [1, 2, [3, 4]]
      const clone = deepClone(arr)
      expect(clone).toEqual(arr)
      expect(clone).not.toBe(arr)
      expect(clone[2]).not.toBe(arr[2])
    })
  })

  describe('debounce', () => {
    beforeEach(() => {
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should delay function execution', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 100)
      
      debouncedFn()
      expect(fn).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(100)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should cancel previous calls', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 100)
      
      debouncedFn()
      debouncedFn()
      debouncedFn()
      
      vi.advanceTimersByTime(100)
      expect(fn).toHaveBeenCalledTimes(1)
    })
  })

  describe('throttle', () => {
    beforeEach(() => {
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should limit function calls', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 100)
      
      throttledFn()
      throttledFn()
      throttledFn()
      
      expect(fn).toHaveBeenCalledTimes(1)
      
      vi.advanceTimersByTime(100)
      throttledFn()
      expect(fn).toHaveBeenCalledTimes(2)
    })
  })

  describe('isEmpty', () => {
    it('should return true for null and undefined', () => {
      expect(isEmpty(null)).toBe(true)
      expect(isEmpty(undefined)).toBe(true)
    })

    it('should return true for empty string', () => {
      expect(isEmpty('')).toBe(true)
      expect(isEmpty('   ')).toBe(true)
    })

    it('should return true for empty array', () => {
      expect(isEmpty([])).toBe(true)
    })

    it('should return true for empty object', () => {
      expect(isEmpty({})).toBe(true)
    })

    it('should return false for non-empty values', () => {
      expect(isEmpty('test')).toBe(false)
      expect(isEmpty([1])).toBe(false)
      expect(isEmpty({ a: 1 })).toBe(false)
      expect(isEmpty(0)).toBe(false)
      expect(isEmpty(false)).toBe(false)
    })
  })

  describe('getTransactionTypeLabel', () => {
    it('should return income label', () => {
      const label = getTransactionTypeLabel('income')
      expect(label.text).toBe('收入')
      expect(label.color).toBe('#10B981')
      expect(label.icon).toBe('arrow-down')
    })

    it('should return expense label', () => {
      const label = getTransactionTypeLabel('expense')
      expect(label.text).toBe('支出')
      expect(label.color).toBe('#EF4444')
      expect(label.icon).toBe('arrow-up')
    })

    it('should return default for unknown type', () => {
      const label = getTransactionTypeLabel('unknown')
      expect(label.text).toBe('未知')
      expect(label.color).toBe('#64748B')
    })
  })

  describe('sumBy', () => {
    it('should sum numbers in array', () => {
      expect(sumBy([1, 2, 3, 4])).toBe(10)
    })

    it('should sum by key', () => {
      const arr = [{ amount: 10 }, { amount: 20 }, { amount: 30 }]
      expect(sumBy(arr, 'amount')).toBe(60)
    })

    it('should return 0 for empty array', () => {
      expect(sumBy([])).toBe(0)
    })

    it('should return 0 for non-array', () => {
      expect(sumBy(null)).toBe(0)
      expect(sumBy('test')).toBe(0)
    })

    it('should handle missing keys', () => {
      const arr = [{ amount: 10 }, { value: 20 }]
      expect(sumBy(arr, 'amount')).toBe(10)
    })
  })
})
