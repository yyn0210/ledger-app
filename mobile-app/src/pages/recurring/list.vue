<template>
	<view class="recurring-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<text class="page-title">周期账单</text>
			<u-button
				type="primary"
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
				<view class="stat-icon" style="background: #f3e8ff;">
					<u-icon name="repeat" size="24" color="#8b5cf6"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ recurringList.length }}</text>
					<text class="stat-label">周期账单</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #dcfce7;">
					<u-icon name="trend" size="24" color="#10b981"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ monthlyCount }}</text>
					<text class="stat-label">本月待执行</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #fef3c7;">
					<u-icon name="calendar" size="24" color="#f59e0b"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ todayCount }}</text>
					<text class="stat-label">今日待执行</text>
				</view>
			</view>
		</view>

		<!-- 筛选 -->
		<view class="filter-section">
			<u-segment-control
				v-model="filterType"
				:list="['全部', '进行中', '已完成']"
				@current="onFilterChange"
			></u-segment-control>
		</view>

		<!-- 列表 -->
		<view class="list-section">
			<view
				v-for="item in filteredList"
				:key="item.id"
				class="recurring-item"
				@click="handleEdit(item)"
			>
				<view class="item-header">
					<text class="item-name">{{ item.name }}</text>
					<u-tag
						:text="item.status === 'active' ? '启用' : '暂停'"
						:type="item.status === 'active' ? 'success' : 'default'"
						size="mini"
						plain
					></u-tag>
				</view>
				<view class="item-body">
					<view class="item-info">
						<text class="item-amount">¥{{ item.amount }}</text>
						<text class="item-type">{{ item.type === 'income' ? '收入' : '支出' }}</text>
					</view>
					<view class="item-meta">
						<text class="item-frequency">{{ getFrequencyText(item.frequency) }}</text>
						<text class="item-next">下次：{{ formatDate(item.nextDate) }}</text>
					</view>
				</view>
				<view class="item-actions">
					<u-button
						size="mini"
						@click.stop="handleExecute(item)"
						:custom-style="{
							background: '#f3f4f6',
							border: 'none'
						}"
					>
						执行
					</u-button>
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
				text="暂无周期账单"
			></u-empty>
		</view>

		<!-- 新建/编辑弹窗 -->
		<u-modal
			v-model="showCreate"
			:title="editingId ? '编辑周期账单' : '新建周期账单'"
			:show-cancel-button="true"
			@confirm="handleSubmit"
			@cancel="showCreate = false"
		>
			<view class="modal-content">
				<u-form :model="formData" ref="formRef">
					<u-form-item label="账单名称">
						<u-input
							v-model="formData.name"
							placeholder="如：每月房租"
							:border="false"
						></u-input>
					</u-form-item>
					<u-form-item label="交易类型">
						<u-radio-group v-model="formData.type">
							<u-radio :name="'expense'">支出</u-radio>
							<u-radio :name="'income'">收入</u-radio>
						</u-radio-group>
					</u-form-item>
					<u-form-item label="金额">
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
					<u-form-item label="分类">
						<u-select
							v-model="formData.categoryId"
							:options="categoryOptions"
							placeholder="请选择分类"
						></u-select>
					</u-form-item>
					<u-form-item label="周期类型">
						<u-select
							v-model="formData.frequency"
							:options="frequencyOptions"
							placeholder="请选择周期"
						></u-select>
					</u-form-item>
					<u-form-item label="执行日期">
						<u-datetime-picker
							:show="showDate"
							v-model="currentDate"
							mode="date"
							@confirm="onDateConfirm"
							@cancel="showDate = false"
						></u-datetime-picker>
						<u-input
							:model-value="formattedDate"
							@click="showDate = true"
							:border="false"
							placeholder="选择日期"
						></u-input>
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
import { getCategoryList } from '@/api/category';

