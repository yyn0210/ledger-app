<template>
	<view class="mine-page">
		<!-- 用户信息卡片 -->
		<view class="user-card">
			<view class="user-info">
				<view class="avatar">
					<u-avatar :text="userInfo.name.charAt(0)" size="80" :bg-color="userInfo.avatarColor"></u-avatar>
				</view>
				<view class="user-details">
					<text class="user-name">{{ userInfo.name }}</text>
					<text class="user-id">ID: {{ userInfo.id }}</text>
					<text class="user-email">{{ userInfo.email || '未设置邮箱' }}</text>
				</view>
			</view>
			<view class="user-stats">
				<view class="stat-item">
					<text class="stat-value">{{ stats.totalDays }}</text>
					<text class="stat-label">记账天数</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item">
					<text class="stat-value">{{ stats.transactionCount }}</text>
					<text class="stat-label">交易笔数</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item">
					<text class="stat-value">¥{{ stats.totalAsset }}</text>
					<text class="stat-label">总资产</text>
				</view>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="menu-section">
			<view class="section-title">我的账本</view>
			<view class="menu-list">
				<view class="menu-item" @click="goToBookList">
					<view class="menu-icon" style="background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%)">
						<u-icon name="book" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">账本管理</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="goToBudget">
					<view class="menu-icon" style="background: linear-gradient(135deg, #10B981 0%, #34D399 100%)">
						<u-icon name="pie-chart" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">预算管理</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<view class="menu-section">
			<view class="section-title">数据管理</view>
			<view class="menu-list">
				<view class="menu-item" @click="exportData">
					<view class="menu-icon" style="background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)">
						<u-icon name="download" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">导出数据</text>
					<text class="menu-desc">Excel/CSV</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="importData">
					<view class="menu-icon" style="background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%)">
						<u-icon name="upload" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">导入数据</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="backupData">
					<view class="menu-icon" style="background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%)">
						<u-icon name="cloud-upload" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">备份恢复</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<view class="menu-section">
			<view class="section-title">设置</view>
			<view class="menu-list">
				<view class="menu-item" @click="goToSettings">
					<view class="menu-icon" style="background: linear-gradient(135deg, #6B7280 0%, #9CA3AF 100%)">
						<u-icon name="setting" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">通用设置</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="goToCategory">
					<view class="menu-icon" style="background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%)">
						<u-icon name="grid" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">分类管理</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="goToAccount">
					<view class="menu-icon" style="background: linear-gradient(135deg, #06B6D4 0%, #22D3EE 100%)">
						<u-icon name="wallet" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">账户管理</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<view class="menu-section">
			<view class="menu-list">
				<view class="menu-item" @click="checkUpdate">
					<view class="menu-icon" style="background: linear-gradient(135deg, #14B8A6 0%, #2DD4BF 100%)">
						<u-icon name="refresh" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">检查更新</text>
					<text class="menu-desc">v{{ appVersion }}</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="showAbout">
					<view class="menu-icon" style="background: linear-gradient(135deg, #F97316 0%, #FB923C 100%)">
						<u-icon name="info-circle" size="20" color="#fff"></u-icon>
					</view>
					<text class="menu-label">关于我们</text>
					<u-icon name="arrow-right" size="16" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 退出登录 -->
		<view class="logout-section">
			<u-button
				type="error"
				size="large"
				@click="handleLogout"
				:custom-style="{
					height: '50px',
					borderRadius: '25px',
					background: 'linear-gradient(135deg, #EF4444 0%, #F87171 100%)'
				}"
			>
				退出登录
			</u-button>
		</view>

		<!-- 关于弹窗 -->
		<u-modal
			v-model="showAboutModal"
			title="关于简洛记账"
			:show-cancel-button="false"
			@confirm="showAboutModal = false"
		>
			<view class="about-content">
				<text class="about-title">简洛记账</text>
				<text class="about-version">版本 {{ appVersion }}</text>
				<text class="about-desc">简单、优雅的记账工具</text>
				<view class="about-info">
					<text>© 2026 Jianluo Team</text>
					<text>All Rights Reserved</text>
				</view>
			</view>
		</u-modal>
	</view>
</template>

<script>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

