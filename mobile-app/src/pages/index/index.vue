<template>
	<view class="home-page">
		<!-- 顶部状态卡片 -->
		<view class="status-card">
			<!-- 月份切换 -->
			<view class="month-selector">
				<u-icon name="arrow-left" size="20" color="#fff" @click="prevMonth"></u-icon>
				<text class="month-text">{{ currentMonth }} {{ currentYear }}</text>
				<u-icon name="arrow-right" size="20" color="#fff" @click="nextMonth"></u-icon>
			</view>

			<!-- 收支概览 -->
			<view class="summary">
				<view class="item income">
					<view class="item-header">
						<u-icon name="arrow-up-circle" size="18"></u-icon>
						<text class="item-label">收入</text>
					</view>
					<text class="item-value">¥{{ monthlyIncome }}</text>
				</view>
				<view class="divider"></view>
				<view class="item expense">
					<view class="item-header">
						<u-icon name="arrow-down-circle" size="18"></u-icon>
						<text class="item-label">支出</text>
					</view>
					<text class="item-value">¥{{ monthlyExpense }}</text>
				</view>
				<view class="divider"></view>
				<view class="item balance">
					<view class="item-header">
						<u-icon name="wallet" size="18"></u-icon>
						<text class="item-label">结余</text>
					</view>
					<text class="item-value">¥{{ monthlyBalance }}</text>
				</view>
			</view>

			<!-- 预算进度 -->
			<view class="budget-progress">
				<view class="budget-header">
					<text class="budget-label">本月预算</text>
					<text class="budget-value">¥{{ budgetTotal }} / ¥{{ budgetUsed }}</text>
				</view>
				<u-progress
					:percent="budgetPercent"
					:stroke-width="6"
					:active-color="budgetPercent > 90 ? '#EF4444' : budgetPercent > 70 ? '#F59E0B' : '#10B981'"
				></u-progress>
				<text class="budget-remaining">剩余 ¥{{ budgetRemaining }}</text>
			</view>
		</view>

		<!-- 快捷功能 -->
		<view class="quick-actions">
			<view class="quick-item" @click="goToCreate">
				<view class="quick-icon add">
					<u-icon name="edit-pen" size="24" color="#fff"></u-icon>
				</view>
				<text class="quick-label">记账</text>
			</view>
			<view class="quick-item" @click="goToAccount">
				<view class="quick-icon account">
					<u-icon name="wallet" size="24" color="#fff"></u-icon>
				</view>
				<text class="quick-label">账户</text>
			</view>
			<view class="quick-item" @click="goToCategory">
				<view class="quick-icon category">
					<u-icon name="grid" size="24" color="#fff"></u-icon>
				</view>
				<text class="quick-label">分类</text>
			</view>
			<view class="quick-item" @click="goToBook">
				<view class="quick-icon book">
					<u-icon name="book" size="24" color="#fff"></u-icon>
				</view>
				<text class="quick-label">账本</text>
			</view>
		</view>

		<!-- 最近交易 -->
		<view class="recent-section">
			<view class="section-header">
				<text class="section-title">最近交易</text>
				<text class="section-more" @click="goToTransactions">
					查看全部
					<u-icon name="arrow-right" size="14"></u-icon>
				</text>
			</view>
			<view class="transaction-list">
				<view
					class="transaction-item"
					v-for="(item, index) in recentTransactions"
					:key="index"
					@click="goToTransactionDetail(item.id)"
				>
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

			<!-- 空状态 -->
			<u-empty
				v-if="recentTransactions.length === 0"
				mode="data"
				text="暂无交易记录"
			></u-empty>
		</view>
	</view>
</template>

<script>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

