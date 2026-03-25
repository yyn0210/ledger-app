import request from './request'

/**
 * 获取周期账单列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.status - 状态筛选
 */
export function getRecurringBillList(params) {
  return request({ url: '/recurring-bill', method: 'get', params })
}

/**
 * 创建周期账单
 * @param {Object} data - 周期账单数据
 * @param {string} data.name - 账单名称
 * @param {number} data.recurringType - 周期类型 (1-每日，2-每周，3-每两周，4-每月，5-每季度，6-每年)
 * @param {number} data.recurringValue - 周期值
 * @param {number} data.amount - 金额
 * @param {number} data.categoryId - 分类 ID
 * @param {number} data.accountId - 账户 ID
 * @param {number} data.transactionType - 交易类型 (1-收入，2-支出)
 * @param {string} data.startDate - 开始日期
 * @param {string} [data.endDate] - 结束日期（可选）
 * @param {string} [data.note] - 备注
 * @param {string} [data.merchant] - 商户
 * @param {boolean} [data.autoExecute] - 是否自动执行
 */
export function createRecurringBill(data) {
  return request({ url: '/recurring-bill', method: 'post', data })
}

/**
 * 获取周期账单详情
 * @param {number|string} id - 周期账单 ID
 */
export function getRecurringBillDetail(id) {
  return request({ url: `/recurring-bill/${id}`, method: 'get' })
}

/**
 * 更新周期账单
 * @param {number|string} id - 周期账单 ID
 * @param {Object} data - 周期账单数据
 */
export function updateRecurringBill(id, data) {
  return request({ url: `/recurring-bill/${id}`, method: 'put', data })
}

/**
 * 删除周期账单
 * @param {number|string} id - 周期账单 ID
 */
export function deleteRecurringBill(id) {
  return request({ url: `/recurring-bill/${id}`, method: 'delete' })
}

/**
 * 手动执行周期账单
 * @param {number|string} id - 周期账单 ID
 */
export function executeRecurringBill(id) {
  return request({ url: `/recurring-bill/${id}/execute`, method: 'post' })
}

/**
 * 暂停/恢复周期账单
 * @param {number|string} id - 周期账单 ID
 * @param {boolean} pause - true=暂停，false=恢复
 */
export function toggleRecurringBillStatus(id, pause) {
  return request({
    url: `/recurring-bill/${id}/status`,
    method: 'post',
    data: { pause }
  })
}
