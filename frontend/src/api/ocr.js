import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// OCR 识别图片
export function recognizeImage(file) {
  if (USE_MOCK) {
    return mockApi.ocr.recognize(file)
  }
  const formData = new FormData()
  formData.append('image', file)
  return request.post('/ocr/recognize', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 批量 OCR 识别
export function batchRecognize(files) {
  if (USE_MOCK) {
    return mockApi.ocr.batchRecognize(files)
  }
  const formData = new FormData()
  files.forEach((file, index) => {
    formData.append(`images`, file)
  })
  return request.post('/ocr/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取 OCR 识别历史
export function getOcrHistory(params) {
  if (USE_MOCK) {
    return mockApi.ocr.history(params)
  }
  return request.get('/ocr/history', { params })
}
