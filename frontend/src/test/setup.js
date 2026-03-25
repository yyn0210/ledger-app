import '@testing-library/jest-dom'
import { config } from '@vue/test-utils'

// 全局配置 Vue Test Utils
config.global.mocks = {
  $t: (key) => key
}

// 抑制 Vue 警告
config.global.config.warnHandler = () => {}
