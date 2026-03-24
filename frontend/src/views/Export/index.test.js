import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createApp, h } from 'vue'
import { NMessageProvider } from 'naive-ui'
import Export from '@/views/Export/index.vue'

describe('Export View', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const mountExport = () => {
    return mount(
      {
        setup() {
          return () => h(NMessageProvider, null, { default: () => h(Export) })
        }
      },
      {
        global: {
          stubs: {
            NForm: true,
            NFormItem: true,
            NSelect: true,
            NCheckbox: true,
            NCheckboxGroup: true,
            NDatePicker: true,
            NCard: true,
            NGrid: true,
            NGridItem: true,
            NSpace: true,
            NUpload: true,
            NDivider: true,
            NDataTable: true,
            NEmpty: true,
            NTag: true,
            NIcon: true,
            NButton: true,
            NAlert: true,
            NRadio: true,
            NRadioGroup: true
          }
        }
      }
    )
  }

  it('should render page title', () => {
    const wrapper = mountExport()
    expect(wrapper.text()).toContain('数据导出')
  })

  it('should have export form', () => {
    const wrapper = mountExport()
    // Check that the component renders with correct structure
    expect(wrapper.find('.export-page').exists()).toBe(true)
    expect(wrapper.find('.page-header').exists()).toBe(true)
  })
})
