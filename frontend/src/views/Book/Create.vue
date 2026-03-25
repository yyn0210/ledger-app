<template>
  <div class="book-create">
    <n-card style="max-width: 600px; margin: 0 auto">
      <template #header>
        <n-space align="center">
          <n-button quaternary circle @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
          </n-button>
          <h2 style="margin: 0">📚 新建账本</h2>
        </n-space>
      </template>

      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="账本名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入账本名称（如：日常账本、旅行账本）"
            maxlength="50"
            show-count
          />
        </n-form-item>

        <n-form-item label="账本描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="简单描述这个账本的用途（可选）"
            maxlength="200"
            show-count
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="封面图 URL" path="coverImage">
          <n-input
            v-model:value="formData.coverImage"
            placeholder="输入封面图片链接（可选）"
          />
          <n-text depth="3" style="font-size: 12px; margin-top: 4px">
            可留空，系统将使用默认封面
          </n-text>
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

        <n-divider />

        <n-space justify="end" style="width: 100%">
          <n-button @click="handleBack">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            创建账本
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useBookStore } from '@/stores/book'

const router = useRouter()
const message = useMessage()
const bookStore = useBookStore()

const formRef = ref(null)
const submitting = ref(false)

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

// 返回上一页
const handleBack = () => {
  router.back()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await bookStore.createBook({
      name: formData.name.trim(),
      description: formData.description.trim(),
      coverImage: formData.coverImage.trim(),
      privacy: formData.privacy
    })

    message.success('账本创建成功！')
    router.push('/book')
  } catch (error) {
    if (error?.errors) {
      // 表单验证失败
      return
    }
    message.error('创建失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.book-create {
  padding: 20px;
}
</style>
