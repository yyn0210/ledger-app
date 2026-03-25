<template>
  <div class="account-list">
    <!-- 头部操作区 -->
    <n-card>
      <n-space justify="space-between" align="center">
        <n-space>
          <h2 style="margin: 0">💳 账户管理</h2>
          <n-segmented
            v-model:value="filterType"
            :options="[
              { label: '全部', value: 'all' },
              { label: '💵 现金', value: 'cash' },
              { label: '🏦 银行卡', value: 'bank' },
              { label: '💳 信用卡', value: 'credit' },
              { label: '💙 支付宝', value: 'alipay' },
              { label: '💚 微信', value: 'wechat' }
            ]"
            @update:value="handleFilterChange"
          />
        </n-space>
        <n-space>
          <n-button @click="toggleHideBalance">
            <template #icon>
              <n-icon :component="hideBalance ? EyeOffOutline : EyeOutline" />
            </template>
            {{ hideBalance ? '显示余额' : '隐藏余额' }}
          </n-button>
          <n-button type="primary" @click="handleCreate">
            <template #icon>
              <n-icon :component="AddOutline" />
            </template>
            新建账户
          </n-button>
        </n-space>
      </n-space>
    </n-card>

    <!-- 资产概览 -->
    <n-card style="margin-top: 20px">
      <n-grid :cols="3" :x-gap="20">
        <n-grid-item>
          <n-statistic label="总资产">
            <balance-display :balance="totalAssets" :show-toggle="false" />
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="总负债">
            <n-text type="error">
              <balance-display :balance="totalLiabilities" :show-toggle="false" />
            </n-text>
          </n-statistic>
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="净资产">
            <n-text :type="netAssets >= 0 ? 'success' : 'error'">
              <balance-display :balance="netAssets" :show-toggle="false" />
            </n-text>
          </n-statistic>
        </n-grid-item>
      </n-grid>
    </n-card>

    <!-- 账户列表 -->
    <n-card style="margin-top: 20px">
      <template #header>
        <n-space justify="space-between" align="center">
          <span>账户列表</span>
          <n-tag v-if="filteredAccounts.length > 0" type="info">
            共 {{ filteredAccounts.length }} 个账户
          </n-tag>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-empty
          v-if="!loading && filteredAccounts.length === 0"
          description="暂无账户，点击右上角创建一个吧～"
          style="padding: 60px 0"
        />

        <n-grid
          v-else
          :cols="1"
          :x-gap="20"
          :y-gap="20"
          :breakpoints="{
            640: 2,
            1024: 3
          }"
        >
          <n-grid-item v-for="account in filteredAccounts" :key="account.id">
            <account-card
              :account="account"
              :show-actions="true"
              @edit="handleEdit"
              @delete="handleDelete"
            />
          </n-grid-item>
        </n-grid>
      </n-spin>
    </n-card>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :title="editingAccount ? '编辑账户' : '新建账户'"
      preset="card"
      style="width: 550px"
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
            placeholder="请输入账户名称（如：招商银行卡、支付宝）"
            maxlength="30"
            show-count
          />
        </n-form-item>

        <n-form-item label="账户类型" path="type">
          <n-select
            v-model:value="formData.type"
            :options="accountTypeOptions"
            placeholder="选择账户类型"
          />
        </n-form-item>

        <n-form-item label="初始余额" path="balance">
          <n-input-number
            v-model:value="formData.balance"
            :min="-999999999"
            :max="999999999"
            placeholder="输入初始余额"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <n-form-item label="账户颜色" path="color">
          <color-picker v-model="formData.color" />
        </n-form-item>

        <!-- 信用卡特有字段 -->
        <template v-if="formData.type === 'credit'">
          <n-divider>信用卡信息</n-divider>

          <n-form-item label="信用额度" path="creditLimit">
            <n-input-number
              v-model:value="formData.creditLimit"
              :min="0"
              :max="999999999"
              placeholder="输入信用额度"
              style="width: 100%"
            >
              <template #prefix>¥</template>
            </n-input-number>
          </n-form-item>

          <n-grid :cols="2" :x-gap="16">
            <n-grid-item>
              <n-form-item label="账单日" path="billDay">
                <n-input-number
                  v-model:value="formData.billDay"
                  :min="1"
                  :max="31"
                  placeholder="1-31"
                  style="width: 100%"
                />
              </n-form-item>
            </n-grid-item>
            <n-grid-item>
              <n-form-item label="还款日" path="repaymentDay">
                <n-input-number
                  v-model:value="formData.repaymentDay"
                  :min="1"
                  :max="31"
                  placeholder="1-31"
                  style="width: 100%"
                />
              </n-form-item>
            </n-grid-item>
          </n-grid>
        </template>

        <n-form-item label="备注" path="note">
          <n-input
            v-model:value="formData.note"
            type="textarea"
            placeholder="备注信息（可选）"
            maxlength="200"
            show-count
            :rows="2"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ editingAccount ? '保存' : '创建' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  AddOutline,
  EyeOutline,
  EyeOffOutline
} from '@vicons/ionicons5'
import { useAccountStore } from '@/stores/account'
import AccountCard from '@/components/account/AccountCard.vue'
import BalanceDisplay from '@/components/account/BalanceDisplay.vue'
import ColorPicker from '@/components/category/ColorPicker.vue'
import { typeConfigMap } from '@/components/account/AccountTypeIcon.vue'

