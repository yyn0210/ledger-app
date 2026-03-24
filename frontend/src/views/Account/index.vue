<template>
  <div class="account-management">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Wallet" size="28" color="#3385ff" />
        账户管理
      </h1>
      <n-space>
        <n-button @click="toggleHideBalance">
          <template #icon>
            <n-icon :component="hideBalance ? EyeOff : Eye" />
          </template>
          {{ hideBalance ? '显示余额' : '隐藏余额' }}
        </n-button>
        <n-button type="primary" @click="showCreateModal = true">
          <template #icon>
            <n-icon :component="AddCircle" />
          </template>
          新建账户
        </n-button>
      </n-space>
    </div>

    <!-- 账户概览 -->
    <n-card class="overview-card">
      <n-grid :cols="3" :x-gap="16">
        <n-grid-item>
          <div class="overview-item">
            <div class="overview-label">账户总数</div>
            <div class="overview-value">{{ accounts.length }}</div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="overview-item">
            <div class="overview-label">总资产</div>
            <div class="overview-value asset">
              {{ hideBalance ? '***' : formatMoney(totalAssets) }}
            </div>
          </div>
        </n-grid-item>
        <n-grid-item>
          <div class="overview-item">
            <div class="overview-label">总负债</div>
            <div class="overview-value liability">
              {{ hideBalance ? '***' : formatMoney(totalLiabilities) }}
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 账户列表 -->
    <div class="account-list">
      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="account in accounts" :key="account.id">
          <n-card class="account-card" :class="account.type">
            <template #header>
              <div class="account-header">
                <div class="account-icon" :style="{ backgroundColor: account.color }">
                  <n-icon :component="getIconComponent(account.icon)" size="28" color="#fff" />
                </div>
                <div class="account-actions">
                  <n-button quaternary circle size="small" @click="editAccount(account)">
                    <template #icon>
                      <n-icon :component="Create" />
                    </template>
                  </n-button>
                  <n-button quaternary circle size="small" @click="confirmDelete(account)">
                    <template #icon>
                      <n-icon :component="Trash" />
                    </template>
                  </n-button>
                </div>
              </div>
            </template>

            <div class="account-content">
              <h3 class="account-name">{{ account.name }}</h3>
              <p class="account-type">{{ getTypeLabel(account.type) }}</p>
              <p v-if="account.note" class="account-note">{{ account.note }}</p>
            </div>

            <template #footer>
              <div class="account-footer">
                <span class="account-label">余额</span>
                <span class="account-balance" :class="{ hidden: hideBalance }">
                  {{ hideBalance ? '***' : formatMoney(account.balance) }}
                </span>
              </div>
              <div v-if="account.type === 'credit'" class="credit-info">
                <span class="credit-label">额度</span>
                <span class="credit-limit">{{ hideBalance ? '***' : formatMoney(account.creditLimit) }}</span>
              </div>
            </template>
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 空状态 -->
      <n-empty
        v-if="accounts.length === 0"
        description="还没有账户，添加一个开始记账吧"
        size="large"
      />
    </div>

    <!-- 新建/编辑账户弹窗 -->
    <n-modal
      v-model:show="showCreateModal"
      :title="editingAccount ? '编辑账户' : '新建账户'"
      preset="dialog"
      :show-icon="false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="top"
      >
        <n-form-item label="账户类型" path="type">
          <n-select
            v-model:value="formData.type"
            :options="accountTypeOptions"
            placeholder="请选择账户类型"
          />
        </n-form-item>

        <n-form-item label="账户名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入账户名称"
            :maxlength="20"
            show-count
          />
        </n-form-item>

        <n-form-item label="初始余额" path="balance">
          <n-input-number
            v-model:value="formData.balance"
            placeholder="请输入初始余额"
            :min="-999999999"
            :max="999999999"
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item
          v-if="formData.type === 'credit'"
          label="信用额度"
          path="creditLimit"
        >
          <n-input-number
            v-model:value="formData.creditLimit"
            placeholder="请输入信用额度"
            :min="0"
            :max="999999999"
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item
          v-if="formData.type === 'credit'"
          label="账单日"
          path="billDay"
        >
          <n-select
            v-model:value="formData.billDay"
            :options="dayOptions"
            placeholder="请选择账单日"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="请输入备注（可选）"
            :maxlength="100"
            show-count
            :rows="3"
          />
        </n-form-item>
      </n-form>

      <template #action>
        <n-button @click="showCreateModal = false">取消</n-button>
        <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Wallet, AddCircle, Create, Trash, Eye, EyeOff,
  Cash, Card, LogoApple, LogoAlipay, WalletOutline
} from '@vicons/ionicons5'
import { getAccountList, createAccount, updateAccount, deleteAccount } from '@/api/account'
import { useAccountStore } from '@/stores/account'

const message = useMessage()
const dialog = useDialog()
const accountStore = useAccountStore()

