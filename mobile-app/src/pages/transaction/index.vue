<template>
	<view class="container">
		<!-- 筛选栏 -->
		<view class="filter-bar">
			<u-button size="small" @click="showFilter = true" :custom-style="{
				background: '#fff',
				color: '#4F46E5',
				border: '1px solid #4F46E5',
				height: '36px',
				borderRadius: '8px'
			}">
				<u-icon name="filter" size="16"></u-icon>
				筛选
			</u-button>
			<u-button size="small" @click="goToCreate" type="primary" :custom-style="{
				height: '36px',
				borderRadius: '8px'
			}">
				<u-icon name="plus" size="16"></u-icon>
				记账
			</u-button>
		</view>

		<!-- 交易列表 -->
		<view class="transaction-list">
			<view class="date-group" v-for="(group, date) in groupedTransactions" :key="date">
				<text class="date-label">{{ date }}</text>
				<view class="transaction-item" v-for="(item, index) in group" :key="index" @click="goToDetail(item.id)">
					<view class="transaction-icon" :style="{ background: item.categoryColor }">
						<u-icon :name="item.categoryIcon" size="20" color="#fff"></u-icon>
					</view>
					<view class="transaction-info">
						<text class="transaction-name">{{ item.name }}</text>
						<text class="transaction-category">{{ item.category }}</text>
					</view>
					<text class="transaction-amount" :class="item.type === 'income' ? 'income' : 'expense'">
						{{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
					</text>
				</view>
			</view>
		</view>

		<!-- 筛选弹窗 -->
		<u-modal v-model="showFilter" title="筛选" :show-cancel-button="true" @confirm="applyFilter" @cancel="showFilter = false">
			<view class="modal-content">
				<u-form :model="filterForm" label-width="60">
					<u-form-item label="类型" prop="type">
						<u-radio-group v-model="filterForm.type">
							<u-radio name="all">全部</u-radio>
							<u-radio name="income">收入</u-radio>
							<u-radio name="expense">支出</u-radio>
						</u-radio-group>
					</u-form-item>
					<u-form-item label="分类" prop="category">
						<u-input v-model="filterForm.category" placeholder="请输入分类"></u-input>
					</u-form-item>
				</u-form>
			</view>
		</u-modal>

		<!-- 添加交易弹窗 -->
		<u-modal v-model="showAddModal" title="添加交易" :show-cancel-button="true" @confirm="handleAddTransaction" @cancel="showAddModal = false">
			<view class="modal-content">
				<u-form :model="newTransaction" label-width="70">
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
export default {
	data() {
		return {
			showFilter: false,
			showAddModal: false,
			filterForm: { type: 'all', category: '' },
			transactions: [],
			newTransaction: { type: 'expense', amount: '', category: '', note: '' }
		};
	},
	computed: {
		groupedTransactions() {
			const groups = {};
			this.transactions.forEach(item => {
				if (!groups[item.date]) groups[item.date] = [];
				groups[item.date].push(item);
			});
			return groups;
		}
	},
	onLoad() {
		this.loadMockData();
	},
	methods: {
		goToCreate() {
			uni.navigateTo({ url: '/pages/transaction/create' });
		},
		loadMockData() {
			this.transactions = [
				{ id: 1, name: '午餐', category: '餐饮', amount: 35, type: 'expense', date: '今天', categoryColor: '#EF4444', categoryIcon: 'shopping-cart' },
				{ id: 2, name: '工资', category: '工作', amount: 15000, type: 'income', date: '昨天', categoryColor: '#10B981', categoryIcon: 'money' },
				{ id: 3, name: '地铁', category: '交通', amount: 6, type: 'expense', date: '昨天', categoryColor: '#3B82F6', categoryIcon: 'bus' },
				{ id: 4, name: '咖啡', category: '餐饮', amount: 28, type: 'expense', date: '03-22', categoryColor: '#F59E0B', categoryIcon: 'cup' },
				{ id: 5, name: '超市购物', category: '购物', amount: 256, type: 'expense', date: '03-21', categoryColor: '#8B5CF6', categoryIcon: 'bag' }
			];
		},
		applyFilter() {
			uni.showToast({ title: '筛选功能开发中', icon: 'none' });
			this.showFilter = false;
		},
		goToDetail(id) {
			uni.showToast({ title: '详情页开发中', icon: 'none' });
		},
		handleAddTransaction() {
			if (!this.newTransaction.amount) {
				uni.showToast({ title: '请输入金额', icon: 'none' });
				return;
			}
			this.transactions.unshift({
				id: Date.now(),
				name: this.newTransaction.category || '未分类',
				category: this.newTransaction.category || '其他',
				amount: parseFloat(this.newTransaction.amount),
				type: this.newTransaction.type,
				date: '今天',
				categoryColor: this.newTransaction.type === 'income' ? '#10B981' : '#EF4444',
				categoryIcon: this.newTransaction.type === 'income' ? 'money' : 'shopping-cart'
			});
			uni.showToast({ title: '添加成功', icon: 'success' });
			this.showAddModal = false;
			this.newTransaction = { type: 'expense', amount: '', category: '', note: '' };
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

.filter-bar {
	display: flex;
	justify-content: space-between;
	margin-bottom: 16px;
}

.transaction-list {
	background: #fff;
	border-radius: 12px;
	padding: 16px;

	.date-group {
		margin-bottom: 20px;

		&:last-child {
			margin-bottom: 0;
		}

		.date-label {
			display: block;
			font-size: 13px;
			color: #9CA3AF;
			margin-bottom: 8px;
		}

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

				.transaction-category {
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
