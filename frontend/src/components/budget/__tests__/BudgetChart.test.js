import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BudgetChart from '../BudgetChart.vue'

describe('BudgetChart', () => {
  const mockBudget = {
    id: 1,
    name: '餐饮预算',
    amount: 1000,
    usedAmount: 500,
    remaining: 500
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render chart container', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget }
    })
    expect(wrapper.find('[data-testid="budget-chart"]').exists()).toBe(true)
  })

  it('should display budget name', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget }
    })
    expect(wrapper.text()).toContain('餐饮预算')
  })

  it('should display total amount', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget }
    })
    expect(wrapper.text()).toContain('1,000')
  })

  it('should display used amount', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget }
    })
    expect(wrapper.text()).toContain('500')
  })

  it('should display remaining amount', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget }
    })
    expect(wrapper.text()).toContain('500')
  })

  it('should calculate percentage correctly (50%)', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 500 } }
    })
    expect(wrapper.text()).toContain('50%')
  })

  it('should calculate percentage correctly (75%)', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 750 } }
    })
    expect(wrapper.text()).toContain('75%')
  })

  it('should calculate percentage correctly (100%)', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 1000 } }
    })
    expect(wrapper.text()).toContain('100%')
  })

  it('should show normal status when usage < 90%', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 800 } }
    })
    const chart = wrapper.find('[data-testid="budget-chart"]')
    expect(chart.classes()).not.toContain('warning')
    expect(chart.classes()).not.toContain('danger')
  })

  it('should show warning status when usage >= 90% and < 100%', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 950 } }
    })
    const chart = wrapper.find('[data-testid="budget-chart"]')
    expect(chart.classes()).toContain('warning')
  })

  it('should show danger status when usage >= 100%', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 1200 } }
    })
    const chart = wrapper.find('[data-testid="budget-chart"]')
    expect(chart.classes()).toContain('danger')
  })

  it('should handle zero amount gracefully', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 0, usedAmount: 0 } }
    })
    // Should not crash with division by zero
    expect(wrapper.exists()).toBe(true)
  })

  it('should handle negative remaining amount', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 1500, remaining: -500 } }
    })
    expect(wrapper.text()).toContain('-500')
  })

  it('should format large amounts with commas', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000000 } }
    })
    expect(wrapper.text()).toContain('1,000,000')
  })

  it('should accept width prop', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, width: 300 }
    })
    expect(wrapper.props().width).toBe(300)
  })

  it('should accept height prop', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, height: 200 }
    })
    expect(wrapper.props().height).toBe(200)
  })

  it('should accept showLegend prop', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, showLegend: true }
    })
    expect(wrapper.props().showLegend).toBe(true)
  })

  it('should emit click event when chart is clicked', async () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, clickable: true }
    })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')[0]).toEqual([mockBudget])
  })

  it('should not emit click event when clickable is false', async () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, clickable: false }
    })
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeFalsy()
  })

  it('should display period label when provided', () => {
    const wrapper = mount(BudgetChart, {
      props: { 
        budget: { ...mockBudget, period: 'month' },
        showPeriod: true
      }
    })
    expect(wrapper.text()).toContain('月')
  })

  it('should handle undefined budget gracefully', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: null }
    })
    // Should render empty state or handle gracefully
    expect(wrapper.exists()).toBe(true)
  })

  it('should display currency symbol', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, currency: '¥' }
    })
    expect(wrapper.text()).toContain('¥')
  })

  it('should calculate used percentage with decimal precision', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: { ...mockBudget, amount: 1000, usedAmount: 333 } }
    })
    // 333/1000 = 33.3%
    expect(wrapper.text()).toContain('33')
  })

  it('should apply custom class when provided', () => {
    const wrapper = mount(BudgetChart, {
      props: { budget: mockBudget, customClass: 'custom-budget-chart' }
    })
    expect(wrapper.classes()).toContain('custom-budget-chart')
  })
})
