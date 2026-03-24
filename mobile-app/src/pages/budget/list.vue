<template>
	<view class="budget-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<text class="page-title">预算管理</text>
			<u-button
				type="success"
				size="small"
				@click="showCreate = true"
				:custom-style="{
					borderRadius: '20px'
				}"
			>
				<template #icon>
					<u-icon name="plus" size="16"></u-icon>
				</template>
				新建
			</u-button>
		</view>

		<!-- 统计卡片 -->
		<view class="stats-section">
			<view class="stat-item">
				<view class="stat-icon" style="background: #dcfce7;">
					<u-icon name="wallet" size="24" color="#10b981"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">¥{{ totalBudget }}</text>
					<text class="stat-label">总预算</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #fef3c7;">
					<u-icon name="trend" size="24" color="#f59e0b"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">¥{{ totalSpent }}</text>
					<text class="stat-label">已支出</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #dbeafe;">
					<u-icon name="piechart" size="24" color="#3b82f6"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ remainingPercent }}%</text>
					<text class="stat-label">剩余比例</text>
				</view>
			</view>
		</view>

		<!-- 筛选 -->
		<view class="filter-section">
			<u-segment-control
				v-model="filterType"
				:list="['全部', '进行中', '已超支']"
				@current="onFilterChange"
			></u-segment-control>
		</view>

		<!-- 列表 -->
		<view class="list-section">
			<view
				v-for="item in filteredList"
				:key="item.id"
				class="budget-item"
				:class="{ overspent: item.spent > item.amount }"
			>
				<view class="item-header">
					<view class="item-info">
						<text class="item-name">{{ item.name }}</text>
						<u-tag
							:text="item.type === 'category' ? '分类预算' : '账户预算'"
							:type="item.type === 'category' ? 'info' : 'success'"
							size="mini"
							plain
						></u-tag>
					</view>
					<view class="item-actions">
						<u-button
							size="mini"
							@click.stop="handleEdit(item)"
							:custom-style="{
								background: '#f3f4f6',
								border: 'none'
							}"
						>
							编辑
						</u-button>
					</view>
				</view>

				<view class="item-amounts">
					<view class="amount-row">
						<text class="amount-label">预算金额</text>
						<text class="amount-value">¥{{ item.amount }}</text>
					</view>
					<view class="amount-row">
						<text class="amount-label">已支出</text>
						<text class="amount-value spent">¥{{ item.spent || 0 }}</text>
					</view>
					<view class="amount-row">
						<text class="amount-label">剩余</text>
						<text class="amount-value remaining">¥{{ Math.max(0, item.amount - (item.spent || 0)) }}</text>
					</view>
				</view>

				<view class="item-progress">
					<u-progress
						:percentage="Math.min(100, ((item.spent || 0) / item.amount * 100).toFixed(1))"
						:status="item.spent > item.amount ? 'error' : 'primary'"
						:show-text="true"
					></u-progress>
					
					<view class="progress-warning" v-if="item.spent > item.amount * 0.9 && item.spent <= item.amount">
						<u-notice-bar
							text="预算即将用尽，请控制支出"
							mode="closable"
							type="warning"
							:scrollable="false"
						></u-notice-bar>
					</view>
					<view class="progress-overspent" v-if="item.spent > item.amount">
						<u-notice-bar
							:text="`已超支 ¥${(item.spent - item.amount).toFixed(2)}`"
							mode="closable"
							type="error"
							:scrollable="false"
						></u-notice-bar>
					</view>
				</view>

				<view class="item-footer">
					<text class="item-period">
						<u-icon name="calendar" size="14"></u-icon>
						{{ getPeriodText(item.period) }}
					</text>
					<u-tag
						:text="item.status === 'active' ? '启用中' : '已暂停'"
						:type="item.status === 'active' ? 'success' : 'default'"
						size="mini"
					></u-tag>
				</view>

				<view class="item-delete">
					<u-button
						size="mini"
						type="error"
						@click.stop="handleDelete(item)"
						:custom-style="{
							background: '#fee2e2',
							border: 'none'
						}"
					>
						删除
					</u-button>
				</view>
			</view>

			<u-empty
				v-if="filteredList.length === 0"
				mode="data"
				text="暂无预算"
			></u-empty>
		</view>

		<!-- 新建/编辑弹窗 -->
		<u-modal
			v-model="showCreate"
			:title="editingId ? '编辑预算' : '新建预算'"
			:show-cancel-button="true"
			@confirm="handleSubmit"
			@cancel="showCreate = false"
		>
			<view class="modal-content">
				<u-form :model="formData" ref="formRef">
					<u-form-item label="预算名称">
						<u-input
							v-model="formData.name"
							placeholder="如：每月餐饮预算"
							:border="false"
						></u-input>
					</u-form-item>
					<u-form-item label="预算类型">
						<u-radio-group v-model="formData.type">
							<u-radio :name="'category'">分类预算</u-radio>
							<u-radio :name="'account'">账户预算</u-radio>
						</u-radio-group>
					</u-form-item>
					<u-form-item label="预算金额">
						<u-input
							v-model="formData.amount"
							type="digit"
							placeholder="请输入金额"
							:border="false"
						>
							<template #prefix>
								<text>¥</text>
							</template>
						</u-input>
					</u-form-item>
					<u-form-item label="周期">
						<u-select
							v-model="formData.period"
							:options="periodOptions"
							placeholder="请选择周期"
						></u-select>
					</u-form-item>
					<u-form-item label="状态">
						<u-switch
							v-model="formData.status"
							:active-value="'active'"
							:inactive-value="'paused'"
						></u-switch>
						<text style="margin-left: 12px; color: #6b7280; font-size: 13px;">
							{{ formData.status === 'active' ? '启用' : '暂停' }}
						</text>
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
			showCreate: false,
			editingId: null,
			filterType: 0,
			budgetList: [],
			formData: {
				name: '',
				type: 'category',
				amount: '',
				period: 'monthly',
				status: 'active'
			},
			periodOptions: [
				{ label: '每日', value: 'daily' },
				{ label: '每周', value: 'weekly' },
				{ label: '每月', value: 'monthly' },
				{ label: '每年', value: 'yearly' }
			]
		};
	},
	computed: {
		filteredList() {
			if (this.filterType === 0) return this.budgetList;
			if (this.filterType === 1) return this.budgetList.filter(b => b.status === 'active');
			if (this.filterType === 2) return this.budgetList.filter(b => b.spent > b.amount);
			return this.budgetList;
		},
		totalBudget() {
			return this.budgetList.reduce((sum, b) => sum + b.amount, 0);
		},
		totalSpent() {
			return this.budgetList.reduce((sum, b) => sum + (b.spent || 0), 0);
		},
		remainingPercent() {
			if (this.totalBudget === 0) return 100;
			return ((this.totalBudget - this.totalSpent) / this.totalBudget * 100).toFixed(1);
		}
	},
	onLoad() {
		this.loadBudgetList();
	},
	methods: {
		loadBudgetList() {
			// Mock 数据
			this.budgetList = [
				{
					id: 1,
					name: '每月餐饮',
					type: 'category',
					amount: 3000,
					spent: 2500,
					period: 'monthly',
					status: 'active'
				},
				{
					id: 2,
					name: '交通预算',
					type: 'category',
					amount: 500,
					spent: 320,
					period: 'monthly',
					status: 'active'
				},
				{
					id: 3,
					name: '购物预算',
					type: 'category',
					amount: 2000,
					spent: 2150,
					period: 'monthly',
					status: 'active'
				}
			];
		},

		onFilterChange(index) {
			this.filterType = index;
		},

		getPeriodText(period) {
			const map = { daily: '每日', weekly: '每周', monthly: '每月', yearly: '每年' };
			return map[period] || period;
		},

		handleEdit(item) {
			this.editingId = item.id;
			this.formData = { ...item };
			this.showCreate = true;
		},

		handleDelete(item) {
			uni.showModal({
				title: '确认删除',
				content: `确定要删除"${item.name}"吗？`,
				success: (res) => {
					if (res.confirm) {
						this.budgetList = this.budgetList.filter(b => b.id !== item.id);
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						});
					}
				}
			});
		},

		handleSubmit() {
			if (!this.formData.name) {
				uni.showToast({
					title: '请输入预算名称',
					icon: 'none'
				});
				return;
			}

			if (!this.formData.amount) {
				uni.showToast({
					title: '请输入预算金额',
					icon: 'none'
				});
				return;
			}

			if (this.editingId) {
				// 更新
				const index = this.budgetList.findIndex(b => b.id === this.editingId);
				if (index !== -1) {
					this.budgetList[index] = { ...this.budgetList[index], ...this.formData };
				}
				uni.showToast({
					title: '更新成功',
					icon: 'success'
				});
			} else {
				// 新建
				const newItem = {
					id: Date.now(),
					...this.formData,
					spent: 0
				};
				this.budgetList.push(newItem);
				uni.showToast({
					title: '创建成功',
					icon: 'success'
				});
			}

			this.showCreate = false;
			this.editingId = null;
			this.resetForm();
		},

		resetForm() {
			this.formData = {
				name: '',
				type: 'category',
				amount: '',
				period: 'monthly',
				status: 'active'
			};
		}
	}
};
</script>

