import { describe, it, expect } from 'vitest'

// Mock utility functions that would be in a utils file
export function formatAmount(amount) {
  return Number(amount).toFixed(2)
}

export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export function parseVoiceAmount(text) {
  const match = text.match(/[\d,]+\.?\d*/)?.[0]
  return match ? parseFloat(match.replace(/,/g, '')) : null
}

export function parseVoiceType(text) {
  const isIncome = text.includes('收入') || text.includes('工资') || text.includes('收到')
  return isIncome ? 'income' : 'expense'
}

describe('Utility Functions', () => {
  describe('formatAmount', () => {
    it('should format number to 2 decimal places', () => {
      expect(formatAmount(100)).toBe('100.00')
      expect(formatAmount(100.5)).toBe('100.50')
      expect(formatAmount(100.555)).toBe('100.56')
    })

    it('should handle string input', () => {
      expect(formatAmount('100')).toBe('100.00')
      expect(formatAmount('100.5')).toBe('100.50')
    })
  })

  describe('formatDate', () => {
    it('should format date to YYYY-MM-DD', () => {
      const date = new Date('2026-03-24')
      expect(formatDate(date)).toBe('2026-03-24')
    })

    it('should return empty string for null/undefined', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate(undefined)).toBe('')
    })
  })

  describe('parseVoiceAmount', () => {
    it('should extract amount from text', () => {
      expect(parseVoiceAmount('今天花了 35 元')).toBe(35)
      expect(parseVoiceAmount('消费 128.5 元')).toBe(128.5)
      expect(parseVoiceAmount('金额：1,234.56')).toBe(1234.56)
    })

    it('should return null if no amount found', () => {
      expect(parseVoiceAmount('没有金额')).toBeNull()
    })
  })

  describe('parseVoiceType', () => {
    it('should detect income', () => {
      expect(parseVoiceType('收到工资 15000 元')).toBe('income')
      expect(parseVoiceType('收入 500 元')).toBe('income')
      expect(parseVoiceType('工资到账')).toBe('income')
    })

    it('should default to expense', () => {
      expect(parseVoiceType('今天吃饭花了 100 元')).toBe('expense')
      expect(parseVoiceType('打车 35 元')).toBe('expense')
    })
  })
})
