import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 获取统计数据
export function getStatistics(params) {
  if (USE_MOCK) {
    return mockApi.statistics.overview(params)
  }
  return request.get('/statistics', { params })
}

// 获取趋势数据
export function getTrendData(params) {
  if (USE_MOCK) {
    return mockApi.statistics.trend(params)
  }
  return request.get('/statistics/trend', { params })
}

// 获取分类统计
export function getCategoryStats(params) {
  if (USE_MOCK) {
    return mockApi.statistics.category(params)
  }
  return request.get('/statistics/category', { params })
}

// 获取月度对比
export function getMonthlyComparison(params) {
  if (USE_MOCK) {
    return mockApi.statistics.monthly(params)
  }
  return request.get('/statistics/monthly', { params })
}

// 导出数据
export function exportStatistics(params) {
  if (USE_MOCK) {
    return mockApi.statistics.export(params)
  }
  return request.get('/statistics/export', { params, responseType: 'blob' })
}
