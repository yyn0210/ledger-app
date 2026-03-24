<template>
  <div class="screenshot-page">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Images" size="28" color="#06b6d4" />
        截图记账
      </h1>
      <n-button type="primary" @click="triggerUpload">
        <template #icon>
          <n-icon :component="Upload" />
        </template>
        上传截图
      </n-button>
    </div>

    <!-- 上传区域 -->
    <n-card class="upload-card" v-if="images.length === 0">
      <n-upload
        :max="10"
        multiple
        accept="image/*"
        :file-list="uploadFileList"
        @change="handleUploadChange"
        @finish="handleUploadFinish"
      >
        <div class="upload-area">
          <div class="upload-icon">
            <n-icon :component="Images" size="48" color="#06b6d4" />
          </div>
          <div class="upload-text">
            <div class="upload-title">点击或拖拽上传截图</div>
            <div class="upload-desc">支持微信/支付宝账单截图，最多 10 张</div>
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

    <!-- 图片列表和识别结果 -->
    <n-grid :cols="2" :x-gap="16" v-else>
      <n-grid-item>
        <!-- 图片列表 -->
        <n-card class="images-card">
          <template #header>
            <div class="card-header">
              <span>已上传截图（{{ images.length }}张）</span>
              <n-space>
                <n-button size="small" @click="resetUpload">
                  <template #icon>
                    <n-icon :component="Refresh" />
                  </template>
                  重新上传
                </n-button>
                <n-button
                  size="small"
                  type="primary"
                  @click="scanAllImages"
                  :loading="isScanning"
                >
                  <template #icon>
                    <n-icon :component="Scan" />
                  </template>
                  批量识别
                </n-button>
              </n-space>
            </div>
          </template>
          <div class="image-list">
            <div
              v-for="(img, index) in images"
              :key="index"
              class="image-item"
              :class="{ active: currentIndex === index }"
              @click="selectImage(index)"
            >
              <img :src="img.preview" :alt="'截图' + (index + 1)" class="image-thumb" />
              <div class="image-status">
                <n-tag
                  :type="img.scanned ? 'success' : 'default'"
                  size="small"
                  bordered
                >
                  {{ img.scanned ? '已识别' : '待识别' }}
                </n-tag>
              </div>
            </div>
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <!-- 识别结果 -->
        <n-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>识别结果（{{ currentIndex + 1 }}/{{ images.length }}）</span>
              <n-text depth="3" v-if="currentImage?.scanned">
                已识别 {{ extractedData.length }} 条交易
              </n-text>
            </div>
          </template>

          <div v-if="isScanning" class="scanning">
            <n-spin size="large" description="正在批量识别中..." />
          </div>

          <div v-else-if="currentImage && currentImage.scanned" class="scan-results">
            <!-- 交易列表 -->
            <div class="transaction-list">
              <div
                v-for="(item, idx) in extractedData"
                :key="idx"
                class="transaction-item"
              >
                <div class="transaction-header">
                  <span class="transaction-date">{{ item.date }}</span>
                  <n-tag
                    :type="item.type === 'income' ? 'success' : 'error'"
                    size="small"
                  >
                    {{ item.type === 'income' ? '收入' : '支出' }}
                  </n-tag>
                </div>
                <div class="transaction-body">
                  <span class="transaction-note">{{ item.note || '无备注' }}</span>
                  <span
                    class="transaction-amount"
                    :class="item.type"
                  >
                    {{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
                  </span>
                </div>
                <div class="transaction-actions">
                  <n-button
                    size="small"
                    type="primary"
                    @click="confirmTransaction(item)"
                  >
                    确认导入
                  </n-button>
                </div>
              </div>
            </div>

            <!-- 批量操作 -->
            <div class="batch-actions">
              <n-button
                type="primary"
                size="large"
                @click="confirmAllTransactions"
                :loading="submitting"
              >
                <template #icon>
                  <n-icon :component="CheckmarkCircle" />
                </template>
                全部导入（{{ extractedData.length }}条）
              </n-button>
            </div>
          </div>

          <n-empty
            v-else
            description='点击"批量识别"开始 OCR 识别'
            size="large"
            style="padding: 40px 0"
          />
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  Images, CloudUploadOutline as Upload, Refresh, Scan, ImageOutline,
  CheckmarkCircle, Create, Trash
} from '@vicons/ionicons5'
import { useOcrStore } from '@/stores/ocr'
import { createTransaction } from '@/api/transaction'

const message = useMessage()
const ocrStore = useOcrStore()

const images = ref([])
const uploadFileList = ref([])
const currentIndex = ref(0)
const isScanning = ref(false)
const submitting = ref(false)
const extractedData = ref([])

const currentImage = computed(() => images.value[currentIndex.value])

onMounted(() => {
  // 初始化
})

// 触发上传
const triggerUpload = () => {
  document.querySelector('input[type="file"]')?.click()
}

// 处理上传变化
const handleUploadChange = ({ fileList }) => {
  uploadFileList.value = fileList
  images.value = fileList.map(file => ({
    file: file.file,
    name: file.name,
    preview: file.previewUrl,
    scanned: false,
    result: null
  }))
}

// 上传完成
const handleUploadFinish = () => {
  if (images.value.length > 0) {
    message.success(`已上传 ${images.value.length} 张截图`)
    // 自动开始识别
    scanAllImages()
  }
}

// 选择图片
const selectImage = (index) => {
  currentIndex.value = index
  if (images.value[index].scanned) {
    extractedData.value = images.value[index].result || []
  }
}

// 批量识别
const scanAllImages = async () => {
  if (images.value.length === 0) return
  
  isScanning.value = true
  
  try {
    // 模拟批量 OCR 识别
    for (let i = 0; i < images.value.length; i++) {
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Mock 识别结果
      const mockResult = generateMockTransactions()
      images.value[i].scanned = true
      images.value[i].result = mockResult
      
      if (i === currentIndex.value) {
        extractedData.value = mockResult
      }
    }
    
    message.success(`识别完成！共识别 ${extractedData.value.length} 条交易`)
  } catch (error) {
    message.error('识别失败，请重试')
  } finally {
    isScanning.value = false
  }
}

// 生成 Mock 交易数据
const generateMockTransactions = () => {
  const count = Math.floor(Math.random() * 5) + 3 // 3-7 条
  const transactions = []
  
  for (let i = 0; i < count; i++) {
    transactions.push({
      id: Date.now() + i,
      type: Math.random() > 0.8 ? 'income' : 'expense',
      amount: (Math.random() * 500 + 10).toFixed(2),
      date: `2026-03-${String(Math.floor(Math.random() * 24) + 1).padStart(2, '0')}`,
      note: ['餐饮', '交通', '购物', '娱乐', '工资', '其他'][Math.floor(Math.random() * 6)],
      categoryId: Math.floor(Math.random() * 6) + 1,
      imageIndex: currentIndex.value
    })
  }
  
  return transactions
}

// 确认单条交易
const confirmTransaction = async (item) => {
  try {
    submitting.value = true
    await createTransaction(item)
    message.success('导入成功！')
    
    // 移除已导入的交易
    extractedData.value = extractedData.value.filter(t => t.id !== item.id)
  } catch (error) {
    message.error('导入失败')
  } finally {
    submitting.value = false
  }
}

// 批量导入
const confirmAllTransactions = async () => {
  if (extractedData.value.length === 0) {
    message.warning('没有可导入的交易')
    return
  }
  
  try {
    submitting.value = true
    
    for (const item of extractedData.value) {
      await createTransaction(item)
    }
    
    message.success(`成功导入 ${extractedData.value.length} 条交易！`)
    resetUpload()
  } catch (error) {
    message.error('批量导入失败')
  } finally {
    submitting.value = false
  }
}

// 重置上传
const resetUpload = () => {
  images.value = []
  uploadFileList.value = []
  currentIndex.value = 0
  extractedData.value = []
  isScanning.value = false
}
</script>

<style scoped>
.screenshot-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  border-color: #06b6d4;
  background: #f0f9ff;
}

.upload-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f9ff;
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

.images-card,
.result-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.image-item:hover,
.image-item.active {
  border-color: #06b6d4;
}

.image-thumb {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.image-status {
  position: absolute;
  top: 8px;
  right: 8px;
}

.scanning {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.scan-results {
  padding: 10px 0;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.transaction-item {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  transition: all 0.3s;
}

.transaction-item:hover {
  background: #f3f4f6;
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.transaction-date {
  font-size: 13px;
  color: #6b7280;
}

.transaction-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.transaction-note {
  font-size: 14px;
  color: #1f2937;
}

.transaction-amount {
  font-size: 16px;
  font-weight: 600;

  &.income {
    color: #10b981;
  }

  &.expense {
    color: #ef4444;
  }
}

.transaction-actions {
  display: flex;
  justify-content: flex-end;
}

.batch-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

:deep(.n-card__content) {
  padding: 16px 20px;
}
</style>
