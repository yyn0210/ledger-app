import request from './request'
import { mockApi } from '@/mock'

const USE_MOCK = true

// 语音识别（STT）
export function speechToText(audioBlob) {
  if (USE_MOCK) {
    return mockApi.voice.speechToText(audioBlob)
  }
  const formData = new FormData()
  formData.append('audio', audioBlob)
  return request.post('/voice/stt', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 语义解析
export function parseVoiceCommand(text) {
  if (USE_MOCK) {
    return mockApi.voice.parse(text)
  }
  return request.post('/voice/parse', { text })
}

// 语音记账（一步完成）
export function voiceTransaction(audioBlob) {
  if (USE_MOCK) {
    return mockApi.voice.transaction(audioBlob)
  }
  const formData = new FormData()
  formData.append('audio', audioBlob)
  return request.post('/voice/transaction', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
