import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RecurringCard from '../RecurringCard.vue'

describe('RecurringCard', () => {
  const mockBill = {
    id: 1,
    name: '房租',
    recurringType: 4,
    recurringValue: 1,
    amount: 3000,
    transactionType: 2,
    categoryName: '住房',
    status: 1,
    nextExecutionDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    executionCount: 3,
    maxExecutions: null
  }

  it('should render with bill props', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill }
    })
    expect(wrapper.props().bill).toEqual(mockBill)
  })

  it('should accept showActions prop', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: false }
    })
    expect(wrapper.props().showActions).toBe(false)
  })

  it('should display amount for expense (negative)', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, transactionType: 2, amount: 3000 } }
    })
    expect(wrapper.text()).toContain('-')
    expect(wrapper.text()).toContain('3,000.00')
  })

  it('should display amount for income (positive)', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, transactionType: 1, amount: 5000 } }
    })
    expect(wrapper.text()).toContain('5,000.00')
  })

  it('should display category name', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, categoryName: '住房' } }
    })
    expect(wrapper.props().bill.categoryName).toBe('住房')
  })

  it('should display execution count', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, executionCount: 5 } }
    })
    expect(wrapper.text()).toContain('5')
  })

  it('should display max executions when set', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, executionCount: 3, maxExecutions: 12 } }
    })
    expect(wrapper.text()).toContain('3/12')
  })

  it('should emit edit event', async () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: true }
    })
    await wrapper.vm.$emit('edit', mockBill)
    expect(wrapper.emitted('edit')).toBeTruthy()
  })

  it('should emit delete event', async () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: true }
    })
    await wrapper.vm.$emit('delete', mockBill)
    expect(wrapper.emitted('delete')).toBeTruthy()
  })

  it('should emit execute event', async () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: true }
    })
    await wrapper.vm.$emit('execute', mockBill)
    expect(wrapper.emitted('execute')).toBeTruthy()
  })

  it('should emit toggle-status event', async () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: true }
    })
    await wrapper.vm.$emit('toggle-status', mockBill)
    expect(wrapper.emitted('toggle-status')).toBeTruthy()
  })

  it('should handle different recurring types', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, recurringType: 1 } }
    })
    expect(wrapper.props().bill.recurringType).toBe(1)
  })

  it('should handle different statuses', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, status: 2 } }
    })
    expect(wrapper.props().bill.status).toBe(2)
  })

  it('should format amount with commas', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, amount: 10000 } }
    })
    expect(wrapper.text()).toContain('10,000.00')
  })

  it('should handle missing nextExecutionDate', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, nextExecutionDate: null } }
    })
    expect(wrapper.props().bill.nextExecutionDate).toBeNull()
  })

  it('should show actions when showActions is true', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: true }
    })
    expect(wrapper.props().showActions).toBe(true)
  })

  it('should hide actions when showActions is false', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: mockBill, showActions: false }
    })
    expect(wrapper.props().showActions).toBe(false)
  })

  it('should display bill name in props', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, name: '测试账单' } }
    })
    expect(wrapper.props().bill.name).toBe('测试账单')
  })

  it('should display correct amount value', () => {
    const wrapper = mount(RecurringCard, {
      props: { bill: { ...mockBill, amount: 999.99 } }
    })
    expect(wrapper.props().bill.amount).toBe(999.99)
  })
})
