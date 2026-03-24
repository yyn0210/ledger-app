import request from './request'

export function aiClassify(data) {
  return request({ url: '/ai/classify', method: 'post', data })
}

export function aiAnalyze(params) {
  return request({ url: '/ai/analyze', method: 'get', params })
}

export function aiBudgetSuggestion(params) {
  return request({ url: '/ai/budget-suggestion', method: 'get', params })
}
