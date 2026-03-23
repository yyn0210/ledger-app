<template>
  <div class="account-management">
    <div class="page-header">
      <h1 class="page-title">
        <n-icon :component="Wallet" size="28" color="#3385ff" />
        账户管理
      </h1>
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <n-icon :component="AddCircle" />
        </template>
        新建账户
      </n-button>
    </div>

    <!-- 账户总览卡片 -->
    <div class="overview-cards">
      <n-card class="overview-card">
        <div class="card-header">
          <n-icon :component="TrendingUp" size="20" color="#52c41a" />
          <span class="card-label">总资产</span>
        </div>
        <div class="card-value">¥{{ totalAssets }}</div>
      </n-card>
      <n-card class="overview-card">
        <div class="card-header">
          <n-icon :component="TrendingDown" size="20" color="#ff6b6b" />
          <span class="card-label">总负债</span>
        </div>
        <div class="card-value debt">¥{{ totalDebt }}</div>
      </n-card>
      <n-card class="overview-card">
        <div class="card-header">
          <n-icon :component="Wallet" size="20" color="#3385ff" />
          <span class="card-label">净资产</span>
        </div>
        <div class="card-value net">¥{{ netAssets }}</div>
      </n-card>
    </div>

    <!-- 账户列表 -->
    <div class="account-list">
      <n-card class="account-card" v-for="account in accounts" :key="account.id">
        <div class="account-content">
          <div class="account-icon" :style="{ backgroundColor: account.color }">
            <n-icon :component="account.icon" size="32" color="#fff" />
          </div>
          <div class="account-info">
            <div class="account-name">{{ account.name }}</div>
            <div class="account-type">{{ account.typeName }}</div>
            <div v-if="account.note" class="account-note">{{ account.note }}</div>
          </div>
          <div class="account-balance" :class="{ debt: account.balance < 0 }">
            <div class="balance-label">余额</div>
            <div class="balance-value">¥{{ formatBalance(account.balance) }}</div>
          </div>
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
      </n-card>
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
        <n-form-item label="账户名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入账户名称"
            :maxlength="20"
            show-count
          />
        </n-form-item>

        <n-form-item label="账户类型" path="type">
          <n-select
            v-model:value="formData.type"
            :options="accountTypeOptions"
            placeholder="请选择账户类型"
          />
        </n-form-item>

        <n-form-item label="初始余额" path="balance">
          <n-input-number
            v-model:value="formData.balance"
            :min="-999999999"
            :max="999999999"
            :precision="2"
            placeholder="请输入初始余额"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  Wallet, AddCircle, Create, Trash, TrendingUp, TrendingDown,
  Cash, Card, LogoBitcoin, LogoApple, Mail, ellipsisHorizontal
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

const showCreateModal = ref(false)
const editingAccount = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const accountTypeOptions = [
  { label: '现金', value: 'cash', icon: Cash, color: '#52c41a' },
  { label: '银行卡', value: 'bank', icon: Card, color: '#3385ff' },
  { label: '信用卡', value: 'credit', icon: Card, color: '#ff6b6b' },
  { label: '支付宝', value: 'alipay', icon: LogoApple, color: '#1677ff' },
  { label: '微信', value: 'wechat', icon: LogoApple, color: '#07c160' },
  { label: '其他', value: 'other', icon: ellipsisHorizontal, color: '#95a5a6' }
]

const iconMap = {
  Cash, Card, LogoBitcoin, LogoApple, Mail, ellipsisHorizontal
}

const formData = reactive({
  name: '',
  type: 'cash',
  balance: 0,
  note: ''
})

const formRules = {
  name: {
    required: true,
    message: '请输入账户名称',
    trigger: 'blur'
  },
  type: {
    required: true,
    message: '请选择账户类型',
    trigger: 'change'
  }
}