<style lang="scss" scoped>
.budget-page {
	min-height: 100vh;
	background: #F5F6F7;
	padding: 20px;
	padding-bottom: 40px;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;

	.page-title {
		font-size: 20px;
		font-weight: 700;
		color: #333;
	}
}

.stats-section {
	display: flex;
	gap: 12px;
	margin-bottom: 16px;
}

.stat-item {
	flex: 1;
	background: #fff;
	border-radius: 12px;
	padding: 16px 12px;
	display: flex;
	align-items: center;
	gap: 12px;
}

.stat-icon {
	width: 48px;
	height: 48px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 10px;
}

.stat-info {
	display: flex;
	flex-direction: column;
}

.stat-value {
	font-size: 18px;
	font-weight: 700;
	color: #1f2937;
	line-height: 1;
}

.stat-label {
	font-size: 12px;
	color: #6b7280;
	margin-top: 4px;
}

.filter-section {
	margin-bottom: 16px;
}

.list-section {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.budget-item {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
	border-left: 4px solid #10b981;

	&.overspent {
		border-left-color: #ef4444;
		background: #fef2f2;
	}
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
}

.item-info {
	display: flex;
	align-items: center;
	gap: 8px;

	.item-name {
		font-size: 16px;
		font-weight: 600;
		color: #333;
	}
}

.item-actions {
	display: flex;
	gap: 8px;
}

.item-amounts {
	display: flex;
	flex-direction: column;
	gap: 8px;
	margin-bottom: 12px;
	padding: 12px;
	background: #f9fafb;
	border-radius: 8px;
}

.amount-row {
	display: flex;
	justify-content: space-between;
	align-items: center;

	.amount-label {
		font-size: 13px;
		color: #6b7280;
	}

	.amount-value {
		font-size: 15px;
		font-weight: 600;
		color: #1f2937;

		&.spent {
			color: #f59e0b;
		}

		&.remaining {
			color: #10b981;
		}
	}
}

.item-progress {
	margin-bottom: 12px;
}

.progress-warning,
.progress-overspent {
	margin-top: 8px;
}

.item-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 12px;
	border-top: 1px solid #e5e7eb;

	.item-period {
		display: flex;
		align-items: center;
		gap: 6px;
		font-size: 13px;
		color: #6b7280;
	}
}

.item-delete {
	display: flex;
	justify-content: flex-end;
	margin-top: 12px;
}

.modal-content {
	padding: 10px 0;
}
</style>
