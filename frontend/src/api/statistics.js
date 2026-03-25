import request from './request'

/**
 * 获取统计概览
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 */
export function getStatisticsOverview(params) {
  return request({ url: '/statistics/overview', method: 'get', params })
}

/**
 * 获取收支趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {'day'|'week'|'month'} params.groupBy - 分组方式
 */
export function getTrendData(params) {
  return request({ url: '/statistics/trend', method: 'get', params })
}

/**
 * 获取分类统计数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {'income'|'expense'} params.type - 交易类型
 */
export function getCategoryStats(params) {
  return request({ url: '/statistics/category', method: 'get', params })
}

/**
 * 获取账户统计数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 */
export function getAccountStats(params) {
  return request({ url: '/statistics/account', method: 'get', params })
}

/**
 * 获取排行榜数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {'category'|'day'|'single'} params.type - 排行榜类型
 * @param {number} params.limit - 返回数量
 */
export function getRankings(params) {
  return request({ url: '/statistics/rankings', method: 'get', params })
}

/**
 * 获取预算执行数据
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 */
export function getBudgetExecution(params) {
  return request({ url: '/statistics/budget', method: 'get', params })
}