export default {
	data() {
		return {
			currentDate: new Date(),
			monthlyIncome: 0,
			monthlyExpense: 0,
			monthlyBalance: 0,
			budgetTotal: 10000,
			budgetUsed: 0,
			recentTransactions: []
		};
	},
	computed: {
		currentMonth() {
			return this.currentDate.getMonth() + 1;
		},
		currentYear() {
			return this.currentDate.getFullYear();
		},
		budgetPercent() {
			if (this.budgetTotal === 0) return 0;
			return Math.round((this.budgetUsed / this.budgetTotal) * 100);
		},
		budgetRemaining() {
			return this.budgetTotal - this.budgetUsed;
		}
	},
	onLoad() {
		this.loadData();
	},
	methods: {
		loadData() {
			// Mock 数据
			this.monthlyIncome = 15000;
			this.monthlyExpense = 6500;
			this.monthlyBalance = this.monthlyIncome - this.monthlyExpense;
			this.budgetUsed = this.monthlyExpense;

			this.recentTransactions = [
				{ id: 1, name: '午餐', time: '今天 12:30', amount: 35, type: 'expense', categoryColor: '#EF4444', categoryIcon: 'fastfood' },
				{ id: 2, name: '地铁', time: '今天 08:30', amount: 6, type: 'expense', categoryColor: '#3B82F6', categoryIcon: 'car' },
				{ id: 3, name: '工资', time: '昨天 09:00', amount: 15000, type: 'income', categoryColor: '#10B981', categoryIcon: 'cash' },
				{ id: 4, name: '咖啡', time: '03-22 14:00', amount: 28, type: 'expense', categoryColor: '#F59E0B', categoryIcon: 'beer' },
				{ id: 5, name: '超市购物', time: '03-21 18:00', amount: 256, type: 'expense', categoryColor: '#8B5CF6', categoryIcon: 'cart' }
			];
		},
		prevMonth() {
			this.currentDate.setMonth(this.currentDate.getMonth() - 1);
			this.loadData();
		},
		nextMonth() {
			this.currentDate.setMonth(this.currentDate.getMonth() + 1);
			this.loadData();
		},
		goToCreate() {
			uni.navigateTo({ url: '/pages/transaction/create' });
		},
		goToAccount() {
			uni.showToast({ title: '账户页面开发中', icon: 'none' });
		},
		goToCategory() {
			uni.showToast({ title: '分类页面开发中', icon: 'none' });
		},
		goToBook() {
			uni.switchTab({ url: '/pages/book/index' });
		},
		goToTransactions() {
			uni.switchTab({ url: '/pages/transaction/index' });
		},
		goToTransactionDetail(id) {
			uni.showToast({ title: '详情页开发中', icon: 'none' });
		}
	}
};
</script>

<style lang="scss" scoped>
.home-page {
	min-height: 100vh;
	background: #F5F6F7;
}

.status-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 0 20px 20px;
	padding-bottom: 20px;
}

.month-selector {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px 0 16px;

	.month-text {
		font-size: 18px;
		font-weight: 600;
		color: #fff;
	}
}

.summary {
	display: flex;
	justify-content: space-between;
	align-items: center;
	background: rgba(255, 255, 255, 0.15);
	border-radius: 16px;
	padding: 20px 16px;
	margin-bottom: 16px;

	.item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8px;

		.item-header {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 4px;

			.item-label {
				font-size: 13px;
				color: rgba(255, 255, 255, 0.8);
			}
		}

		.item-value {
			font-size: 20px;
			font-weight: 600;
			color: #fff;
		}

		&.income .item-value {
			color: #BBF7D0;
		}

		&.expense .item-value {
			color: #FECACA;
		}
	}

	.divider {
		width: 1px;
		height: 40px;
		background: rgba(255, 255, 255, 0.3);
	}
}

.budget-progress {
	background: rgba(255, 255, 255, 0.15);
	border-radius: 12px;
	padding: 16px;

	.budget-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 12px;

		.budget-label {
			font-size: 14px;
			color: rgba(255, 255, 255, 0.8);
		}

		.budget-value {
			font-size: 14px;
			color: #fff;
			font-weight: 600;
		}
	}

	.budget-remaining {
		display: block;
		text-align: right;
		font-size: 12px;
		color: rgba(255, 255, 255, 0.8);
		margin-top: 8px;
	}
}

.quick-actions {
	display: flex;
	justify-content: space-around;
	padding: 20px 16px;
	background: #fff;
	margin-bottom: 12px;

	.quick-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8px;

		.quick-icon {
			width: 56px;
			height: 56px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 16px;

			&.add {
				background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
			}

			&.account {
				background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
			}

			&.category {
				background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
			}

			&.book {
				background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
			}
		}

		.quick-label {
			font-size: 13px;
			color: #666;
		}
	}
}

.recent-section {
	background: #fff;
	border-radius: 12px;
	margin: 0 16px 16px;
	padding: 16px;

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16px;

		.section-title {
			font-size: 16px;
			font-weight: 600;
			color: #1F2937;
		}

		.section-more {
			font-size: 13px;
			color: #9CA3AF;
			display: flex;
			align-items: center;
			gap: 4px;
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
				display: flex;
				align-items: center;
				justify-content: center;
				border-radius: 10px;
				margin-right: 12px;
				flex-shrink: 0;
			}

			.transaction-info {
				flex: 1;
				min-width: 0;
				display: flex;
				flex-direction: column;
				gap: 4px;

				.transaction-name {
					font-size: 15px;
					color: #1F2937;
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
				}

				.transaction-time {
					font-size: 12px;
					color: #9CA3AF;
				}
			}

			.transaction-amount {
				font-size: 16px;
				font-weight: 600;
				flex-shrink: 0;

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
</style>
