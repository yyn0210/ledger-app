<template>
	<view class="container">
		<!-- 顶部状态卡片 -->
		<view class="status-card">
			<view class="month-info">
				<text class="month-label">{{ currentMonth }}</text>
				<text class="month-year">{{ currentYear }}</text>
			</view>
			<view class="summary">
				<view class="item income">
					<text class="label">收入</text>
					<text class="value">¥{{ monthlyIncome }}</text>
				</view>
				<view class="item expense">
					<text class="label">支出</text>
					<text class="value">¥{{ monthlyExpense }}</text>
				</view>
				<view class="item balance">
					<text class="label">结余</text>
					<text class="value">¥{{ monthlyBalance }}</text>
				</view>
			</view>
		</view>

		<!-- 快速记账按钮 -->
		<view class="quick-add">
			<u-button type="primary" size="large" @click="showAddModal = true" :custom-style="{
				background: 'linear-gradient(135deg, #4F46E5 0%, #6366F1 100%)',
				height: '56px',
				borderRadius: '12px',
				fontSize: '18px',
				fontWeight: '600'
			}">
				<u-icon name="plus" size="20"></u-icon>
				快速记账
			</u-button>
		</view>

		<!-- 最近交易 -->
		<view class="recent-section">
			<view class="section-header">
				<text class="section-title">最近交易</text>
				<text class="section-more" @click="goToTransactions">查看全部 ></text>
			</view>
			<view class="transaction-list">
				<view class="transaction-item" v-for="(item, index) in recentTransactions" :key="index" @click="goToTransactionDetail(item.id)">
					<view class="transaction-icon" :style="{ background: item.categoryColor }">
						<u-icon :name="item.categoryIcon" size="20" color="#fff"></u-icon>
					</view>
					<view class="transaction-info">
						<text class="transaction-name">{{ item.name }}</text>
						<text class="transaction-time">{{ item.time }}</text>
					</view>
					<text class="transaction-amount" :class="item.type === 'income' ? 'income' : 'expense'">
						{{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
					</text>
				</view>
			</view>
		</view>

		<!-- 添加交易弹窗 -->
		<u-modal v-model="showAddModal" title="快速记账" :show-cancel-button="true" @confirm="handleAddTransaction" @cancel="showAddModal = false">
			<view class="modal-content">
				<u-form :model="newTransaction" label-width="80">
					<u-form-item label="类型" prop="type">
						<u-radio-group v-model="newTransaction.type">
							<u-radio name="expense">支出</u-radio>
							<u-radio name="income">收入</u-radio>
						</u-radio-group>
					</u-form-item>
					<u-form-item label="金额" prop="amount">
						<u-input v-model="newTransaction.amount" type="number" placeholder="请输入金额"></u-input>
					</u-form-item>
					<u-form-item label="分类" prop="category">
						<u-input v-model="newTransaction.category" placeholder="请输入分类"></u-input>
					</u-form-item>
					<u-form-item label="备注" prop="note">
						<u-input v-model="newTransaction.note" placeholder="请输入备注"></u-input>
					</u-form-item>
				</u-form>
			</view>
		</u-modal>
	</view>
</template>

<script>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

export default {
	data() {
		return {
			showAddModal: false,
			currentMonth: '三月',
			currentYear: '2026',
			monthlyIncome: 0,
			monthlyExpense: 0,
			monthlyBalance: 0,
			recentTransactions: [],
			newTransaction: {
				type: 'expense',
				amount: '',
				category: '',
				note: ''
			}
		};
	},
	onLoad() {
		this.loadMockData();
	},
	methods: {
		loadMockData() {
			// Mock 数据
			this.monthlyIncome = 15000;
			this.monthlyExpense = 8500;
			this.monthlyBalance = this.monthlyIncome - this.monthlyExpense;
			
			this.recentTransactions = [
				{ id: 1, name: '午餐', time: '今天 12:30', amount: 35, type: 'expense', categoryColor: '#EF4444', categoryIcon: 'shopping-cart' },
				{ id: 2, name: '工资', time: '昨天 09:00', amount: 15000, type: 'income', categoryColor: '#10B981', categoryIcon: 'money' },
				{ id: 3, name: '地铁', time: '昨天 08:30', amount: 6, type: 'expense', categoryColor: '#3B82F6', categoryIcon: 'bus' },
				{ id: 4, name: '咖啡', time: '03-22 14:00', amount: 28, type: 'expense', categoryColor: '#F59E0B', categoryIcon: 'cup' }
			];
		},
		goToTransactions() {
			uni.switchTab({ url: '/pages/transaction/index' });
		},
		goToTransactionDetail(id) {
			uni.showToast({ title: '详情页开发中', icon: 'none' });
		},
		handleAddTransaction() {
			if (!this.newTransaction.amount) {
				uni.showToast({ title: '请输入金额', icon: 'none' });
				return;
			}
			uni.showToast({ title: '记账成功', icon: 'success' });
			this.showAddModal = false;
			this.newTransaction = { type: 'expense', amount: '', category: '', note: '' };
			this.loadMockData();
		}
	}
};
</script>

<style scoped>
.container {
	padding: 16px;
	padding-bottom: 80px;
	background: #F5F6F7;
	min-height: 100vh;
}

.status-card {
	background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
	border-radius: 16px;
	padding: 20px 16px;
	color: #fff;
	margin-bottom: 16px;

	.month-info {
		display: flex;
		justify-content: space-between;
		align-items: baseline;
		margin-bottom: 16px;

		.month-label {
			font-size: 18px;
			font-weight: 600;
		}

		.month-year {
			font-size: 14px;
			opacity: 0.8;
		}
	}

	.summary {
		display: flex;
		justify-content: space-between;

		.item {
			display: flex;
			flex-direction: column;
			gap: 4px;

			.label {
				font-size: 12px;
				opacity: 0.8;
			}

			.value {
				font-size: 18px;
				font-weight: 600;
			}

			&.income .value {
				color: #BBF7D0;
			}

			&.expense .value {
				color: #FECACA;
			}

			&.balance .value {
				color: #fff;
			}
		}
	}
}

.quick-add {
	margin-bottom: 16px;
}

.recent-section {
	background: #fff;
	border-radius: 12px;
	padding: 16px;

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 12px;

		.section-title {
			font-size: 16px;
			font-weight: 600;
			color: #1F2937;
		}

		.section-more {
			font-size: 13px;
			color: #9CA3AF;
		}
	}

	.transaction-list {
		.transaction-item {
			display: flex;
			align-items: center;
			padding: 12px 0;
			border-bottom: 1px solid #F3F4F6;

			&:last-child {
				border-bottom: none;
			}

			.transaction-icon {
				width: 40px;
				height: 40px;
				border-radius: 10px;
				display: flex;
				align-items: center;
				justify-content: center;
				margin-right: 12px;
			}

			.transaction-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				gap: 4px;

				.transaction-name {
					font-size: 15px;
					color: #1F2937;
				}

				.transaction-time {
					font-size: 12px;
					color: #9CA3AF;
				}
			}

			.transaction-amount {
				font-size: 16px;
				font-weight: 600;

				&.income {
					color: #10B981;
				}

				&.expense {
					color: #EF4444;
				}
			}
		}
	}
}

.modal-content {
	padding: 16px 0;
}
</style>
