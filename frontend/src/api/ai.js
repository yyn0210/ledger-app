import request from './request'

// AI 智能分析
export function analyzeTransactions(params) {
  return request.get('/ai/analyze', { params })
}

// AI 预算建议
export function getBudgetSuggestions() {
  return request.get('/ai/budget/suggestions')
}

// AI 消费洞察
export function getSpendingInsights() {
  return request.get('/ai/insights')
}
