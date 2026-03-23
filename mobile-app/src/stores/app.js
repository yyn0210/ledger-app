import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 系统信息
    systemInfo: null,
    // 网络状态
    networkType: 'unknown',
    // 版本信息
    version: '1.0.0',
    // 是否需要更新
    needUpdate: false,
    // 全局加载状态
    loading: false
  }),
  
  getters: {
    isH5: (state) => {
      return uni.getSystemInfoSync().platform === 'web'
    },
    isWeixin: (state) => {
      // #ifdef MP-WEIXIN
      return true
      // #endif
      return false
    }
  },
  
  actions: {
    // 初始化系统信息
    initSystemInfo() {
      try {
        this.systemInfo = uni.getSystemInfoSync()
        uni.getNetworkType({
          success: (res) => {
            this.networkType = res.networkType
          }
        })
      } catch (error) {
        console.error('Get system info error:', error)
      }
    },
    
    // 设置加载状态
    setLoading(loading) {
      this.loading = loading
      if (loading) {
        uni.showLoading({ title: '加载中...' })
      } else {
        uni.hideLoading()
      }
    },
    
    // 检查更新
    checkUpdate() {
      // TODO: 实现版本检查逻辑
      this.needUpdate = false
    }
  }
})
