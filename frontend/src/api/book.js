import request from './request'
<<<<<<< HEAD

export function getBookList(params) {
  return request({ url: '/books', method: 'get', params })
}

export function createBook(data) {
  return request({ url: '/books', method: 'post', data })
}

export function getBookDetail(id) {
  return request({ url: `/books/${id}`, method: 'get' })
}

export function updateBook(id, data) {
  return request({ url: `/books/${id}`, method: 'put', data })
}

export function deleteBook(id) {
  return request({ url: `/books/${id}`, method: 'delete' })
=======
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取账本列表
export function getBookList(params) {
  if (USE_MOCK) {
    return mockApi.books.list(params)
  }
  return request.get('/books', { params })
}

// 创建账本
export function createBook(data) {
  if (USE_MOCK) {
    return mockApi.books.create(data)
  }
  return request.post('/books', data)
}

// 更新账本
export function updateBook(id, data) {
  if (USE_MOCK) {
    return mockApi.books.update(id, data)
  }
  return request.put(`/books/${id}`, data)
}

// 删除账本
export function deleteBook(id) {
  if (USE_MOCK) {
    return mockApi.books.delete(id)
  }
  return request.delete(`/books/${id}`)
}

// 切换当前账本
export function switchBook(id) {
  if (USE_MOCK) {
    return mockApi.books.update(id, { isCurrent: true })
  }
  return request.put(`/books/${id}/switch`)
}

// 获取账本详情
export function getBookDetail(id) {
  if (USE_MOCK) {
    const book = mockApi.books.list().then(res => res.data.list.find(b => b.id === id))
    return book || Promise.reject({ code: 404, message: '账本不存在' })
  }
  return request.get(`/books/${id}`)
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
}
