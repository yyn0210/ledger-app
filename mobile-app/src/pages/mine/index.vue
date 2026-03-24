<template>
	<view class="container">
		<!-- 用户信息卡片 -->
		<view class="user-card">
			<view class="avatar">
				<u-avatar :size="70" :src="userInfo.avatar || ''"></u-avatar>
			</view>
			<view class="user-info">
				<text class="username">{{ userInfo.username }}</text>
				<text class="user-desc">{{ userInfo.description }}</text>
			</view>
			<u-icon name="arrow-right" size="20" color="#9CA3AF"></u-icon>
		</view>

		<!-- 统计卡片 -->
		<view class="stats-card">
			<view class="stat-item">
				<text class="stat-value">{{ stats.totalDays }}</text>
				<text class="stat-label">记账天数</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<text class="stat-value">{{ stats.totalTransactions }}</text>
				<text class="stat-label">交易笔数</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<text class="stat-value">{{ stats.totalAmount }}</text>
				<text class="stat-label">累计金额</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="menu-list">
			<view class="menu-group">
				<text class="group-title">账本管理</text>
				<view class="menu-item" @click="goToPage('/pages/book/index')">
					<view class="menu-left">
						<u-icon name="book" size="20" color="#4F46E5"></u-icon>
						<text class="menu-label">账本切换</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="showToast('分类管理开发中')">
					<view class="menu-left">
						<u-icon name="folder" size="20" color="#10B981"></u-icon>
						<text class="menu-label">分类管理</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
			</view>

			<view class="menu-group">
				<text class="group-title">数据统计</text>
				<view class="menu-item" @click="showToast('统计报表开发中')">
					<view class="menu-left">
						<u-icon name="piechart" size="20" color="#F59E0B"></u-icon>
						<text class="menu-label">统计报表</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="showToast('预算管理开发中')">
					<view class="menu-left">
						<u-icon name="budget" size="20" color="#3B82F6"></u-icon>
						<text class="menu-label">预算管理</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
			</view>

			<view class="menu-group">
				<text class="group-title">设置</text>
				<view class="menu-item" @click="showToast('账户设置开发中')">
					<view class="menu-left">
						<u-icon name="setting" size="20" color="#6B7280"></u-icon>
						<text class="menu-label">账户设置</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
				<view class="menu-item" @click="showToast('关于我们开发中')">
					<view class="menu-left">
						<u-icon name="info-circle" size="20" color="#6B7280"></u-icon>
						<text class="menu-label">关于我们</text>
					</view>
					<u-icon name="arrow-right" size="18" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 版本信息 -->
		<view class="version-info">
			<text class="version">Version 1.0.0</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			userInfo: {
				username: '简洛用户',
				description: '精简生活，逻辑理财',
				avatar: ''
			},
			stats: {
				totalDays: 30,
				totalTransactions: 156,
				totalAmount: '23.5K'
			}
		};
	},
	methods: {
		goToPage(url) {
			uni.navigateTo({ url });
		},
		showToast(message) {
			uni.showToast({ title: message, icon: 'none' });
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

.user-card {
	background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
	border-radius: 16px;
	padding: 24px 16px;
	display: flex;
	align-items: center;
	gap: 16px;
	color: #fff;
	margin-bottom: 16px;

	.avatar {
		flex-shrink: 0;
	}

	.user-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 4px;

		.username {
			font-size: 18px;
			font-weight: 600;
		}

		.user-desc {
			font-size: 13px;
			opacity: 0.8;
		}
	}
}

.stats-card {
	background: #fff;
	border-radius: 12px;
	padding: 20px 16px;
	display: flex;
	justify-content: space-around;
	align-items: center;
	margin-bottom: 16px;

	.stat-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 4px;

		.stat-value {
			font-size: 20px;
			font-weight: 600;
			color: #4F46E5;
		}

		.stat-label {
			font-size: 12px;
			color: #9CA3AF;
		}
	}

	.stat-divider {
		width: 1px;
		height: 40px;
		background: #F3F4F6;
	}
}

.menu-list {
	.menu-group {
		background: #fff;
		border-radius: 12px;
		padding: 16px;
		margin-bottom: 16px;

		.group-title {
			display: block;
			font-size: 13px;
			color: #9CA3AF;
			margin-bottom: 12px;
		}

		.menu-item {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 12px 0;
			border-bottom: 1px solid #F3F4F6;

			&:last-child {
				border-bottom: none;
			}

			.menu-left {
				display: flex;
				align-items: center;
				gap: 12px;

				.menu-label {
					font-size: 15px;
					color: #1F2937;
				}
			}
		}
	}
}

.version-info {
	text-align: center;
	padding: 20px 0;

	.version {
		font-size: 12px;
		color: #9CA3AF;
	}
}
</style>
