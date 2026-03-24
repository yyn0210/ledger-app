<template>
  <div class="ocr-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Scan" size="28" color="#8b5cf6" />
        拍照记账
      </h1>
    </div>

    <!-- 上传区域 -->
    <n-card class="upload-card" v-if="!hasImage">
      <n-upload
        :max="1"
        :show-file-list="false"
        accept="image/*"
        @change="handleImageUpload"
      >
        <div class="upload-area">
          <div class="upload-icon">
            <n-icon :component="Camera" size="48" color="#8b5cf6" />
          </div>
          <div class="upload-text">
            <div class="upload-title">点击上传小票/账单图片</div>
            <div class="upload-desc">支持 JPG、PNG 格式，最大 10MB</div>
          </div>
          <n-button type="primary" size="large">
            <template #icon>
              <n-icon :component="ImageOutline" />
            </template>
            选择图片
          </n-button>
        </div>
      </n-upload>
    </n-card>

    <!-- 图片预览和 OCR 结果 -->
    <n-grid :cols="2" :x-gap="16" v-else>
      <n-grid-item>
        <!-- 图片预览 -->
        <n-card class="image-card">
          <template #header>
            <div class="card-header">
              <span>原始图片</span>
              <n-space>
                <n-button size="small" @click="resetUpload">
                  <template #icon>
                    <n-icon :component="Refresh" />
                  </template>
                  重新上传
                </n-button>
              </n-space>
            </div>
          </template>
          <div class="image-preview">
            <img :src="imagePreview" alt="上传的图片" class="preview-image" />
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <!-- OCR 识别结果 -->
        <n-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>识别结果</span>
              <n-space>
                <n-button
                  v-if="!isScanning"
                  size="small"
                  type="primary"
                  @click="handleScan"
                >
                  <template #icon>
                    <n-icon :component="ScanOutline" />
                  </template>
                  重新识别
                </n-button>
              </n-space>
            </div>
          </template>

          <div v-if="isScanning" class="scanning">
            <n-spin size="large" description="正在识别中..." />
          </div>

          <n-form
            v-else-if="ocrResult"
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-placement="top"
          >
            <n-form-item label="交易类型" path="type">
              <n-radio-group v-model:value="formData.type">
                <n-radio value="expense">支出</n-radio>
                <n-radio value="income">收入</n-radio>
              </n-radio-group>
            </n-form-item>

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

            <n-form-item label="分类" path="categoryId">
              <n-select
                v-model:value="formData.categoryId"
                :options="categoryOptions"
                placeholder="请选择分类"
              />
            </n-form-item>

            <n-form-item label="日期" path="date">
              <n-date-picker
                v-model:value="formData.date"
                type="datetime"
                placeholder="选择日期"
                style="width: 100%"
              />
            </n-form-item>

            <n-form-item label="商户/备注" path="note">
              <n-input
                v-model:value="formData.note"
                type="textarea"
                placeholder="请输入备注"
                :rows="3"
                show-count
                :maxlength="200"
              />
            </n-form-item>

            <!-- OCR 原始文本 -->
            <n-form-item label="OCR 识别文本">
              <n-input
                v-model:value="ocrResult.text"
                type="textarea"
                :rows="4"
                placeholder="识别的原始文本"
              />
            </n-form-item>
          </n-form>

          <n-empty
            v-else-if="scanError"
            description="识别失败，请重试或手动输入"
            size="large"
          >
            <template #extra>
              <n-button type="primary" @click="handleScan">重新识别</n-button>
            </template>
          </n-empty>

          <n-empty
            v-else
            description='点击"重新识别"开始 OCR 识别'
            size="large"
          />
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 提交按钮 -->
    <div class="submit-bar" v-if="ocrResult && !isScanning">
      <n-space style="width: 100%; justify-content: flex-end;">
        <n-button @click="resetUpload">取消</n-button>
        <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
          <template #icon>
            <n-icon :component="CheckmarkCircle" />
          </template>
          确认记账
        </n-button>
      </n-space>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import {
  Scan, Camera, Refresh, ImageOutline, ScanOutline,
  CheckmarkCircle, Create, Trash
} from '@vicons/ionicons5'
import { useOcrStore } from '@/stores/ocr'
import { createTransaction } from '@/api/transaction'
import { getCategoryList } from '@/api/category'
import { getAccountList } from '@/api/account'

const message = useMessage()
const ocrStore = useOcrStore()

const hasImage = ref(false)
const imageFile = ref(null)
const imagePreview = ref('')
const isScanning = computed(() => ocrStore.isScanning)
const scanError = computed(() => ocrStore.error)
const ocrResult = computed(() => ocrStore.scanResult)
const formRef = ref(null)
const submitting = ref(false)

const categories = ref({ expense: [], income: [] })
const accounts = ref([])

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
  await loadAccounts()
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

const loadAccounts = async () => {
  try {
    const data = await getAccountList()
    accounts.value = data.list || data || []
  } catch (error) {
    console.error('获取账户失败:', error)
  }
}

const handleImageUpload = ({ file }) => {
  imageFile.value = file.file
  hasImage.value = true
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file.file)
  
  // 自动开始识别
  handleScan()
}

const handleScan = async () => {
  if (!imageFile.value) return
  
  try {
    await ocrStore.scanImage(imageFile.value)
    
    // Mock 识别结果 - 实际项目中会调用 OCR API
    if (ocrResult.value) {
      // 尝试从 OCR 结果中提取金额和商户
      const extractedData = extractFromOcr(ocrResult.value.text)
      if (extractedData.amount) {
        formData.amount = extractedData.amount
      }
      if (extractedData.note) {
        formData.note = extractedData.note
      }
      if (extractedData.date) {
        formData.date = extractedData.date
      }
    }
  } catch (error) {
    message.error('OCR 识别失败，请手动输入')
  }
}

const extractFromOcr = (text) => {
  // 简单的正则提取，实际项目需要更智能的解析
  const result = {}
  
  // 提取金额
  const amountMatch = text.match(/[\d,]+\.?\d*/)?.[0]
  if (amountMatch) {
    result.amount = parseFloat(amountMatch.replace(/,/g, ''))
  }
  
  // 提取日期
  const dateMatch = text.match(/\d{4}[-/]\d{1,2}[-/]\d{1,2}/)?.[0]
  if (dateMatch) {
    result.date = new Date(dateMatch).getTime()
  }
  
  // 提取商户（第一行非数字文本）
  const lines = text.split('\n').filter(line => line.trim() && !/^\d/.test(line))
  if (lines.length > 0) {
    result.note = lines[0].trim()
  }
  
  return result
}

const resetUpload = () => {
  hasImage.value = false
  imageFile.value = null
  imagePreview.value = ''
  ocrStore.clearResult()
  formData.amount = null
  formData.categoryId = null
  formData.note = ''
  formData.date = Date.now()
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await createTransaction({
      ...formData,
      date: new Date(formData.date).toISOString().split('T')[0],
      images: imagePreview.value ? [imagePreview.value] : []
    })

    message.success('记账成功！')
    resetUpload()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.ocr-page {
  max-width: 1200px;
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

.upload-card {
  margin-bottom: 16px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 20px;
  border: 2px dashed #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.upload-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f3ff;
  border-radius: 50%;
}

.upload-text {
  text-align: center;
}

.upload-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.upload-desc {
  font-size: 13px;
  color: #6b7280;
}

.image-card,
.result-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  background: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.scanning {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.submit-bar {
  margin-top: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.n-card__content) {
  padding: 16px 20px;
}
</style>
