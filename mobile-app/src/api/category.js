import { mockDelay } from './mock'

const USE_MOCK = true

// Mock 分类数据
const mockCategories = {
  defaults: [
    { id: 1, name: '餐饮', type: 'expense', icon: 'restaurant', color: '#EF4444' },
    { id: 2, name: '交通', type: 'expense', icon: 'car', color: '#3B82F6' },
    { id: 3, name: '购物', type: 'expense', icon: 'cart', color: '#F59E0B' },
    { id: 4, name: '娱乐', type: 'expense', icon: 'game', color: '#8B5CF6' },
    { id: 5, name: '医疗', type: 'expense', icon: 'medical', color: '#EC4899' },
    { id: 6, name: '其他', type: 'expense', icon: 'more', color: '#6B7280' },
    { id: 7, name: '工资', type: 'income', icon: 'money', color: '#10B981' },
    { id: 8, name: '兼职', type: 'income', icon: 'briefcase', color: '#06B6D4' },
    { id: 9, name: '理财', type: 'income', icon: 'trending-up', color: '#8B5CF6' },
    { id: 10, name: '其他', type: 'income', icon: 'more', color: '#6B7280' }
  ]
}

// 获取分类列表
export async function getCategoryList() {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, data: mockCategories }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: '/api/category/list',
      method: 'GET',
      success: resolve,
      fail: reject
    })
  })
}

// 创建分类
export async function createCategory(data) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, data: { id: Date.now(), ...data } }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: '/api/category',
      method: 'POST',
      data,
      success: resolve,
      fail: reject
    })
  })
}

// 更新分类
export async function updateCategory(id, data) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, data: { id, ...data } }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: `/api/category/${id}`,
      method: 'PUT',
      data,
      success: resolve,
      fail: reject
    })
  })
}

// 删除分类
export async function deleteCategory(id) {
  if (USE_MOCK) {
    await mockDelay()
    return { code: 200, message: '删除成功' }
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: `/api/category/${id}`,
      method: 'DELETE',
      success: resolve,
      fail: reject
    })
  })
}
