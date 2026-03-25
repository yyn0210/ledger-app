/**
 * 通用工具函数
 */

/**
 * 格式化金额
 * @param {number} amount - 金额
 * @param {string} currency - 货币符号，默认 ¥
 * @returns {string} 格式化后的金额字符串
 */
export function formatAmount(amount, currency = '¥') {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return `${currency}0.00`
  }
  const num = Number(amount)
  return `${currency}${num.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')}`
}

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @param {string} format - 格式，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 计算百分比
 * @param {number} value - 当前值
 * @param {number} total - 总值
 * @returns {number} 百分比 (0-100)
 */
export function calculatePercentage(value, total) {
  if (total === 0 || total === null || total === undefined) return 0
  return Math.round((value / total) * 100)
}

/**
 * 生成唯一 ID
 * @returns {string} 唯一 ID
 */
export function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 深拷贝对象
 * @param {Object} obj - 要拷贝的对象
 * @returns {Object} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  return JSON.parse(JSON.stringify(obj))
}

/**
 * 防抖函数
 * @param {Function} func - 要防抖的函数
 * @param {number} wait - 等待时间 (ms)
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要节流的函数
 * @param {number} limit - 限制时间 (ms)
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit = 300) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

/**
 * 检查是否为空值
 * @param {*} value - 要检查的值
 * @returns {boolean} 是否为空
 */
export function isEmpty(value) {
  if (value === null || value === undefined) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

/**
 * 获取交易类型标签
 * @param {string} type - 交易类型 (income/expense)
 * @returns {Object} 标签配置
 */
export function getTransactionTypeLabel(type) {
  const labels = {
    income: { text: '收入', color: '#10B981', icon: 'arrow-down' },
    expense: { text: '支出', color: '#EF4444', icon: 'arrow-up' }
  }
  return labels[type] || { text: '未知', color: '#64748B', icon: 'question' }
}

/**
 * 计算数组总和
 * @param {Array} arr - 数组
 * @param {string} key - 要累加的键
 * @returns {number} 总和
 */
export function sumBy(arr, key) {
  if (!Array.isArray(arr)) return 0
  return arr.reduce((sum, item) => {
    const value = typeof item === 'number' ? item : (item[key] || 0)
    return sum + value
  }, 0)
}
