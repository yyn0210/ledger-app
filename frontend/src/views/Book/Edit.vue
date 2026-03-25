<template>
  <div class="book-edit">
    <n-card style="max-width: 600px; margin: 0 auto">
      <template #header>
        <n-space align="center">
          <n-button quaternary circle @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
          </n-button>
          <h2 style="margin: 0">✏️ 编辑账本</h2>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-placement="top"
        >
          <n-form-item label="账本名称" path="name">
            <n-input
              v-model:value="formData.name"
              placeholder="请输入账本名称"
              maxlength="50"
              show-count
            />
          </n-form-item>

          <n-form-item label="账本描述" path="description">
            <n-input
              v-model:value="formData.description"
              type="textarea"
              placeholder="简单描述这个账本的用途"
              maxlength="200"
              show-count
              :rows="3"
            />
          </n-form-item>

          <n-form-item label="封面图 URL" path="coverImage">
            <n-input
              v-model:value="formData.coverImage"
              placeholder="输入封面图片链接"
            />
          </n-form-item>

          <n-form-item label="隐私设置" path="privacy">
            <n-radio-group v-model:value="formData.privacy">
              <n-space>
                <n-radio value="private">
                  🔒 私有（仅自己可见）
                </n-radio>
                <n-radio value="public">
                  🌍 公开（其他人可查看）
                </n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>

          <n-form-item label="统计信息">
            <n-space>
              <n-tag type="info">
                📅 创建于 {{ formatDate(book?.createdAt) }}
              </n-tag>
              <n-tag type="success">
                💰 {{ book?.transactionCount || 0 }} 笔交易
              </n-tag>
            </n-space>
          </n-form-item>

          <n-divider />

          <n-space justify="end" style="width: 100%">
            <n-button @click="handleBack">取消</n-button>
            <n-button type="primary" @click="handleSubmit" :loading="submitting">
              保存修改
            </n-button>
          </n-space>
        </n-form>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useBookStore } from '@/stores/book'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const bookStore = useBookStore()

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const bookId = computed(() => route.params.id)
const book = computed(() => bookStore.currentBook)

const formData = reactive({
  name: '',
  description: '',
  coverImage: '',
  privacy: 'private'
})

const formRules = {
  name: [
    {
      required: true,
      message: '请输入账本名称',
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: '名称长度 1-50 个字符',
      trigger: 'blur'
    }
  ],
  description: [
    {
      max: 200,
      message: '描述不超过 200 个字符',
      trigger: 'blur'
    }
  ]
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 加载账本数据
const loadBook = async () => {
  loading.value = true
  try {
    await bookStore.fetchCurrentBook(bookId.value)
    const data = bookStore.currentBook
    if (data) {
      formData.name = data.name || ''
      formData.description = data.description || ''
      formData.coverImage = data.coverImage || ''
      formData.privacy = data.privacy || 'private'
    }
  } catch (error) {
    message.error('加载账本失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await bookStore.updateBook(bookId.value, {
      name: formData.name.trim(),
      description: formData.description.trim(),
      coverImage: formData.coverImage.trim(),
      privacy: formData.privacy
    })

    message.success('账本已更新！')
    router.push('/book')
  } catch (error) {
    if (error?.errors) {
      return
    }
    message.error('更新失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadBook()
})
</script>

<style scoped>
.book-edit {
  padding: 20px;
}
</style>
