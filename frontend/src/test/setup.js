import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach } from 'vitest'

// Setup Pinia for tests
beforeEach(() => {
  const pinia = createPinia()
  setActivePinia(pinia)
})

// Global config for Vue Test Utils
config.global.stubs = {
  NIcon: true,
  NButton: true,
  NInput: true,
  NSelect: true,
  NDatePicker: true,
  NForm: true,
  NFormItem: true,
  NCard: true,
  NGrid: true,
  NGridItem: true,
  NSpace: true,
  NTag: true,
  NAlert: true,
  NProgress: true,
  NModal: true,
  NEmpty: true,
  NDataTable: true,
  NUpload: true,
  NRadio: true,
  NRadioGroup: true,
  NRadioButton: true,
  NCheckbox: true,
  NCheckboxGroup: true,
  NSwitch: true,
  NInputNumber: true,
  NTimeline: true,
  NTimelineItem: true
}
