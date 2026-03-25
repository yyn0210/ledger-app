/**
 * 设计系统主题配置
 * 基于设计规范 (task-20260324-000603)
 */

// 品牌色
export const PRIMARY = {
  DEFAULT: '#4F46E5',  // Primary Indigo
  LIGHT: '#6366F1',
  DARK: '#4338CA'
}

// 功能色
export const FUNCTIONAL = {
  SUCCESS: '#10B981',  // 收入、正向数据
  ERROR: '#EF4444',    // 支出、警告
  WARNING: '#F59E0B',  // 待处理
  INFO: '#3B82F6'      // 信息提示
}

// 中性色
export const NEUTRAL = {
  900: '#0F172A',  // 主标题
  700: '#334155',  // 正文
  500: '#64748B',  // 次要文字
  400: '#94A3B8',  // 占位符
  200: '#E2E8F0',  // 边框
  100: '#F1F5F9',  // 背景
  WHITE: '#FFFFFF'
}

// 账户类型颜色
export const ACCOUNT_TYPES = {
  cash: { label: '现金', color: FUNCTIONAL.SUCCESS, icon: 'cash' },
  bank: { label: '银行卡', color: FUNCTIONAL.INFO, icon: 'bank' },
  credit: { label: '信用卡', color: FUNCTIONAL.WARNING, icon: 'credit' },
  alipay: { label: '支付宝', color: '#1677FF', icon: 'alipay' },
  wechat: { label: '微信支付', color: '#07C160', icon: 'wechat' },
  other: { label: '其他', color: NEUTRAL[500], icon: 'other' }
}

// 分类图标颜色（与 IconPicker 对应）
export const CATEGORY_ICONS = {
  food: '#FF6B6B',
  transport: '#3B82F6',
  shopping: '#F97316',
  entertainment: '#EC4899',
  income: '#10B981',
  other: NEUTRAL[500]
}

// 间距系统
export const SPACING = {
  1: '4px',
  2: '8px',
  3: '12px',
  4: '16px',
  5: '20px',
  6: '24px',
  8: '32px',
  10: '40px',
  12: '48px',
  16: '64px'
}

// 圆角规范
export const RADIUS = {
  SM: '4px',
  MD: '8px',
  LG: '12px',
  FULL: '9999px'
}

// 阴影
export const SHADOWS = {
  SM: '0 1px 2px rgba(0, 0, 0, 0.05)',
  MD: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
  LG: '0 10px 15px -3px rgba(0, 0, 0, 0.1)'
}

// 响应式断点
export const BREAKPOINTS = {
  SM: 640,
  MD: 768,
  LG: 1024,
  XL: 1280,
  '2XL': 1536
}

// 默认导出
export default {
  PRIMARY,
  FUNCTIONAL,
  NEUTRAL,
  ACCOUNT_TYPES,
  CATEGORY_ICONS,
  SPACING,
  RADIUS,
  SHADOWS,
  BREAKPOINTS
}
