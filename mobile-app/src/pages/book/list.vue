<template>
	<view class="book-list-page">
		<!-- 顶部导航 -->
		<view class="header">
			<u-icon name="arrow-left" size="24" color="#fff" @click="goBack"></u-icon>
			<text class="title">账本列表</text>
			<view style="width: 24px"></view>
		</view>

		<!-- 账本列表 -->
		<view class="book-list">
			<view
				class="book-item"
				v-for="(book, index) in bookList"
				:key="index"
				@click="selectBook(book)"
			>
				<view class="book-card" :class="{ active: book.isCurrent }">
					<view class="book-icon" :style="{ backgroundColor: book.color }">
						<u-icon :name="book.icon" size="28" color="#fff"></u-icon>
					</view>
					<view class="book-info">
						<text class="book-name">{{ book.name }}</text>
						<text class="book-desc">{{ book.description || '暂无描述' }}</text>
						<view class="book-stats">
							<text class="stat">{{ book.transactionCount || 0 }}笔交易</text>
							<text class="stat">{{ book.memberCount || 1 }}人</text>
						</view>
					</view>
					<view class="book-action">
						<u-icon
							v-if="book.isCurrent"
							name="checkmark-circle"
							size="24"
							color="#4F46E5"
						></u-icon>
						<u-icon v-else name="arrow-right" size="20" color="#9CA3AF"></u-icon>
					</view>
				</view>
			</view>

			<!-- 添加账本 -->
			<view class="add-book" @click="showAddModal = true">
				<view class="add-icon">
					<u-icon name="plus" size="24" color="#4F46E5"></u-icon>
				</view>
				<text class="add-label">添加账本</text>
			</view>
		</view>

		<!-- 添加账本弹窗 -->
		<u-popup v-model="showAddModal" mode="bottom" :round="20">
			<view class="modal-content">
				<view class="modal-header">
					<text class="modal-title">新建账本</text>
					<u-icon name="close" size="20" @click="showAddModal = false"></u-icon>
				</view>

				<view class="form-section">
					<u-form :model="formData" label-width="80">
						<u-form-item label="账本名称">
							<u-input
								v-model="formData.name"
								placeholder="请输入账本名称"
								maxlength="20"
							></u-input>
						</u-form-item>

						<u-form-item label="账本描述">
							<u-input
								v-model="formData.description"
								placeholder="请输入描述（可选）"
								maxlength="50"
							></u-input>
						</u-form-item>

						<u-form-item label="图标颜色">
							<view class="color-picker">
								<view
									v-for="color in colors"
									:key="color"
									class="color-option"
									:class="{ selected: formData.color === color }"
									:style="{ backgroundColor: color }"
									@click="formData.color = color"
								></view>
							</view>
						</u-form-item>

						<u-form-item label="账本图标">
							<view class="icon-picker">
								<view
									v-for="icon in icons"
									:key="icon.value"
									class="icon-option"
									:class="{ selected: formData.icon === icon.value }"
									@click="formData.icon = icon.value"
								>
									<u-icon :name="icon.value" size="24"></u-icon>
								</view>
							</view>
						</u-form-item>
					</u-form>
				</view>

				<view class="modal-footer">
					<u-button
						type="primary"
						size="large"
						:disabled="!formData.name"
						@click="handleAddBook"
						:custom-style="{
							height: '50px',
							borderRadius: '25px'
						}"
					>
						创建账本
					</u-button>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
