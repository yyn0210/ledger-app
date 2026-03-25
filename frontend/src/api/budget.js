import request from './request'

/**
 * 获取预算列表
 * @param {Object} params - 查询参数
 * @param {'category'|'account'} params.type - 预算类型筛选
 * @param {'week'|'month'|'year'} params.period - 周期筛选
 */
export function getBudgetList(params) {
  return request({ url: '/budgets', method: 'get', params })
}

/**
 * 创建预算
 * @param {Object} data - 预算数据
 * @param {string} data.name - 预算名称
 * @param {'category'|'account'} data.type - 预算类型
 * @param {Array<number>} data.targetIds - 目标 ID 数组（分类或账户）
 * @param {number} data.amount - 预算金额
 * @param {'week'|'month'|'year'} data.period - 周期
 * @param {string} data.startDate - 开始日期
 */
export function createBudget(data) {
  return request({ url: '/budgets', method: 'post', data })
}

/**
 * 获取预算详情
 * @param {number|string} id - 预算 ID
 */
export function getBudgetDetail(id) {
  return request({ url: `/budgets/${id}`, method: 'get' })
}

/**
 * 获取预算执行数据
 * @param {number|string} id - 预算 ID
 */
export function getBudgetExecution(id) {
  return request({ url: `/budgets/${id}/execution`, method: 'get' })
}

/**
 * 更新预算
 * @param {number|string} id - 预算 ID
 * @param {Object} data - 预算数据
 */
export function updateBudget(id, data) {
  return request({ url: `/budgets/${id}`, method: 'put', data })
}

/**
 * 删除预算
 * @param {number|string} id - 预算 ID
 */
export function deleteBudget(id) {
  return request({ url: `/budgets/${id}`, method: 'delete' })
}

/**
 * 批量删除预算
 * @param {Array<number>} ids - 预算 ID 数组
 */
export function batchDeleteBudgets(ids) {
  return request({ url: '/budgets/batch', method: 'delete', data: { ids } })
}
