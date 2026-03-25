import request from './request'

/**
 * 获取分类列表
 * @param {Object} params - 查询参数
 * @param {'income'|'expense'} params.type - 分类类型（收入/支出）
 */
export function getCategoryList(params) {
  return request({ url: '/categories', method: 'get', params })
}

/**
 * 创建分类
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称（必填）
 * @param {string} data.icon - 图标名称
 * @param {string} data.color - 颜色 hex
 * @param {'income'|'expense'} data.type - 分类类型
 * @param {number|null} data.parentId - 父分类 ID
 * @param {number} data.order - 排序号
 */
export function createCategory(data) {
  return request({ url: '/categories', method: 'post', data })
}

/**
 * 获取分类详情
 * @param {number|string} id - 分类 ID
 */
export function getCategoryDetail(id) {
  return request({ url: `/categories/${id}`, method: 'get' })
}

/**
 * 更新分类
 * @param {number|string} id - 分类 ID
 * @param {Object} data - 分类数据
 */
export function updateCategory(id, data) {
  return request({ url: `/categories/${id}`, method: 'put', data })
}

/**
 * 删除分类
 * @param {number|string} id - 分类 ID
 */
export function deleteCategory(id) {
  return request({ url: `/categories/${id}`, method: 'delete' })
}

/**
 * 批量删除分类
 * @param {Array<number>} ids - 分类 ID 数组
 */
export function batchDeleteCategories(ids) {
  return request({ url: '/categories/batch', method: 'delete', data: { ids } })
}
