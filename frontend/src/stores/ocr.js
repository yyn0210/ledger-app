import { defineStore } from 'pinia'
import { recognizeImage, batchRecognize } from '@/api/ocr'

export const useOcrStore = defineStore('ocr', {
  state: () => ({
    isScanning: false,
    scanResult: null,
    scanHistory: [],
    error: null,
    cameraEnabled: false
  }),
  getters: {
    hasResult: state => state.scanResult !== null,
    scanProgress: state => state.isScanning ? 100 : 0
  },
  actions: {
    async scanImage(file) {
      this.isScanning = true
      this.error = null
      try {
        const data = await recognizeImage(file)
        this.scanResult = data
        this.scanHistory.unshift({
          ...data,
          timestamp: Date.now(),
          image: file.name
        })
        return data
      } catch (error) {
        this.error = error.message || 'OCR 识别失败'
        throw error
      } finally {
        this.isScanning = false
      }
    },
    async scanMultiple(files) {
      this.isScanning = true
      this.error = null
      try {
        const data = await batchRecognize(files)
        this.scanResult = data
        return data
      } catch (error) {
        this.error = error.message || '批量 OCR 识别失败'
        throw error
      } finally {
        this.isScanning = false
      }
    },
    setResult(result) {
      this.scanResult = result
    },
    clearResult() {
      this.scanResult = null
      this.error = null
    },
    enableCamera() {
      this.cameraEnabled = true
    },
    disableCamera() {
      this.cameraEnabled = false
    }
  }
})
