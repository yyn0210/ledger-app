import { onLCP, onFID, onCLS, onFCP, onTTFB } from 'web-vitals'

/**
 * Web Vitals 性能监控
 * 将性能指标上报到分析服务
 */

// 性能指标上报函数
function sendToAnalytics(metric) {
  // 生产环境才上报
  if (import.meta.env.PROD) {
    const body = {
      name: metric.name,
      value: metric.value,
      delta: metric.delta,
      rating: metric.rating,
      id: metric.id,
      navigationType: metric.navigationType,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: Date.now()
    }

    // 使用 sendBeacon 确保数据发送成功
    navigator.sendBeacon('/api/analytics/performance', JSON.stringify(body))
    
    // 开发环境打印到控制台
    console.log(`[Web Vitals] ${metric.name}:`, metric.value, metric.rating)
  }
}

/**
 * 初始化 Web Vitals 监控
 */
export function initWebVitals() {
  // Largest Contentful Paint (LCP)
  // 衡量页面主要内容加载完成的时间
  // 目标：<2.5s
  onLCP(sendToAnalytics)

  // First Input Delay (FID)
  // 衡量用户首次交互到浏览器响应的时间
  // 目标：<100ms
  onFID(sendToAnalytics)

  // Cumulative Layout Shift (CLS)
  // 衡量页面视觉稳定性
  // 目标：<0.1
  onCLS(sendToAnalytics)

  // First Contentful Paint (FCP)
  // 衡量首次内容绘制的时间
  // 目标：<1.5s
  onFCP(sendToAnalytics)

  // Time to First Byte (TTFB)
  // 衡量服务器响应时间
  // 目标：<800ms
  onTTFB(sendToAnalytics)
}

/**
 * 获取当前页面性能指标快照
 */
export function getPerformanceMetrics() {
  const metrics = {}
  
  // 从 Performance API 获取基础指标
  if (window.performance && window.performance.getEntriesByType) {
    const navigation = performance.getEntriesByType('navigation')[0]
    if (navigation) {
      metrics.ttfb = navigation.responseStart
      metrics.domContentLoaded = navigation.domContentLoadedEventEnd
      metrics.loadComplete = navigation.loadEventEnd
    }

    // 获取资源加载信息
    const resources = performance.getEntriesByType('resource')
    metrics.resourceCount = resources.length
    metrics.resourceLoadTime = resources.reduce((sum, r) => sum + r.duration, 0)
  }

  // 获取内存信息（仅 Chrome）
  if (window.performance && window.performance.memory) {
    metrics.jsHeapSizeLimit = performance.memory.jsHeapSizeLimit
    metrics.totalJSHeapSize = performance.memory.totalJSHeapSize
    metrics.usedJSHeapSize = performance.memory.usedJSHeapSize
  }

  // 获取网络信息（仅部分浏览器）
  if (navigator.connection) {
    metrics.effectiveType = navigator.connection.effectiveType
    metrics.downlink = navigator.connection.downlink
    metrics.rtt = navigator.connection.rtt
  }

  return metrics
}

/**
 * 性能指标评级
 */
export function getRating(value, thresholds) {
  if (value <= thresholds.good) return 'good'
  if (value <= thresholds.needsImprovement) return 'needs-improvement'
  return 'poor'
}

// Web Vitals 阈值标准
export const thresholds = {
  lcp: { good: 2500, needsImprovement: 4000 }, // ms
  fid: { good: 100, needsImprovement: 300 }, // ms
  cls: { good: 0.1, needsImprovement: 0.25 }, // score
  fcp: { good: 1500, needsImprovement: 3000 }, // ms
  ttfb: { good: 800, needsImprovement: 1800 } // ms
}