export default {
	data() {
		return {
			showCreate: false,
			showDate: false,
			editingId: null,
			filterType: 0,
			currentDate: Date.now(),
			recurringList: [],
			formData: {
				name: '',
				type: 'expense',
				amount: '',
				categoryId: null,
				frequency: 'monthly',
				executionDate: Date.now(),
				status: 'active'
			},
			categories: {
				expense: [],
				income: []
			},
			frequencyOptions: [
				{ label: '每日', value: 'daily' },
				{ label: '每周', value: 'weekly' },
				{ label: '每月', value: 'monthly' },
				{ label: '每年', value: 'yearly' }
			]
		};
	},
	computed: {
		categoryOptions() {
			const list = this.formData.type === 'income'
				? this.categories.income
				: this.categories.expense;
			return list.map(c => ({ label: c.name, value: c.id }));
		},
		filteredList() {
			if (this.filterType === 0) return this.recurringList;
			const status = this.filterType === 1 ? 'active' : 'completed';
			return this.recurringList.filter(r => r.status === status);
		},
		monthlyCount() {
			const now = new Date();
			const thisMonth = now.getMonth();
			return this.recurringList.filter(r => {
				if (r.status !== 'active') return false;
				const nextDate = new Date(r.nextDate);
				return nextDate.getMonth() === thisMonth && nextDate.getFullYear() === now.getFullYear();
			}).length;
		},
		todayCount() {
			const today = new Date().toDateString();
			return this.recurringList.filter(r => {
				if (r.status !== 'active') return false;
				return new Date(r.nextDate).toDateString() === today;
			}).length;
		},
		formattedDate() {
			const date = new Date(this.formData.executionDate);
			return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
		}
	},
	onLoad() {
		this.loadCategories();
		this.loadRecurringList();
	},
	methods: {
		async loadCategories() {
			try {
				const data = await getCategoryList();
				const defaults = data.defaults || [];
				this.categories.expense = defaults.filter(c => c.type === 'expense');
				this.categories.income = defaults.filter(c => c.type === 'income');
			} catch (error) {
				console.error('获取分类失败:', error);
			}
		},
		
		async loadRecurringList() {
			// Mock 数据
			this.recurringList = [
				{
					id: 1,
					name: '每月房租',
					type: 'expense',
					amount: 3500,
					categoryId: 6,
					frequency: 'monthly',
					nextDate: Date.now() + 86400000 * 5,
					status: 'active'
				},
				{
					id: 2,
					name: '每周买菜',
					type: 'expense',
					amount: 200,
					categoryId: 1,
					frequency: 'weekly',
					nextDate: Date.now() + 86400000 * 2,
					status: 'active'
				},
				{
					id: 3,
					name: '工资收入',
					type: 'income',
					amount: 15000,
					categoryId: 7,
					frequency: 'monthly',
					nextDate: Date.now() + 86400000 * 10,
					status: 'active'
				}
			];
		},
		
		onFilterChange(index) {
			this.filterType = index;
		},
		
		getFrequencyText(freq) {
			const map = { daily: '每日', weekly: '每周', monthly: '每月', yearly: '每年' };
			return map[freq] || freq;
		},
		
		formatDate(timestamp) {
			const date = new Date(timestamp);
			return `${date.getMonth() + 1}/${date.getDate()}`;
		},
		
		handleEdit(item) {
			this.editingId = item.id;
			this.formData = { ...item };
			this.showCreate = true;
		},
		
		handleExecute(item) {
			uni.showToast({
				title: '执行成功',
				icon: 'success'
			});
		},
		
		handleDelete(item) {
			uni.showModal({
				title: '确认删除',
				content: `确定要删除"${item.name}"吗？`,
				success: (res) => {
					if (res.confirm) {
						this.recurringList = this.recurringList.filter(r => r.id !== item.id);
						uni.showToast({
							title: '删除成功',
							icon: 'success'
						});
					}
				}
			});
		},
		
		onDateConfirm(e) {
			this.formData.executionDate = e.value;
			this.showDate = false;
		},
		
		handleSubmit() {
			if (!this.formData.name) {
				uni.showToast({
					title: '请输入账单名称',
					icon: 'none'
				});
				return;
			}
			
			if (!this.formData.amount) {
				uni.showToast({
					title: '请输入金额',
					icon: 'none'
				});
				return;
			}
			
			if (this.editingId) {
				// 更新
				const index = this.recurringList.findIndex(r => r.id === this.editingId);
				if (index !== -1) {
					this.recurringList[index] = { ...this.recurringList[index], ...this.formData };
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
					nextDate: this.formData.executionDate
				};
				this.recurringList.push(newItem);
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
				type: 'expense',
				amount: '',
				categoryId: null,
				frequency: 'monthly',
				executionDate: Date.now(),
				status: 'active'
			};
		}
	}
};
</script>

<style lang="scss" scoped>
.recurring-page {
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
	font-size: 20px;
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

.recurring-item {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
	
	.item-name {
		font-size: 16px;
		font-weight: 600;
		color: #333;
	}
}

.item-body {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
}

.item-info {
	display: flex;
	align-items: center;
	gap: 8px;
	
	.item-amount {
		font-size: 18px;
		font-weight: 700;
		color: #059669;
	}
	
	.item-type {
		font-size: 12px;
		padding: 2px 8px;
		background: #dcfce7;
		color: #10b981;
		border-radius: 10px;
	}
}

.item-meta {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 4px;
	
	.item-frequency {
		font-size: 13px;
		color: #6b7280;
	}
	
	.item-next {
		font-size: 12px;
		color: #9ca3af;
	}
}

.item-actions {
	display: flex;
	gap: 8px;
	justify-content: flex-end;
}

.modal-content {
	padding: 10px 0;
}
</style>