export default {
	data() {
		return {
			showAboutModal: false,
			appVersion: '1.0.0',
			userInfo: {
				id: 'U001',
				name: '新用户',
				email: '',
				avatarColor: '#4F46E5'
			},
			stats: {
				totalDays: 0,
				transactionCount: 0,
				totalAsset: '0.00'
			}
		};
	},
	onLoad() {
		this.loadUserInfo();
	},
	methods: {
		loadUserInfo() {
			// 从本地存储加载用户信息
			const token = uni.getStorageSync('token');
			if (token) {
				// Mock 用户信息
				this.userInfo = {
					id: 'U' + Date.now().toString().slice(-6),
					name: '记账达人',
					email: 'user@example.com',
					avatarColor: '#4F46E5'
				};
				this.stats = {
					totalDays: 30,
					transactionCount: 156,
					totalAsset: '12,580.00'
				};
			}
		},
		goToBookList() {
			uni.navigateTo({ url: '/pages/book/list' });
		},
		goToBudget() {
			uni.showToast({ title: '预算管理开发中', icon: 'none' });
		},
		exportData() {
			uni.showActionSheet({
				itemList: ['导出为 Excel', '导出为 CSV'],
				success: (res) => {
					uni.showToast({
						title: '开始导出...',
						icon: 'none'
					});
				}
			});
		},
		importData() {
			uni.showToast({ title: '数据导入开发中', icon: 'none' });
		},
		backupData() {
			uni.showToast({ title: '备份恢复开发中', icon: 'none' });
		},
		goToSettings() {
			uni.showToast({ title: '设置页面开发中', icon: 'none' });
		},
		goToCategory() {
			uni.showToast({ title: '分类管理开发中', icon: 'none' });
		},
		goToAccount() {
			uni.showToast({ title: '账户管理开发中', icon: 'none' });
		},
		checkUpdate() {
			uni.showLoading({ title: '检查中...' });
			setTimeout(() => {
				uni.hideLoading();
				uni.showToast({
					title: '已是最新版本',
					icon: 'success'
				});
			}, 1000);
		},
		showAbout() {
			this.showAboutModal = true;
		},
		handleLogout() {
			uni.showModal({
				title: '确认退出',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						// 清除 token
						uni.removeStorageSync('token');
						// 返回登录页
						uni.reLaunch({ url: '/pages/login/login' });
					}
				}
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.mine-page {
	min-height: 100vh;
	background: #F5F6F7;
}

.user-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40px 20px 20px;
	margin-bottom: 12px;

	.user-info {
		display: flex;
		align-items: center;
		margin-bottom: 24px;

		.avatar {
			margin-right: 16px;
		}

		.user-details {
			display: flex;
			flex-direction: column;
			gap: 4px;

			.user-name {
				font-size: 20px;
				font-weight: 600;
				color: #fff;
			}

			.user-id {
				font-size: 12px;
				color: rgba(255, 255, 255, 0.7);
			}

			.user-email {
				font-size: 13px;
				color: rgba(255, 255, 255, 0.8);
			}
		}
	}

	.user-stats {
		display: flex;
		background: rgba(255, 255, 255, 0.15);
		border-radius: 16px;
		padding: 16px;

		.stat-item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 4px;

			.stat-value {
				font-size: 18px;
				font-weight: 600;
				color: #fff;
			}

			.stat-label {
				font-size: 12px;
				color: rgba(255, 255, 255, 0.7);
			}
		}

		.stat-divider {
			width: 1px;
			background: rgba(255, 255, 255, 0.3);
			margin: 0 8px;
		}
	}
}

.menu-section {
	margin-bottom: 12px;

	.section-title {
		font-size: 14px;
		color: #6B7280;
		padding: 12px 16px 8px;
	}
}

.menu-list {
	background: #fff;
	border-radius: 12px;
	margin: 0 16px;
	overflow: hidden;

	.menu-item {
		display: flex;
		align-items: center;
		padding: 16px;
		border-bottom: 1px solid #F3F4F6;

		&:last-child {
			border-bottom: none;
		}

		.menu-icon {
			width: 40px;
			height: 40px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 10px;
			margin-right: 12px;
			flex-shrink: 0;
		}

		.menu-label {
			flex: 1;
			font-size: 15px;
			color: #1F2937;
		}

		.menu-desc {
			font-size: 12px;
			color: #9CA3AF;
			margin-right: 8px;
		}
	}
}

.logout-section {
	padding: 24px 16px 40px;
}

.about-content {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20px 0;
	gap: 8px;

	.about-title {
		font-size: 20px;
		font-weight: 600;
		color: #1F2937;
	}

	.about-version {
		font-size: 14px;
		color: #6B7280;
	}

	.about-desc {
		font-size: 13px;
		color: #9CA3AF;
		margin-top: 8px;
	}

	.about-info {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 4px;
		margin-top: 16px;
		font-size: 12px;
		color: #9CA3AF;
	}
}
</style>
