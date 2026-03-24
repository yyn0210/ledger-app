import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useVoiceStore } from '@/stores/voice'

describe('Voice Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with default state', () => {
    const store = useVoiceStore()
    expect(store.isRecording).toBe(false)
    expect(store.isProcessing).toBe(false)
    expect(store.transcript).toBe('')
    expect(store.parsedData).toBeNull()
    expect(store.error).toBeNull()
  })

  it('should have correct getters', () => {
    const store = useVoiceStore()
    expect(store.hasResult).toBe(false)
    expect(store.recordingProgress).toBe(0)

    store.transcript = 'Test transcript'
    expect(store.hasResult).toBe(true)
  })

  it('should set transcript', () => {
    const store = useVoiceStore()
    store.setTranscript('Hello world')
    expect(store.transcript).toBe('Hello world')
  })

  it('should set parsed data', () => {
    const store = useVoiceStore()
    const data = { amount: 100, type: 'expense' }
    store.setParsedData(data)
    expect(store.parsedData).toEqual(data)
  })

  it('should reset state', () => {
    const store = useVoiceStore()
    store.isRecording = true
    store.transcript = 'Test'
    store.error = 'Error'

    store.reset()

    expect(store.isRecording).toBe(false)
    expect(store.transcript).toBe('')
    expect(store.error).toBeNull()
  })
})