// 模拟数据
const accounts = ref([
  { id: 1, name: '钱包', type: 'cash', typeName: '现金', color: '#52c41a', icon: Cash, balance: 1500.00, note: '' },
  { id: 2, name: '招商银行储蓄卡', type: 'bank', typeName: '银行卡', color: '#3385ff', icon: Card, balance: 25680.50, note: '尾号 8888' },
  { id: 3, name: '支付宝', type: 'alipay', typeName: '支付宝', color: '#1677ff', icon: LogoApple, balance: 8920.30, note: '' },
  { id: 4, name: '微信零钱', type: 'wechat', typeName: '微信', color: '#07c160', icon: LogoApple, balance: 2350.80, note: '' },
  { id: 5, name: '信用卡', type: 'credit', typeName: '信用卡', color: '#ff6b6b', icon: Card, balance: -3200.00, note: '账单日每月 15 号' }
])

const totalAssets = computed(() => {
  return accounts.value
    .filter(a => a.balance >= 0)
    .reduce((sum, a) => sum + a.balance, 0)
    .toFixed(2)
})

const totalDebt = computed(() => {
  return Math.abs(
    accounts.value
      .filter(a => a.balance < 0)
      .reduce((sum, a) => sum + a.balance, 0)
  ).toFixed(2)
})

const netAssets = computed(() => {
  return accounts.value
    .reduce((sum, a) => sum + a.balance, 0)
    .toFixed(2)
})

const formatBalance = (balance) => {
  return Math.abs(balance).toFixed(2)
}

const editAccount = (account) => {
  editingAccount.value = account
  formData.name = account.name
  formData.type = account.type
  formData.balance = account.balance
  formData.note = account.note || ''
  showCreateModal.value = true
}

const confirmDelete = (account) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除账户"${account.name}"吗？${account.balance !== 0 ? '该账户还有余额，删除后余额将清零。' : ''}删除账户不会影响历史交易记录。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const index = accounts.value.findIndex(a => a.id === account.id)
        if (index > -1) {
          accounts.value.splice(index, 1)
        }
        message.success('账户已删除')
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

    const typeInfo = accountTypeOptions.find(t => t.value === formData.type)

    if (editingAccount.value) {
      // 编辑
      const index = accounts.value.findIndex(a => a.id === editingAccount.value.id)
      if (index > -1) {
        accounts.value[index] = {
          ...accounts.value[index],
          name: formData.name,
          type: formData.type,
          typeName: typeInfo?.label,
          color: typeInfo?.color,
          icon: typeInfo?.icon,
          balance: formData.balance,
          note: formData.note
        }
      }
      message.success('账户已更新')
    } else {
      // 新建
      const newAccount = {
        id: Date.now(),
        name: formData.name,
        type: formData.type,
        typeName: typeInfo?.label,
        color: typeInfo?.color,
        icon: typeInfo?.icon,
        balance: formData.balance,
        note: formData.note,
        createdAt: new Date().toISOString()
      }
      accounts.value.push(newAccount)
      message.success('账户已创建')
    }

    showCreateModal.value = false
    editingAccount.value = null
    resetForm()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formData.name = ''
  formData.type = 'cash'
  formData.balance = 0
  formData.note = ''
  formRef.value?.restoreValidation()
}
</script>

<style scoped>
.account-management {
  max-width: 800px;
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

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }

  .card-label {
    font-size: 14px;
    color: #666;
  }

  .card-value {
    font-size: 28px;
    font-weight: 700;
    color: #333;

    &.debt {
      color: #ff6b6b;
    }

    &.net {
      color: #3385ff;
    }
  }
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.account-card {
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.account-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.account-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.account-info {
  flex: 1;
  min-width: 0;

  .account-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .account-type {
    font-size: 13px;
    color: #999;
    margin-bottom: 4px;
  }

  .account-note {
    font-size: 12px;
    color: #ccc;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.account-balance {
  text-align: right;
  flex-shrink: 0;

  &.debt .balance-value {
    color: #ff6b6b;
  }

  .balance-label {
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
  }

  .balance-value {
    font-size: 20px;
    font-weight: 700;
    color: #333;
  }
}

.account-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
}
</style>
