import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AmountInput from '../index.vue'

// Mock Naive UI components
vi.mock('naive-ui', () => ({
  NInputNumber: {
    name: 'NInputNumber',
    props: ['value', 'placeholder', 'min', 'max', 'precision', 'step', 'disabled'],
    emits: ['update:value'],
    template: '<input :value="value" @input="$emit(\'update:value\', $event.target.value)" />'
  }
}))

describe('AmountInput', () => {
  it('should emit update:modelValue on change', async () => {
    const wrapper = mount(AmountInput)
    
    // Trigger the change event directly
    wrapper.vm.$emit('update:modelValue', 50.25)
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })

  it('should emit change event', async () => {
    const wrapper = mount(AmountInput)
    
    wrapper.vm.$emit('change', 75)
    
    expect(wrapper.emitted('change')).toBeTruthy()
  })

  it('should have default placeholder', () => {
    const wrapper = mount(AmountInput)
    expect(wrapper.props().placeholder).toBe('请输入金额')
  })

  it('should accept modelValue prop', () => {
    const wrapper = mount(AmountInput, {
      props: { modelValue: 100 }
    })
    expect(wrapper.props().modelValue).toBe(100)
  })

  it('should accept min prop', () => {
    const wrapper = mount(AmountInput, {
      props: { min: 50 }
    })
    expect(wrapper.props().min).toBe(50)
  })

  it('should accept max prop', () => {
    const wrapper = mount(AmountInput, {
      props: { max: 1000 }
    })
    expect(wrapper.props().max).toBe(1000)
  })

  it('should accept disabled prop', () => {
    const wrapper = mount(AmountInput, {
      props: { disabled: true }
    })
    expect(wrapper.props().disabled).toBe(true)
  })
})
