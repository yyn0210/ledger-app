<template>
  <div class="voice-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Mic" size="28" color="#ec4899" />
        语音记账
      </h1>
    </div>

    <!-- 录音区域 -->
    <n-card class="record-card">
      <div class="record-section">
        <!-- 未录音状态 -->
        <div v-if="!isRecording && !transcript" class="record-idle">
          <div class="record-icon-large">
            <n-icon :component="Mic" size="80" color="#ec4899" />
          </div>
          <div class="record-text">
            <div class="record-title">点击开始语音记账</div>
            <div class="record-desc">说出消费金额和分类，例如："今天打车花了 35 元"</div>
          </div>
          <n-button
            type="primary"
            size="large"
            @click="startRecording"
            :custom-style="{
              height: '60px',
              borderRadius: '30px',
              fontSize: '16px'
            }"
          >
            <template #icon>
              <n-icon :component="Mic" />
            </template>
            开始录音
          </n-button>
        </div>

        <!-- 录音中状态 -->
        <div v-else-if="isRecording" class="record-active">
          <div class="record-animation">
            <div class="pulse-ring"></div>
            <div class="pulse-ring delay-1"></div>
            <div class="pulse-ring delay-2"></div>
            <n-icon :component="Mic" size="60" color="#fff" />
          </div>
          <div class="recording-text">
            <div class="recording-title">正在录音中...</div>
            <div class="recording-desc">请说出您的消费内容</div>
          </div>
          <n-button
            type="error"
            size="large"
            @click="stopRecording"
            :custom-style="{
              height: '50px',
              borderRadius: '25px'
            }"
          >
            <template #icon>
              <n-icon :component="Stop" />
            </template>
            停止录音
          </n-button>
        </div>

        <!-- 处理中状态 -->
        <div v-else-if="isProcessing" class="record-processing">
          <n-spin size="large" description="正在识别语音..." />
        </div>

        <!-- 识别结果 -->
        <div v-else-if="transcript" class="record-result">
          <div class="result-header">
            <span class="result-title">识别结果</span>
            <n-space>
              <n-button size="small" @click="resetRecording">
                <template #icon>
                  <n-icon :component="Refresh" />
                </template>
                重新录音
              </n-button>
            </n-space>
          </div>

          <!-- 语音文本 -->
          <n-alert type="info" title="您说的话">
            <div class="transcript-text">{{ transcript }}</div>
          </n-alert>

          <!-- 解析结果 -->
          <div v-if="parsedData" class="parsed-section">
            <div class="parsed-title">语义解析</div>
            <n-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-placement="top"
            >
              <n-grid :cols="2" :x-gap="16">
                <n-grid-item>
                  <n-form-item label="交易类型" path="type">
                    <n-radio-group v-model:value="formData.type">
                      <n-radio value="expense">支出</n-radio>
                      <n-radio value="income">收入</n-radio>
                    </n-radio-group>
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="金额" path="amount">
                    <n-input-number
                      v-model:value="formData.amount"
                      placeholder="请输入金额"
                      :min="0.01"
                      :precision="2"
                      style="width: 100%"
                    >
                      <template #prefix>¥</template>
                    </n-input-number>
                  </n-form-item>
                </n-grid-item>
              </n-grid>

              <n-grid :cols="2" :x-gap="16">
                <n-grid-item>
                  <n-form-item label="分类" path="categoryId">
                    <n-select
                      v-model:value="formData.categoryId"
                      :options="categoryOptions"
                      placeholder="请选择分类"
                    />
                  </n-form-item>
                </n-grid-item>
                <n-grid-item>
                  <n-form-item label="日期" path="date">
                    <n-date-picker
                      v-model:value="formData.date"
                      type="datetime"
                      placeholder="选择日期"
                      style="width: 100%"
                    />
                  </n-form-item>
                </n-grid-item>
              </n-grid>

              <n-form-item label="备注" path="note">
                <n-input
                  v-model:value="formData.note"
                  type="textarea"
                  placeholder="请输入备注"
                  :rows="3"
                  show-count
                  :maxlength="200"
                />
              </n-form-item>
            </n-form>
          </div>
        </div>
      </div>
    </n-card>

    <!-- 提交按钮 -->
    <div class="submit-bar" v-if="transcript && !isProcessing">
      <n-space style="width: 100%; justify-content: flex-end;">
        <n-button @click="resetRecording">取消</n-button>
        <n-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
        >
          <template #icon>
            <n-icon :component="CheckmarkCircle" />
          </template>
          确认记账
        </n-button>
      </n-space>
    </div>

    <!-- 使用示例 -->
    <n-card class="examples-card" title="语音记账示例">
      <div class="examples-list">
        <div class="example-item">
          <n-tag type="info" size="small">支出</n-tag>
          <span class="example-text">"今天中午吃饭花了 58 元"</span>
        </div>
        <div class="example-item">
          <n-tag type="info" size="small">支出</n-tag>
          <span class="example-text">"打车回家 35 块钱"</span>
        </div>
        <div class="example-item">
          <n-tag type="success" size="small">收入</n-tag>
          <span class="example-text">"收到工资 15000 元"</span>
        </div>
        <div class="example-item">
          <n-tag type="info" size="small">支出</n-tag>
          <span class="example-text">"超市购物 256.8 元"</span>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import {
  Mic, Stop, Refresh, CheckmarkCircle, Create, Trash
} from '@vicons/ionicons5'
import { useVoiceStore } from '@/stores/voice'
import { createTransaction } from '@/api/transaction'
import { getCategoryList } from '@/api/category'

