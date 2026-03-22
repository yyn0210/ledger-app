import request from './request'

// AI 智能分类
export function aiClassify(data) {
  return request({
    url: '/ai/classify',
    method: 'post',
    data
  })
}

// AI 智能分析
export function aiAnalyze(params) {
  return request({
    url: '/ai/analyze',
    method: 'get',
    params
  })
}

// AI 预算建议
export function aiBudgetSuggestion(params) {
  return request({
    url: '/ai/budget-suggestion',
    method: 'get',
    params
  })
}
