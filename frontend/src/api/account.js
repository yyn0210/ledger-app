import request from './request'

/**
 * 获取账户列表
 * @param {Object} params - 查询参数
 * @param {string} params.type - 账户类型筛选
 */
export function getAccountList(params) {
  return request({ url: '/accounts', method: 'get', params })
}

/**
 * 创建账户
 * @param {Object} data - 账户数据
 * @param {string} data.name - 账户名称（必填）
 * @param {string} data.type - 账户类型：cash|bank|credit|alipay|wechat|other
 * @param {number} data.balance - 初始余额
 * @param {string} data.color - 账户颜色
 * @param {string} data.note - 备注
 * @param {number} data.creditLimit - 信用额度（信用卡专用）
 * @param {number} data.billDay - 账单日（信用卡专用）
 * @param {number} data.repaymentDay - 还款日（信用卡专用）
 * @param {number} data.order - 排序号
 */
export function createAccount(data) {
  return request({ url: '/accounts', method: 'post', data })
}

/**
 * 获取账户详情
 * @param {number|string} id - 账户 ID
 */
export function getAccountDetail(id) {
  return request({ url: `/accounts/${id}`, method: 'get' })
}

/**
 * 更新账户
 * @param {number|string} id - 账户 ID
 * @param {Object} data - 账户数据
 */
export function updateAccount(id, data) {
  return request({ url: `/accounts/${id}`, method: 'put', data })
}

/**
 * 删除账户
 * @param {number|string} id - 账户 ID
 */
export function deleteAccount(id) {
  return request({ url: `/accounts/${id}`, method: 'delete' })
}

/**
 * 批量删除账户
 * @param {Array<number>} ids - 账户 ID 数组
 */
export function batchDeleteAccounts(ids) {
  return request({ url: '/accounts/batch', method: 'delete', data: { ids } })
}
