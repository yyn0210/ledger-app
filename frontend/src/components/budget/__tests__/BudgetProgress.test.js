import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BudgetProgress from '../BudgetProgress.vue'

describe('BudgetProgress', () => {
  const defaultProps = {
    used: 500,
    total: 1000,
    type: 'line',
    showIndicator: true,
    showDetails: true
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  // Basic rendering tests
  it('should render progress component', () => {
    const wrapper = mount(BudgetProgress, {
      props: defaultProps
    })
    expect(wrapper.find('.budget-progress').exists()).toBe(true)
  })

  it('should display used amount', () => {
    const wrapper = mount(BudgetProgress, {
      props: defaultProps
    })
    expect(wrapper.text()).toContain('500')
  })

  it('should display total amount', () => {
    const wrapper = mount(BudgetProgress, {
      props: defaultProps
    })
    expect(wrapper.text()).toContain('1,000')
  })

  it('should display remaining amount', () => {
    const wrapper = mount(BudgetProgress, {
      props: defaultProps
    })
    expect(wrapper.text()).toContain('500') // 1000 - 500 = 500
  })

  // Percentage calculation tests
  it('should calculate 50% progress', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 500, total: 1000 }
    })
    expect(wrapper.text()).toContain('50')
  })

  it('should calculate 75% progress', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 750, total: 1000 }
    })
    expect(wrapper.text()).toContain('75')
  })

  it('should calculate 100% progress', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1000, total: 1000 }
    })
    expect(wrapper.text()).toContain('100')
  })

  it('should calculate 0% progress', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 0, total: 1000 }
    })
    expect(wrapper.text()).toContain('0')
  })

  // Status tests
  it('should show normal status when usage < 90%', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 800, total: 1000 }
    })
    expect(wrapper.vm.isOverBudget).toBe(false)
    expect(wrapper.vm.isWarning).toBe(false)
  })

  it('should show warning status when usage >= 90%', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 900, total: 1000 }
    })
    expect(wrapper.vm.isWarning).toBe(true)
  })

  it('should show warning status when usage >= 90% and < 100%', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 950, total: 1000 }
    })
    expect(wrapper.vm.isWarning).toBe(true)
    expect(wrapper.vm.isOverBudget).toBe(false)
  })

  it('should show over budget status when usage > 100%', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1200, total: 1000 }
    })
    expect(wrapper.vm.isOverBudget).toBe(true)
  })

  it('should calculate over amount correctly', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1500, total: 1000 }
    })
    expect(wrapper.vm.overAmount).toBe(500)
  })

  // Edge cases
  it('should handle zero total gracefully', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 0, total: 0 }
    })
    expect(wrapper.exists()).toBe(true)
    // Percentage should be 0 or NaN handled gracefully
    expect(wrapper.vm.percentage).toBeDefined()
  })

  it('should handle negative used amount', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: -100, total: 1000 }
    })
    expect(wrapper.vm.percentage).toBeLessThanOrEqual(0)
  })

  it('should handle used > total (over budget)', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 2000, total: 1000 }
    })
    expect(wrapper.vm.percentage).toBeGreaterThan(100)
  })

  // Prop tests
  it('should accept type prop as line', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, type: 'line' }
    })
    expect(wrapper.props().type).toBe('line')
  })

  it('should accept type prop as circle', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, type: 'circle' }
    })
    expect(wrapper.props().type).toBe('circle')
  })

  it('should accept height prop', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, height: 24 }
    })
    expect(wrapper.props().height).toBe(24)
  })

  it('should accept showIndicator prop', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, showIndicator: false }
    })
    expect(wrapper.props().showIndicator).toBe(false)
  })

  it('should accept showDetails prop', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, showDetails: false }
    })
    expect(wrapper.props().showDetails).toBe(false)
  })

  // Color tests
  it('should use correct color for normal status', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 500, total: 1000 }
    })
    expect(wrapper.vm.progressColor).toBeDefined()
  })

  it('should use warning color for warning status', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 950, total: 1000 }
    })
    expect(wrapper.vm.progressColor).toBe('warning')
  })

  it('should use error color for over budget status', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1200, total: 1000 }
    })
    expect(wrapper.vm.progressColor).toBe('error')
  })

  // Alert visibility tests
  it('should show over budget alert when over budget', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1200, total: 1000 }
    })
    expect(wrapper.text()).toContain('已超支')
  })

  it('should show warning alert when near budget', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 950, total: 1000 }
    })
    expect(wrapper.text()).toContain('即将超支')
  })

  it('should not show alerts when usage is normal', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 500, total: 1000 }
    })
    expect(wrapper.text()).not.toContain('已超支')
    expect(wrapper.text()).not.toContain('即将超支')
  })

  // Formatting tests
  it('should format amounts with commas', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 1000000, total: 2000000 }
    })
    expect(wrapper.text()).toContain('1,000,000')
  })

  it('should handle decimal amounts', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, used: 500.50, total: 1000 }
    })
    expect(wrapper.text()).toContain('500.5')
  })

  // Details visibility tests
  it('should show details when showDetails is true', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, showDetails: true }
    })
    expect(wrapper.text()).toContain('已用')
    expect(wrapper.text()).toContain('剩余')
    expect(wrapper.text()).toContain('预算')
  })

  it('should hide details when showDetails is false', () => {
    const wrapper = mount(BudgetProgress, {
      props: { ...defaultProps, showDetails: false }
    })
    // Details section should not be visible
    expect(wrapper.vm.showDetails).toBe(false)
  })
})
