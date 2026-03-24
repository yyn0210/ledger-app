import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取分类列表
export function getCategoryList(params) {
  if (USE_MOCK) {
    return mockApi.categories.list(params)
  }
  return request.get('/categories', { params })
}

// 创建分类
export function createCategory(data) {
  if (USE_MOCK) {
    return mockApi.categories.create(data)
  }
  return request.post('/categories', data)
}

// 更新分类
export function updateCategory(id, data) {
  if (USE_MOCK) {
    return mockApi.categories.update(id, data)
  }
  return request.put(`/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id) {
  if (USE_MOCK) {
    return mockApi.categories.delete(id)
  }
  return request.delete(`/categories/${id}`)
}

// 获取分类详情
export function getCategoryDetail(id) {
  if (USE_MOCK) {
    const category = mockApi.categories.list().then(res => 
      [...res.data.defaults, ...res.data.customs].find(c => c.id === id)
    )
    return category || Promise.reject({ code: 404, message: '分类不存在' })
  }
  return request.get(`/categories/${id}`)
}