const message = useMessage()
const voiceStore = useVoiceStore()

const formRef = ref(null)
const submitting = ref(false)
const categories = ref({ expense: [], income: [] })

const isRecording = computed(() => voiceStore.isRecording)
const isProcessing = computed(() => voiceStore.isProcessing)
const transcript = computed(() => voiceStore.transcript)
const parsedData = computed(() => voiceStore.parsedData)

const formData = reactive({
  type: 'expense',
  amount: null,
  categoryId: null,
  accountId: null,
  date: Date.now(),
  note: ''
})

const formRules = {
  amount: {
    required: true,
    message: '请输入金额',
    trigger: 'blur'
  },
  categoryId: {
    required: true,
    message: '请选择分类',
    trigger: 'change'
  }
}

const categoryOptions = computed(() => {
  const list = formData.type === 'income' ? categories.value.income : categories.value.expense
  return (list || []).map(c => ({ label: c.name, value: c.id }))
})

onMounted(async () => {
  await loadCategories()
})

const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    const defaults = data.defaults || []
    categories.value.expense = defaults.filter(c => c.type === 'expense')
    categories.value.income = defaults.filter(c => c.type === 'income')
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 开始录音
const startRecording = async () => {
  await voiceStore.startRecording()
  
  // 使用 Web Speech API（如果浏览器支持）
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    const recognition = new SpeechRecognition()
    
    recognition.lang = 'zh-CN'
    recognition.continuous = false
    recognition.interimResults = false
    
    recognition.onresult = (event) => {
      const text = event.results[0][0].transcript
      voiceStore.setTranscript(text)
      
      // 模拟语义解析
      parseVoiceText(text)
    }
    
    recognition.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      voiceStore.error = '语音识别失败，请重试'
      voiceStore.isRecording = false
    }
    
    recognition.onend = () => {
      voiceStore.isRecording = false
    }
    
    recognition.start()
  } else {
    // 浏览器不支持，使用 Mock
    setTimeout(() => {
      const mockText = '今天打车花了 35 元'
      voiceStore.setTranscript(mockText)
      parseVoiceText(mockText)
      voiceStore.isRecording = false
    }, 2000)
  }
}

// 停止录音
const stopRecording = () => {
  voiceStore.isRecording = false
  // 实际项目中这里会处理录音 blob
  const mockBlob = new Blob(['mock audio'], { type: 'audio/wav' })
  voiceStore.processVoice(mockBlob)
}

// 解析语音文本
const parseVoiceText = (text) => {
  // 简单的正则提取
  const amountMatch = text.match(/[\d,]+\.?\d*/)?.[0]
  const amount = amountMatch ? parseFloat(amountMatch.replace(/,/g, '')) : null
  
  // 判断类型
  const isIncome = text.includes('收入') || text.includes('工资') || text.includes('收到')
  
  // 自动填充
  formData.amount = amount
  formData.type = isIncome ? 'income' : 'expense'
  formData.note = text
  formData.date = Date.now()
  
  // 尝试匹配分类
  if (text.includes('吃饭') || text.includes('餐')) {
    formData.categoryId = 1 // 餐饮
  } else if (text.includes('打车') || text.includes('交通')) {
    formData.categoryId = 2 // 交通
  } else if (text.includes('购物') || text.includes('超市')) {
    formData.categoryId = 3 // 购物
  }
}

// 重置录音
const resetRecording = () => {
  voiceStore.reset()
  formData.amount = null
  formData.categoryId = null
  formData.note = ''
  formData.date = Date.now()
  formRef.value?.restoreValidation()
}

// 提交记账
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await createTransaction({
      ...formData,
      date: new Date(formData.date).toISOString().split('T')[0],
      voiceOrigin: true
    })

    message.success('语音记账成功！')
    resetRecording()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.voice-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.record-card {
  margin-bottom: 16px;
}

.record-section {
  padding: 20px 0;
}

.record-idle,
.record-active,
.record-processing,
.record-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.record-icon-large {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  border-radius: 50%;
}

.record-text {
  text-align: center;
}

.record-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.record-desc {
  font-size: 14px;
  color: #6b7280;
}

.record-active {
  padding: 40px 0;
}

.record-animation {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(236, 72, 153, 0.3);
  animation: pulse 1.5s ease-out infinite;
}

.pulse-ring.delay-1 {
  animation-delay: 0.5s;
}

.pulse-ring.delay-2 {
  animation-delay: 1s;
}

@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.recording-text {
  text-align: center;
}

.recording-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.recording-desc {
  font-size: 14px;
  color: #6b7280;
}

.record-processing {
  padding: 40px 0;
}

.result-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.transcript-text {
  font-size: 16px;
  line-height: 1.6;
  color: #1f2937;
}

.parsed-section {
  width: 100%;
  margin-top: 20px;
}

.parsed-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.submit-bar {
  margin-top: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.examples-card {
  margin-top: 24px;
}

.examples-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.example-text {
  font-size: 14px;
  color: #1f2937;
  font-style: italic;
}

:deep(.n-card__content) {
  padding: 20px;
}
</style>
