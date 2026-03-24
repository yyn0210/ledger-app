<template>
	<view class="offline-page">
		<!-- 页面头部 -->
		<view class="page-header">
			<text class="page-title">离线记账</text>
			<u-badge
				:show="pendingCount > 0"
				:value="pendingCount"
				:type="pendingCount > 0 ? 'error' : 'success'"
			></u-badge>
		</view>

		<!-- 网络状态提示 -->
		<view class="network-status" :class="{ offline: !isOnline }">
			<u-notice-bar
				:text="isOnline ? '当前在线，数据将实时同步' : '当前离线，数据将本地保存，联网后自动同步'"
				:mode="isOnline ? 'link' : 'closable'"
				:type="isOnline ? 'success' : 'warning'"
				:scrollable="false"
			></u-notice-bar>
		</view>

		<!-- 统计卡片 -->
		<view class="stats-section">
			<view class="stat-item">
				<view class="stat-icon" style="background: #fef3c7;">
					<u-icon name="cloud-offline" size="24" color="#f59e0b"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ pendingCount }}</text>
					<text class="stat-label">待同步</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #dcfce7;">
					<u-icon name="checkmark-circle" size="24" color="#10b981"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value">{{ syncedCount }}</text>
					<text class="stat-label">已同步</text>
				</view>
			</view>
			<view class="stat-item">
				<view class="stat-icon" style="background: #dbeafe;">
					<u-icon name="refresh" size="24" color="#3b82f6"></u-icon>
				</view>
				<view class="stat-info">
					<text class="stat-value" @click="handleSync">
						<u-icon name="refresh" :class="{ spinning: syncing }"></u-icon>
					</text>
					<text class="stat-label">点击同步</text>
				</view>
			</view>
		</view>

		<!-- 离线数据列表 -->
		<view class="list-section">
			<view class="section-header">
				<text class="section-title">离线记录</text>
				<u-button
					v-if="pendingCount > 0"
					size="small"
					type="primary"
					@click="handleSync"
					:loading="syncing"
				>
					一键同步
				</u-button>
			</view>

			<view
				v-for="item in offlineList"
				:key="item.id"
				class="offline-item"
				:class="{ syncing: item.syncing }"
			>
				<view class="item-header">
					<view class="item-info">
						<text class="item-name">{{ item.name }}</text>
						<text class="item-category">{{ item.category }}</text>
					</view>
					<view class="item-status">
						<u-tag
							:text="item.synced ? '已同步' : '待同步'"
							:type="item.synced ? 'success' : 'warning'"
							size="mini"
							plain
						></u-tag>
					</view>
				</view>
				<view class="item-body">
					<view class="item-amount" :class="item.type">
						{{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
					</view>
					<view class="item-meta">
						<text class="item-date">{{ item.date }}</text>
						<text class="item-time">{{ item.createdAt }}</text>
					</view>
				</view>
				<view class="item-actions" v-if="!item.synced">
					<u-button
						size="mini"
						type="primary"
						@click.stop="handleSyncOne(item)"
						:loading="item.syncing"
					>
						同步
					</u-button>
					<u-button
						size="mini"
						type="error"
						@click.stop="handleDelete(item)"
					>
						删除
					</u-button>
				</view>
			</view>

			<u-empty
				v-if="offlineList.length === 0"
				mode="data"
				text="暂无离线记录"
			></u-empty>
		</view>

		<!-- 快速记账入口 -->
		<view class="quick-add">
			<u-button
				type="primary"
				size="large"
				@click="handleQuickAdd"
				:custom-style="{
					borderRadius: '30px',
					height: '55px'
				}"
			>
				<template #icon>
					<u-icon name="plus" size="20"></u-icon>
				</template>
				快速记账
			</u-button>
		</view>

		<!-- 同步结果提示 -->
		<u-modal
			v-model="showSyncResult"
			title="同步结果"
			:show-cancel-button="false"
			@confirm="showSyncResult = false"
		>
			<view class="sync-result">
				<view class="result-item success">
					<u-icon name="checkmark-circle" color="#10b981"></u-icon>
					<text>成功：{{ syncResult.success }} 条</text>
				</view>
				<view class="result-item error" v-if="syncResult.failed > 0">
					<u-icon name="close-circle" color="#ef4444"></u-icon>
					<text>失败：{{ syncResult.failed }} 条</text>
				</view>
			</view>
		</u-modal>
	</view>
</template>

<script>
export default {
	data() {
		return {
			isOnline: true,
			syncing: false,
			showSyncResult: false,
			syncResult: {
				success: 0,
				failed: 0
			},
			offlineList: []
		};
	},
	computed: {
		pendingCount() {
			return this.offlineList.filter(item => !item.synced).length;
		},
		syncedCount() {
			return this.offlineList.filter(item => item.synced).length;
		}
	},
	onLoad() {
		this.checkNetwork();
		this.loadOfflineData();
		// 监听网络状态变化
		uni.onNetworkStatusChange(this.onNetworkChange);
	},
	onUnload() {
		uni.offNetworkStatusChange(this.onNetworkChange);
	},
	methods: {
		// 检查网络状态
		checkNetwork() {
			uni.getNetworkType({
				success: (res) => {
					this.isOnline = res.networkType !== 'none';
				}
			});
		},

		// 网络状态变化
		onNetworkChange(res) {
			this.isOnline = res.networkType !== 'none';
			
			// 网络恢复时自动同步
			if (this.isOnline && this.pendingCount > 0) {
				uni.showToast({
					title: '网络已恢复，正在同步...',
					icon: 'none'
				});
				setTimeout(() => this.handleSync(), 1000);
			}
		},

		// 加载离线数据
		loadOfflineData() {
			// 从本地存储加载
			const stored = uni.getStorageSync('offline_transactions');
			if (stored) {
				this.offlineList = JSON.parse(stored);
			} else {
				// Mock 数据
				this.offlineList = [
					{
						id: 1,
						name: '午餐',
						category: '餐饮',
						type: 'expense',
						amount: 35,
						date: '2026-03-24',
						createdAt: '12:30',
						synced: false,
						syncing: false
					},
					{
						id: 2,
						name: '地铁',
						category: '交通',
						type: 'expense',
						amount: 6,
						date: '2026-03-24',
						createdAt: '08:30',
						synced: true,
						syncing: false
					},
					{
						id: 3,
						name: '咖啡',
						category: '餐饮',
						type: 'expense',
						amount: 28,
						date: '2026-03-24',
						createdAt: '14:00',
						synced: false,
						syncing: false
					}
				];
				this.saveOfflineData();
			}
		},

		// 保存离线数据
		saveOfflineData() {
			uni.setStorageSync('offline_transactions', JSON.stringify(this.offlineList));
		},

		// 一键同步
		async handleSync() {
			if (this.pendingCount === 0) {
				uni.showToast({
					title: '没有待同步的数据',
					icon: 'none'
				});
				return;
			}

			if (!this.isOnline) {
				uni.showToast({
					title: '当前离线，无法同步',
					icon: 'none'
				});
				return;
			}

			this.syncing = true;
			this.syncResult = { success: 0, failed: 0 };

			// 模拟批量同步
			const pendingItems = this.offlineList.filter(item => !item.synced);
			
			for (let i = 0; i < pendingItems.length; i++) {
				await new Promise(resolve => setTimeout(resolve, 300));
				
				// 模拟同步结果（90% 成功率）
				const success = Math.random() > 0.1;
				
				if (success) {
					const item = this.offlineList.find(it => it.id === pendingItems[i].id);
					if (item) {
						item.synced = true;
						this.syncResult.success++;
					}
				} else {
					this.syncResult.failed++;
				}
			}

			this.saveOfflineData();
			this.syncing = false;
			this.showSyncResult = true;

			uni.showToast({
				title: `同步完成：成功${this.syncResult.success}条`,
				icon: 'success'
			});
		},

		// 单条同步
		async handleSyncOne(item) {
			if (!this.isOnline) {
				uni.showToast({
					title: '当前离线，无法同步',
					icon: 'none'
				});
				return;
			}

			item.syncing = true;
			
			// 模拟同步
			await new Promise(resolve => setTimeout(resolve, 1000));
			
			// 模拟同步成功
			item.synced = true;
			item.syncing = false;
			this.saveOfflineData();

			uni.showToast({
				title: '同步成功',
				icon: 'success'
			});
		},

		// 删除离线记录
		handleDelete(item) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这条离线记录吗？',
				success: (res) => {
					if (res.confirm) {
						this.offlineList = this.offlineList.filter(it => it.id !== item.id);
						this.saveOfflineData();
						uni.showToast({
							title: '已删除',
							icon: 'success'
						});
					}
				}
			});
		},

		// 快速记账
		handleQuickAdd() {
			// 跳转到记账页面
			uni.navigateTo({
				url: '/pages/transaction/add?offline=true'
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.offline-page {
	min-height: 100vh;
	background: #F5F6F7;
	padding: 20px;
	padding-bottom: 80px;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;

	.page-title {
		font-size: 20px;
		font-weight: 700;
		color: #333;
	}
}

.network-status {
	margin-bottom: 16px;
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
	flex-direction: column;
	align-items: center;
	gap: 8px;
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
	align-items: center;
}

.stat-value {
	font-size: 20px;
	font-weight: 700;
	color: #1f2937;
}

.stat-label {
	font-size: 12px;
	color: #6b7280;
}

.list-section {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
	margin-bottom: 16px;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;

	.section-title {
		font-size: 16px;
		font-weight: 600;
		color: #333;
	}
}

.offline-item {
	padding: 16px;
	background: #f9fafb;
	border-radius: 8px;
	margin-bottom: 12px;

	&:last-child {
		margin-bottom: 0;
	}

	&.syncing {
		opacity: 0.6;
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
	flex-direction: column;
	gap: 4px;

	.item-name {
		font-size: 15px;
		font-weight: 600;
		color: #333;
	}

	.item-category {
		font-size: 13px;
		color: #6b7280;
	}
}

.item-body {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
}

.item-amount {
	font-size: 18px;
	font-weight: 700;

	&.income {
		color: #10b981;
	}

	&.expense {
		color: #ef4444;
	}
}

.item-meta {
	display: flex;
	gap: 12px;

	.item-date,
	.item-time {
		font-size: 13px;
		color: #9ca3af;
	}
}

.item-actions {
	display: flex;
	gap: 8px;
	justify-content: flex-end;
}

.quick-add {
	position: fixed;
	bottom: 20px;
	left: 0;
	right: 0;
	padding: 0 20px;
}

.sync-result {
	display: flex;
	flex-direction: column;
	gap: 12px;
	padding: 20px 0;
}

.result-item {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 15px;

	&.success {
		color: #10b981;
	}

	&.error {
		color: #ef4444;
	}
}

.spinning {
	animation: spin 1s linear infinite;
}

@keyframes spin {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}
</style>
