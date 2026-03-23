<template>
  <view class="account-management">
    <!-- 导航栏 -->
    <view class="navbar">
      <text class="navbar-title">账户管理</text>
      <view class="navbar-add" @click="showCreateModal = true">
        <u-icon name="plus" size="24" color="#fff"></u-icon>
      </view>
    </view>

    <!-- 账户总览 -->
    <view class="overview-section">
      <view class="overview-card">
        <view class="overview-item">
          <text class="overview-label">总资产</text>
          <text class="overview-value">¥{{ totalAssets }}</text>
        </view>
        <view class="overview-divider"></view>
        <view class="overview-item">
          <text class="overview-label">总负债</text>
          <text class="overview-value debt">¥{{ totalDebt }}</text>
        </view>
        <view class="overview-divider"></view>
        <view class="overview-item">
          <text class="overview-label">净资产</text>
          <text class="overview-value net">¥{{ netAssets }}</text>
        </view>
      </view>
    </view>

    <!-- 账户列表 -->
    <scroll-view class="account-list" scroll-y>
      <view class="account-card" v-for="account in accounts" :key="account.id">
        <view class="account-content">
          <view class="account-icon" :style="{ backgroundColor: account.color }">
            <u-icon :name="account.iconName" size="32" color="#fff"></u-icon>
          </view>
          <view class="account-info">
            <text class="account-name">{{ account.name }}</text>
            <text class="account-type">{{ account.typeName }}</text>
            <text v-if="account.note" class="account-note">{{ account.note }}</text>
          </view>
          <view class="account-balance" :class="{ debt: account.balance < 0 }">
            <text class="balance-label">余额</text>
            <text class="balance-value">¥{{ formatBalance(account.balance) }}</text>
          </view>
        </view>
        <view class="account-actions">
          <view class="action-btn" @click="editAccount(account)">
            <u-icon name="edit-pen" size="20" color="#666"></u-icon>
          </view>
          <view class="action-btn" @click="confirmDelete(account)">
            <u-icon name="close-circle" size="20" color="#ff6b6b"></u-icon>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="accounts.length === 0" class="empty-state">
        <u-icon name="wallet" size="80" color="#e0e0e0"></u-icon>
        <text class="empty-text">暂无账户</text>
        <text class="empty-desc">点击右上角添加账户</text>
      </view>
    </scroll-view>

    <!-- 新建/编辑账户弹窗 -->
    <u-popup v-model="showCreateModal" mode="center" :round="16">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">{{ editingAccount ? '编辑账户' : '新建账户' }}</text>
          <u-icon name="close" size="24" @click="closeModal"></u-icon>
        </view>

        <scroll-view class="modal-content" scroll-y>
          <view class="form-item">
            <text class="form-label">账户名称</text>
            <input
              class="form-input"
              v-model="formData.name"
              placeholder="请输入账户名称"
              :maxlength="20"
            />
          </view>

          <view class="form-item">
            <text class="form-label">账户类型</text>
            <view class="type-grid">
              <view
                v-for="type in accountTypes"
                :key="type.value"
                class="type-item"
                :class="{ active: formData.type === type.value }"
                @click="formData.type = type.value"
              >
                <view class="type-icon" :style="{ backgroundColor: type.color }">
                  <u-icon :name="type.icon" size="24" color="#fff"></u-icon>
                </view>
                <text class="type-name">{{ type.name }}</text>
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">初始余额</text>
            <view class="balance-input-wrapper">
              <text class="balance-symbol">¥</text>
              <input
                class="balance-input"
                v-model="formData.balance"
                type="number"
                placeholder="0.00"
              />
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">备注</text>
            <textarea
              class="form-textarea"
              v-model="formData.note"
              placeholder="请输入备注（可选）"
              :maxlength="100"
              :rows="3"
            />
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeModal">取消</button>
          <button class="modal-btn confirm" @click="handleSubmit">
            {{ submitLoading ? '提交中...' : '确定' }}
          </button>
        </view>
      </view>
    </u-popup>

    <!-- 删除确认弹窗 -->
    <u-modal
      v-model="showDeleteModal"
      title="确认删除"
      :content="deleteContent"
      :show-cancel-button="true"
      @confirm="handleDelete"
    ></u-modal>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const showCreateModal = ref(false)
const showDeleteModal = ref(false)
const editingAccount = ref(null)
const deleteContent = ref('')
const submitLoading = ref(false)

const accountTypes = [
  { name: '现金', value: 'cash', icon: 'yen', color: '#52c41a' },
  { name: '银行卡', value: 'bank', icon: 'bank', color: '#3385ff' },
  { name: '信用卡', value: 'credit', icon: 'card', color: '#ff6b6b' },
  { name: '支付宝', value: 'alipay', icon: 'alipay', color: '#1677ff' },
  { name: '微信', value: 'wechat', icon: 'wechat-fill', color: '#07c160' },
  { name: '其他', value: 'other', icon: 'more', color: '#95a5a6' }
]

const formData = reactive({
  name: '',
  type: 'cash',
  balance: '0',
  note: ''
})