const message = useMessage()
const dialog = useDialog()
const accountStore = useAccountStore()

const formRef = ref(null)
const showModal = ref(false)
const submitting = ref(false)
const filterType = ref('all')
const editingAccount = ref(null)

const loading = computed(() => accountStore.loading)
const filteredAccounts = computed(() => accountStore.filteredAccounts)
const hideBalance = computed(() => accountStore.hideBalance)
const totalAssets = computed(() => accountStore.totalAssets)
const totalLiabilities = computed(() => accountStore.totalLiabilities)
const netAssets = computed(() => accountStore.netAssets)

const formData = reactive({
  name: '',
  type: 'cash',
  balance: 0,
  color: '#10b981',
  note: '',
  creditLimit: null,
  billDay: null,
  repaymentDay: null
})

const formRules = {
  name: [
    {
      required: true,
      message: '请输入账户名称',
      trigger: 'blur'
    },
    {
      min: 1,
      max: 30,
      message: '名称长度 1-30 个字符',
      trigger: 'blur'
    }
  ],
  type: [
    {
      required: true,
      message: '请选择账户类型',
      trigger: 'change'
    }
  ]
}

// 账户类型选项
const accountTypeOptions = computed(() => {
  return Object.entries(typeConfigMap).map(([key, config]) => ({
    label: config.label,
    value: key
  }))
})

// 筛选类型变化
const handleFilterChange = (type) => {
  accountStore.setFilterType(type)
  accountStore.fetchAccountList({ type: type !== 'all' ? type : undefined })
}

// 切换余额隐藏
const toggleHideBalance = () => {
  accountStore.toggleHideBalance()
}

// 新建账户
const handleCreate = () => {
  editingAccount.value = null
  resetForm()
  showModal.value = true
}

// 编辑账户
const handleEdit = (account) => {
  editingAccount.value = account
  formData.name = account.name
  formData.type = account.type
  formData.balance = account.balance || 0
  formData.color = account.color || typeConfigMap[account.type]?.color || '#10b981'
  formData.note = account.note || ''
  formData.creditLimit = account.creditLimit || null
  formData.billDay = account.billDay || null
  formData.repaymentDay = account.repaymentDay || null
  showModal.value = true
}

// 删除账户
const handleDelete = (account) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除账户「${account.name}」吗？删除后无法恢复！`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await accountStore.deleteAccount(account.id)
        message.success('账户已删除')
      } catch (error) {
        message.error('删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.type = 'cash'
  formData.balance = 0
  formData.color = '#10b981'
  formData.note = ''
  formData.creditLimit = null
  formData.billDay = null
  formData.repaymentDay = null
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      name: formData.name.trim(),
      type: formData.type,
      balance: formData.balance || 0,
      color: formData.color,
      note: formData.note.trim(),
      order: 0
    }

    // 信用卡特有字段
    if (formData.type === 'credit') {
      data.creditLimit = formData.creditLimit || null
      data.billDay = formData.billDay || null
      data.repaymentDay = formData.repaymentDay || null
    }

    if (editingAccount.value) {
      await accountStore.updateAccount(editingAccount.value.id, data)
      message.success('账户已更新')
    } else {
      await accountStore.createAccount(data)
      message.success('账户已创建')
    }

    showModal.value = false
    resetForm()
  } catch (error) {
    if (error?.errors) return
    message.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  accountStore.fetchAccountList()
})
</script>

<style scoped>
.account-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
