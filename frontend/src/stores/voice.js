import { defineStore } from 'pinia'
import { speechToText, parseVoiceCommand } from '@/api/voice'

export const useVoiceStore = defineStore('voice', {
  state: () => ({
    isRecording: false,
    isProcessing: false,
    transcript: '',
    parsedData: null,
    error: null,
    audioBlob: null
  }),
  getters: {
    hasResult: state => state.transcript !== '',
    recordingProgress: state => state.isRecording ? 100 : 0
  },
  actions: {
    async startRecording() {
      this.isRecording = true
      this.error = null
      this.transcript = ''
      this.parsedData = null
    },
    async stopRecording(audioBlob) {
      this.isRecording = false
      this.audioBlob = audioBlob
      await this.processVoice(audioBlob)
    },
    async processVoice(audioBlob) {
      this.isProcessing = true
      this.error = null
      try {
        // STT 识别
        const sttResult = await speechToText(audioBlob)
        this.transcript = sttResult.text || ''
        
        // 语义解析
        if (this.transcript) {
          const parsed = await parseVoiceCommand(this.transcript)
          this.parsedData = parsed
        }
        
        return this.parsedData
      } catch (error) {
        this.error = error.message || '语音识别失败'
        throw error
      } finally {
        this.isProcessing = false
      }
    },
    setTranscript(text) {
      this.transcript = text
    },
    setParsedData(data) {
      this.parsedData = data
    },
    reset() {
      this.isRecording = false
      this.isProcessing = false
      this.transcript = ''
      this.parsedData = null
      this.error = null
      this.audioBlob = null
    }
  }
})
