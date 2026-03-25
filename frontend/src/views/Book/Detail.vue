<template>
  <div class="book-detail">
    <n-spin :show="loading">
      <template v-if="book">
        <!-- 头部信息 -->
        <n-card>
          <n-space justify="space-between" align="center">
            <n-space>
              <n-button quaternary circle @click="handleBack">
                <template #icon>
                  <n-icon :component="ArrowBackOutline" />
                </template>
              </n-button>
              <div>
                <h2 style="margin: 0 0 8px 0">📚 {{ book.name }}</h2>
                <n-space>
                  <n-tag :type="book.privacy === 'public' ? 'success' : 'default'">
                    {{ book.privacy === 'public' ? '公开' : '私有' }}
                  </n-tag>
                  <n-tag type="info">
                    📅 创建于 {{ formatDate(book.createdAt) }}
                  </n-tag>
                </n-space>
              </div>
            </n-space>
            <n-space>
              <n-button @click="handleEdit">
                <template #icon>
                  <n-icon :component="CreateOutline" />
                </template>
                编辑
              </n-button>
              <n-button type="error" secondary @click="handleDelete">
                <template #icon>
                  <n-icon :component="TrashOutline" />
                </template>
                删除
              </n-button>
            </n-space>
          </n-space>
        </n-card>

        <!-- 描述 -->
        <n-card title="📝 账本描述" style="margin-top: 20px">
          <n-text depth="2">
            {{ book.description || '暂无描述' }}
          </n-text>
        </n-card>

        <!-- 统计信息 -->
        <n-card title="📊 统计概览" style="margin-top: 20px">
          <n-grid :cols="4" :x-gap="20">
            <n-grid-item>
              <n-statistic label="总交易数">
                {{ book.transactionCount || 0 }}
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="总收入">
                <n-text type="success">
                  ¥{{ formatAmount(book.totalIncome || 0) }}
                </n-text>
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="总支出">
                <n-text type="error">
                  ¥{{ formatAmount(book.totalExpense || 0) }}
                </n-text>
              </n-statistic>
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="结余">
                <n-text :type="book.balance >= 0 ? 'success' : 'error'">
                  ¥{{ formatAmount(book.balance || 0) }}
                </n-text>
              </n-statistic>
            </n-grid-item>
          </n-grid>
        </n-card>

        <!-- 快捷操作 -->
        <n-card title="⚡ 快捷操作" style="margin-top: 20px">
          <n-space>
            <n-button type="primary" @click="goToTransaction">
              <template #icon>
                <n-icon :component="DocumentTextOutline" />
              </template>
              记一笔
            </n-button>
            <n-button @click="goToStatistics">
              <template #icon>
                <n-icon :component="StatsChartOutline" />
              </template>
              查看统计
            </n-button>
          </n-space>
        </n-card>
      </template>

      <n-empty
        v-else-if="!loading"
        description="账本不存在或已被删除"
        style="padding: 60px 0"
      >
        <template #extra>
          <n-button @click="handleBack">返回账本列表</n-button>
        </template>
      </n-empty>
    </n-spin>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import {
  ArrowBackOutline,
  CreateOutline,
  TrashOutline,
  DocumentTextOutline,
  StatsChartOutline
} from '@vicons/ionicons5'
import { useBookStore } from '@/stores/book'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const bookStore = useBookStore()

const bookId = computed(() => route.params.id)
const book = computed(() => bookStore.currentBook)
const loading = computed(() => bookStore.loading)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 格式化金额
const formatAmount = (amount) => {
  return Number(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 编辑账本
const handleEdit = () => {
  router.push(`/book/${bookId.value}/edit`)
}

// 删除账本
const handleDelete = () => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除账本「${book.value?.name}」吗？删除后无法恢复！`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await bookStore.deleteBook(bookId.value)
        message.success('账本已删除')
        router.push('/book')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 跳转到记账页面
const goToTransaction = () => {
  router.push({
    path: '/transaction',
    query: { bookId: bookId.value }
  })
}

// 跳转到统计页面
const goToStatistics = () => {
  router.push({
    path: '/statistics',
    query: { bookId: bookId.value }
  })
}

// 加载账本数据
onMounted(() => {
  bookStore.fetchCurrentBook(bookId.value)
})
</script>

<style scoped>
.book-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
