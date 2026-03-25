import { describe, it, expect } from 'vitest'
import theme, { 
  PRIMARY, 
  FUNCTIONAL, 
  NEUTRAL, 
  ACCOUNT_TYPES, 
  CATEGORY_ICONS,
  SPACING,
  RADIUS,
  SHADOWS,
  BREAKPOINTS
} from '../theme'

describe('theme', () => {
  describe('PRIMARY colors', () => {
    it('should have correct default primary color', () => {
      expect(PRIMARY.DEFAULT).toBe('#4F46E5')
    })

    it('should have light and dark variants', () => {
      expect(PRIMARY.LIGHT).toBe('#6366F1')
      expect(PRIMARY.DARK).toBe('#4338CA')
    })
  })

  describe('FUNCTIONAL colors', () => {
    it('should have success color for income', () => {
      expect(FUNCTIONAL.SUCCESS).toBe('#10B981')
    })

    it('should have error color for expense', () => {
      expect(FUNCTIONAL.ERROR).toBe('#EF4444')
    })

    it('should have warning color', () => {
      expect(FUNCTIONAL.WARNING).toBe('#F59E0B')
    })

    it('should have info color', () => {
      expect(FUNCTIONAL.INFO).toBe('#3B82F6')
    })
  })

  describe('NEUTRAL colors', () => {
    it('should have all neutral shades', () => {
      expect(NEUTRAL[900]).toBe('#0F172A')
      expect(NEUTRAL[700]).toBe('#334155')
      expect(NEUTRAL[500]).toBe('#64748B')
      expect(NEUTRAL[400]).toBe('#94A3B8')
      expect(NEUTRAL[200]).toBe('#E2E8F0')
      expect(NEUTRAL[100]).toBe('#F1F5F9')
      expect(NEUTRAL.WHITE).toBe('#FFFFFF')
    })
  })

  describe('ACCOUNT_TYPES', () => {
    it('should have all account type configurations', () => {
      expect(ACCOUNT_TYPES.cash).toBeDefined()
      expect(ACCOUNT_TYPES.bank).toBeDefined()
      expect(ACCOUNT_TYPES.credit).toBeDefined()
      expect(ACCOUNT_TYPES.alipay).toBeDefined()
      expect(ACCOUNT_TYPES.wechat).toBeDefined()
      expect(ACCOUNT_TYPES.other).toBeDefined()
    })

    it('cash should have correct label and color', () => {
      expect(ACCOUNT_TYPES.cash.label).toBe('现金')
      expect(ACCOUNT_TYPES.cash.color).toBe(FUNCTIONAL.SUCCESS)
      expect(ACCOUNT_TYPES.cash.icon).toBe('cash')
    })

    it('credit should have warning color', () => {
      expect(ACCOUNT_TYPES.credit.color).toBe(FUNCTIONAL.WARNING)
    })
  })

  describe('CATEGORY_ICONS', () => {
    it('should have all category icon colors', () => {
      expect(CATEGORY_ICONS.food).toBe('#FF6B6B')
      expect(CATEGORY_ICONS.transport).toBe('#3B82F6')
      expect(CATEGORY_ICONS.shopping).toBe('#F97316')
      expect(CATEGORY_ICONS.entertainment).toBe('#EC4899')
      expect(CATEGORY_ICONS.income).toBe('#10B981')
      expect(CATEGORY_ICONS.other).toBe(NEUTRAL[500])
    })
  })

  describe('SPACING', () => {
    it('should have all spacing values', () => {
      expect(SPACING[1]).toBe('4px')
      expect(SPACING[2]).toBe('8px')
      expect(SPACING[3]).toBe('12px')
      expect(SPACING[4]).toBe('16px')
      expect(SPACING[6]).toBe('24px')
      expect(SPACING[8]).toBe('32px')
      expect(SPACING[12]).toBe('48px')
      expect(SPACING[16]).toBe('64px')
    })
  })

  describe('RADIUS', () => {
    it('should have all radius values', () => {
      expect(RADIUS.SM).toBe('4px')
      expect(RADIUS.MD).toBe('8px')
      expect(RADIUS.LG).toBe('12px')
      expect(RADIUS.FULL).toBe('9999px')
    })
  })

  describe('SHADOWS', () => {
    it('should have all shadow values', () => {
      expect(SHADOWS.SM).toBe('0 1px 2px rgba(0, 0, 0, 0.05)')
      expect(SHADOWS.MD).toBe('0 4px 6px -1px rgba(0, 0, 0, 0.1)')
      expect(SHADOWS.LG).toBe('0 10px 15px -3px rgba(0, 0, 0, 0.1)')
    })
  })

  describe('BREAKPOINTS', () => {
    it('should have all breakpoint values', () => {
      expect(BREAKPOINTS.SM).toBe(640)
      expect(BREAKPOINTS.MD).toBe(768)
      expect(BREAKPOINTS.LG).toBe(1024)
      expect(BREAKPOINTS.XL).toBe(1280)
      expect(BREAKPOINTS['2XL']).toBe(1536)
    })
  })

  describe('default export', () => {
    it('should export all theme properties', () => {
      expect(theme.PRIMARY).toBe(PRIMARY)
      expect(theme.FUNCTIONAL).toBe(FUNCTIONAL)
      expect(theme.NEUTRAL).toBe(NEUTRAL)
      expect(theme.ACCOUNT_TYPES).toBe(ACCOUNT_TYPES)
      expect(theme.CATEGORY_ICONS).toBe(CATEGORY_ICONS)
      expect(theme.SPACING).toBe(SPACING)
      expect(theme.RADIUS).toBe(RADIUS)
      expect(theme.SHADOWS).toBe(SHADOWS)
      expect(theme.BREAKPOINTS).toBe(BREAKPOINTS)
    })
  })
})