export default {
	data() {
		return {
			showAddModal: false,
			bookList: [],
			formData: {
				name: '',
				description: '',
				color: '#4F46E5',
				icon: 'book'
			},
			colors: ['#4F46E5', '#10B981', '#EF4444', '#F59E0B', '#3B82F6', '#8B5CF6', '#EC4899'],
			icons: [
				{ value: 'book', label: '账本' },
				{ value: 'wallet', label: '钱包' },
				{ value: 'home', label: '家庭' },
				{ value: 'car', label: '旅行' },
				{ value: 'cart', label: '购物' },
				{ value: 'heart', label: '医疗' },
				{ value: 'school', label: '教育' }
			]
		};
	},
	onLoad() {
		this.loadBooks();
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},
		loadBooks() {
			// Mock 数据
			this.bookList = [
				{ id: 1, name: '日常账本', description: '记录日常生活开支', color: '#4F46E5', icon: 'book', transactionCount: 156, memberCount: 1, isCurrent: true },
				{ id: 2, name: '旅行账本', description: '旅行费用专用', color: '#10B981', icon: 'car', transactionCount: 28, memberCount: 1, isCurrent: false },
				{ id: 3, name: '装修账本', description: '新房装修预算', color: '#F59E0B', icon: 'home', transactionCount: 45, memberCount: 2, isCurrent: false }
			];
		},
		selectBook(book) {
			if (book.isCurrent) return;
			
			// 切换账本
			this.bookList.forEach(b => b.isCurrent = false);
			book.isCurrent = true;
			
			uni.showToast({
				title: `已切换到${book.name}`,
				icon: 'success'
			});
			
			// 返回上一页
			setTimeout(() => {
				uni.navigateBack();
			}, 1000);
		},
		handleAddBook() {
			if (!this.formData.name) {
				uni.showToast({ title: '请输入账本名称', icon: 'none' });
				return;
			}
			
			this.bookList.push({
				id: Date.now(),
				name: this.formData.name,
				description: this.formData.description,
				color: this.formData.color,
				icon: this.formData.icon,
				transactionCount: 0,
				memberCount: 1,
				isCurrent: false
			});
			
			uni.showToast({ title: '创建成功', icon: 'success' });
			this.showAddModal = false;
			this.formData = { name: '', description: '', color: '#4F46E5', icon: 'book' };
		}
	}
};
</script>

<style lang="scss" scoped>
.book-list-page {
	min-height: 100vh;
	background: #F5F6F7;
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40px 20px 20px;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

	.title {
		font-size: 20px;
		font-weight: 600;
		color: #fff;
	}
}

.book-list {
	padding: 20px;

	.book-item {
		margin-bottom: 16px;
	}

	.book-card {
		display: flex;
		align-items: center;
		background: #fff;
		border-radius: 16px;
		padding: 16px;
		transition: all 0.3s;
		border: 2px solid transparent;

		&.active {
			border-color: #4F46E5;
			background: #EEF2FF;
		}

		.book-icon {
			width: 56px;
			height: 56px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 14px;
			margin-right: 16px;
			flex-shrink: 0;
		}

		.book-info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 4px;

			.book-name {
				font-size: 16px;
				font-weight: 600;
				color: #1F2937;
			}

			.book-desc {
				font-size: 13px;
				color: #9CA3AF;
			}

			.book-stats {
				display: flex;
				gap: 12px;
				margin-top: 4px;

				.stat {
					font-size: 12px;
					color: #6B7280;
				}
			}
		}

		.book-action {
			display: flex;
			align-items: center;
			margin-left: 12px;
		}
	}
}

.add-book {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: #fff;
	border-radius: 16px;
	padding: 40px 20px;
	border: 2px dashed #E5E7EB;

	.add-icon {
		width: 56px;
		height: 56px;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 50%;
		background: #EEF2FF;
		margin-bottom: 12px;
	}

	.add-label {
		font-size: 14px;
		color: #4F46E5;
	}
}

.modal-content {
	padding: 20px;

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;

		.modal-title {
			font-size: 18px;
			font-weight: 600;
			color: #1F2937;
		}
	}

	.form-section {
		margin-bottom: 20px;
	}

	.color-picker {
		display: flex;
		gap: 12px;
		flex-wrap: wrap;

		.color-option {
			width: 40px;
			height: 40px;
			border-radius: 10px;
			cursor: pointer;
			transition: all 0.3s;
			border: 2px solid transparent;

			&.selected {
				border-color: #1F2937;
				transform: scale(1.1);
			}
		}
	}

	.icon-picker {
		display: flex;
		gap: 12px;
		flex-wrap: wrap;

		.icon-option {
			width: 44px;
			height: 44px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 10px;
			background: #F9FAFB;
			cursor: pointer;
			transition: all 0.3s;
			border: 2px solid transparent;

			&.selected {
				background: #EEF2FF;
				border-color: #4F46E5;
			}
		}
	}

	.modal-footer {
		margin-top: 20px;
	}
}
</style>