const accounts = ref([])
const showCreateModal = ref(false)
const editingAccount = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const hideBalance = computed(() => accountStore.hideBalance)

const formData = reactive({
  type: 'cash',
  name: '',
  balance: 0,
  creditLimit: null,
  billDay: null,
  note: ''
})

const formRules = {
  name: {
    required: true,
    message: '请输入账户名称',
    trigger: 'blur'
  },
  balance: {
    required: true,
    message: '请输入初始余额',
    trigger: 'blur'
  }
}

const accountTypeOptions = [
  { label: '现金', value: 'cash', icon: 'Cash' },
  { label: '银行卡', value: 'bank', icon: 'Card' },
  { label: '信用卡', value: 'credit', icon: 'Card' },
  { label: '支付宝', value: 'alipay', icon: 'LogoAlipay' },
  { label: '微信', value: 'wechat', icon: 'LogoApple' },
  { label: '其他', value: 'other', icon: 'WalletOutline' }
]

const dayOptions = Array.from({ length: 31 }, (_, i) => ({
  label: `每月${i + 1}日`,
  value: i + 1
}))

onMounted(async () => {
  accountStore.initHideBalance()
  await loadAccounts()
})

const loadAccounts = async () => {
  try {
    const data = await getAccountList()
    accounts.value = data.list || data || []
  } catch (error) {
    console.error('获取账户列表失败:', error)
  }
}

const toggleHideBalance = () => {
  accountStore.toggleHideBalance()
}

const getIconComponent = (iconName) => {
  const iconMap = {
    'Cash': Cash,
    'Card': Card,
    'LogoApple': LogoApple,
    'LogoAlipay': LogoAlipay,
    'WalletOutline': WalletOutline
  }
  return iconMap[iconName] || Wallet
}

const getTypeLabel = (type) => {
  const typeMap = {
    'cash': '现金',
    'bank': '银行卡',
    'credit': '信用卡',
    'alipay': '支付宝',
    'wechat': '微信',
    'other': '其他'
  }
  return typeMap[type] || type
}

const editAccount = (account) => {
  editingAccount.value = account
  formData.type = account.type
  formData.name = account.name
  formData.balance = account.balance
  formData.creditLimit = account.creditLimit
  formData.billDay = account.billDay
  formData.note = account.note || ''
  showCreateModal.value = true
}

const confirmDelete = (account) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除账户"${account.name}"吗？删除后该账户下的所有交易记录将受到影响，此操作不可恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAccount(account.id)
        message.success('账户已删除')
        await loadAccounts()
      } catch (error) {
        console.error('删除账户失败:', error)
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (editingAccount.value) {
      await updateAccount(editingAccount.value.id, formData)
      message.success('账户已更新')
    } else {
      await createAccount(formData)
      message.success('账户已创建')
    }

    showCreateModal.value = false
    editingAccount.value = null
    resetForm()
    await loadAccounts()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.type = 'cash'
  formData.name = ''
  formData.balance = 0
  formData.creditLimit = null
  formData.billDay = null
  formData.note = ''
  formRef.value?.restoreValidation()
}

const formatMoney = (value) => {
  if (value === null || value === undefined) return '¥0.00'
  const num = Number(value)
  return num.toLocaleString('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  })
}

const totalAssets = computed(() => {
  return accounts.value
    .filter(a => a.type !== 'credit')
    .reduce((sum, a) => sum + (a.balance || 0), 0)
})

const totalLiabilities = computed(() => {
  return accounts.value
    .filter(a => a.type === 'credit')
    .reduce((sum, a) => sum + Math.abs(a.balance || 0), 0)
})
</script>

<style scoped>
.account-management {
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

.overview-card {
  margin-bottom: 20px;
}

.overview-item {
  text-align: center;
  padding: 12px 0;
}

.overview-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;

  &.asset {
    color: #52c41a;
  }

  &.liability {
    color: #ff6b6b;
  }
}

.account-list {
  margin-top: 20px;
}

.account-card {
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.account-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.account-card:hover .account-actions {
  opacity: 1;
}

.account-content {
  .account-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 4px 0;
  }

  .account-type {
    font-size: 13px;
    color: #999;
    margin: 0 0 8px 0;
  }

  .account-note {
    font-size: 13px;
    color: #666;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.account-footer {
  display: flex;
  justify-content: space-between;
  align-items: baseline;

  .account-label {
    font-size: 13px;
    color: #999;
  }

  .account-balance {
    font-size: 18px;
    font-weight: 600;

    &.hidden {
      color: #999;
    }
  }
}

.credit-info {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;

  .credit-label {
    font-size: 13px;
    color: #999;
  }

  .credit-limit {
    font-size: 14px;
    color: #ff6b6b;
  }
}

:deep(.n-card__content) {
  padding: 16px;
}

:deep(.n-card__footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
