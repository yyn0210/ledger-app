import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOcrStore } from '@/stores/ocr'

describe('Ocr Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with default state', () => {
    const store = useOcrStore()
    expect(store.isScanning).toBe(false)
    expect(store.scanResult).toBeNull()
    expect(store.error).toBeNull()
  })

  it('should have correct getters', () => {
    const store = useOcrStore()
    expect(store.hasResult).toBe(false)
    expect(store.scanProgress).toBe(0)

    store.scanResult = { text: 'Test' }
    expect(store.hasResult).toBe(true)
    expect(store.scanProgress).toBe(0)

    store.isScanning = true
    expect(store.scanProgress).toBe(100)
  })

  it('should set result', () => {
    const store = useOcrStore()
    const result = { text: 'Test OCR result', amount: 100 }
    store.setResult(result)
    expect(store.scanResult).toEqual(result)
  })

  it('should clear result', () => {
    const store = useOcrStore()
    store.scanResult = { text: 'Test' }
    store.error = 'Error'

    store.clearResult()

    expect(store.scanResult).toBeNull()
    expect(store.error).toBeNull()
  })
})