const accounts = ref([
  { id: 1, name: '钱包', type: 'cash', typeName: '现金', color: '#52c41a', iconName: 'yen', balance: 1500.00, note: '' },
  { id: 2, name: '招商银行储蓄卡', type: 'bank', typeName: '银行卡', color: '#3385ff', iconName: 'bank', balance: 25680.50, note: '尾号 8888' },
  { id: 3, name: '支付宝', type: 'alipay', typeName: '支付宝', color: '#1677ff', iconName: 'alipay', balance: 8920.30, note: '' },
  { id: 4, name: '微信零钱', type: 'wechat', typeName: '微信', color: '#07c160', iconName: 'wechat-fill', balance: 2350.80, note: '' },
  { id: 5, name: '信用卡', type: 'credit', typeName: '信用卡', color: '#ff6b6b', iconName: 'card', balance: -3200.00, note: '账单日每月 15 号' }
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
  formData.balance = account.balance.toString()
  formData.note = account.note || ''
  showCreateModal.value = true
}

const confirmDelete = (account) => {
  deleteContent.value = `确定要删除账户"${account.name}"吗？${account.balance !== 0 ? '该账户还有余额，删除后余额将清零。' : ''}删除账户不会影响历史交易记录。`
  showDeleteModal.value = true
  editingAccount.value = account
}

const handleDelete = () => {
  if (!editingAccount.value) return
  
  const index = accounts.value.findIndex(a => a.id === editingAccount.value.id)
  if (index > -1) {
    accounts.value.splice(index, 1)
  }
  
  uni.showToast({
    title: '账户已删除',
    icon: 'success'
  })
}

const handleSubmit = () => {
  if (!formData.name.trim()) {
    uni.showToast({
      title: '请输入账户名称',
      icon: 'none'
    })
    return
  }

  const typeInfo = accountTypes.find(t => t.value === formData.type)
  const balance = parseFloat(formData.balance) || 0

  if (editingAccount.value) {
    // 编辑
    const index = accounts.value.findIndex(a => a.id === editingAccount.value.id)
    if (index > -1) {
      accounts.value[index] = {
        ...accounts.value[index],
        name: formData.name,
        type: formData.type,
        typeName: typeInfo?.name,
        color: typeInfo?.color,
        iconName: typeInfo?.icon,
        balance: balance,
        note: formData.note
      }
    }
    uni.showToast({
      title: '账户已更新',
      icon: 'success'
    })
  } else {
    // 新建
    const newAccount = {
      id: Date.now(),
      name: formData.name,
      type: formData.type,
      typeName: typeInfo?.name,
      color: typeInfo?.color,
      iconName: typeInfo?.icon,
      balance: balance,
      note: formData.note
    }
    accounts.value.push(newAccount)
    uni.showToast({
      title: '账户已创建',
      icon: 'success'
    })
  }

  closeModal()
}

const closeModal = () => {
  showCreateModal.value = false
  editingAccount.value = null
  formData.name = ''
  formData.type = 'cash'
  formData.balance = '0'
  formData.note = ''
}
</script>

<style lang="scss" scoped>
.account-management {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 16px;
  padding-top: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  &-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  &-add {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
  }
}

.overview-section {
  padding: 16px;
}

.overview-card {
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  color: #fff;

  .overview-item {
    flex: 1;
    text-align: center;

    .overview-label {
      display: block;
      font-size: 13px;
      opacity: 0.8;
      margin-bottom: 8px;
    }

    .overview-value {
      display: block;
      font-size: 20px;
      font-weight: 700;

      &.debt {
        color: #ffcc80;
      }

      &.net {
        color: #fff;
      }
    }
  }

  .overview-divider {
    width: 1px;
    background: rgba(255, 255, 255, 0.3);
    margin: 0 10px;
  }
}

.account-list {
  height: calc(100vh - 240px);
  padding: 16px;
}

.account-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.account-content {
  display: flex;
  align-items: center;
  gap: 12px;
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
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .account-type {
    display: block;
    font-size: 13px;
    color: #999;
    margin-bottom: 4px;
  }

  .account-note {
    display: block;
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
    display: block;
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
  }

  .balance-value {
    display: block;
    font-size: 18px;
    font-weight: 700;
    color: #333;
  }
}

.account-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .action-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6f7;
    border-radius: 50%;
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;

  .empty-text {
    display: block;
    font-size: 16px;
    color: #999;
    margin-top: 16px;
  }

  .empty-desc {
    display: block;
    font-size: 14px;
    color: #ccc;
    margin-top: 8px;
  }
}

.modal-container {
  width: 340px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  max-height: 500px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .modal-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.modal-content {
  flex: 1;
  padding: 20px;
  max-height: 340px;
}

.form-item {
  margin-bottom: 16px;

  .form-label {
    display: block;
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }

  .form-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
  }

  .type-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;

    .type-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 8px;
      background: #f5f6f7;
      border-radius: 10px;
      transition: all 0.3s;

      &.active {
        background: #e6f0ff;
        border: 2px solid #3385ff;
      }

      .type-icon {
        width: 44px;
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 10px;
        margin-bottom: 8px;
      }

      .type-name {
        font-size: 13px;
        color: #666;
      }
    }
  }

  .balance-input-wrapper {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;

    .balance-symbol {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-right: 8px;
    }

    .balance-input {
      flex: 1;
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
  }

  .form-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
  }
}

.modal-footer {
  display: flex;
  border-top: 1px solid #f0f0f0;

  .modal-btn {
    flex: 1;
    height: 48px;
    border: none;
    font-size: 15px;

    &.cancel {
      background: #f5f6f7;
      color: #666;
      border-radius: 0 0 0 16px;
    }

    &.confirm {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border-radius: 0 0 16px 0;
    }
  }
}
</style>
