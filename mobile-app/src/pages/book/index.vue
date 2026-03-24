<template>
	<view class="container">
		<!-- 账本列表 -->
		<view class="book-list">
			<view class="book-item" v-for="(book, index) in bookList" :key="index" @click="selectBook(book)">
				<view class="book-icon" :style="{ background: book.color }">
					<u-icon :name="book.icon" size="24" color="#fff"></u-icon>
				</view>
				<view class="book-info">
					<text class="book-name">{{ book.name }}</text>
					<text class="book-desc">{{ book.description }}</text>
				</view>
				<view class="book-action">
					<u-icon v-if="book.isCurrent" name="checkmark-circle" size="24" color="#4F46E5"></u-icon>
					<u-icon v-else name="arrow-right" size="20" color="#9CA3AF"></u-icon>
				</view>
			</view>
		</view>

		<!-- 添加账本按钮 -->
		<view class="add-book">
			<u-button type="primary" @click="showAddModal = true" :custom-style="{
				background: '#fff',
				color: '#4F46E5',
				border: '1px dashed #4F46E5',
				height: '48px',
				borderRadius: '12px'
			}">
				<u-icon name="plus" size="18"></u-icon>
				添加账本
			</u-button>
		</view>

		<!-- 添加账本弹窗 -->
		<u-modal v-model="showAddModal" title="添加账本" :show-cancel-button="true" @confirm="handleAddBook" @cancel="showAddModal = false">
			<view class="modal-content">
				<u-form :model="newBook" label-width="70">
					<u-form-item label="名称" prop="name">
						<u-input v-model="newBook.name" placeholder="请输入账本名称"></u-input>
					</u-form-item>
					<u-form-item label="描述" prop="description">
						<u-input v-model="newBook.description" placeholder="请输入账本描述"></u-input>
					</u-form-item>
					<u-form-item label="颜色" prop="color">
						<view class="color-picker">
							<view class="color-option" v-for="color in colorOptions" :key="color" 
								:style="{ background: color }"
								:class="{ selected: newBook.color === color }"
								@click="newBook.color = color">
							</view>
						</view>
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
			showAddModal: false,
			bookList: [],
			newBook: {
				name: '',
				description: '',
				color: '#4F46E5',
				icon: 'book'
			},
			colorOptions: ['#4F46E5', '#10B981', '#EF4444', '#F59E0B', '#3B82F6', '#8B5CF6']
		};
	},
	onLoad() {
		this.loadMockData();
	},
	methods: {
		loadMockData() {
			this.bookList = [
				{ id: 1, name: '日常账本', description: '记录日常生活开支', color: '#4F46E5', icon: 'book', isCurrent: true },
				{ id: 2, name: '旅行账本', description: '旅行费用专用', color: '#10B981', icon: 'map', isCurrent: false },
				{ id: 3, name: '装修账本', description: '新房装修预算', color: '#F59E0B', icon: 'home', isCurrent: false }
			];
		},
		selectBook(book) {
			this.bookList.forEach(b => b.isCurrent = false);
			book.isCurrent = true;
			uni.showToast({ title: `已切换到${book.name}`, icon: 'success' });
		},
		handleAddBook() {
			if (!this.newBook.name) {
				uni.showToast({ title: '请输入账本名称', icon: 'none' });
				return;
			}
			this.bookList.push({
				id: Date.now(),
				...this.newBook,
				icon: 'book',
				isCurrent: false
			});
			uni.showToast({ title: '添加成功', icon: 'success' });
			this.showAddModal = false;
			this.newBook = { name: '', description: '', color: '#4F46E5', icon: 'book' };
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

.book-list {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
	margin-bottom: 16px;

	.book-item {
		display: flex;
		align-items: center;
		padding: 16px 0;
		border-bottom: 1px solid #F3F4F6;

		&:last-child {
			border-bottom: none;
		}

		.book-icon {
			width: 48px;
			height: 48px;
			border-radius: 12px;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-right: 12px;
		}

		.book-info {
			flex: 1;
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
		}

		.book-action {
			display: flex;
			align-items: center;
		}
	}
}

.add-book {
	margin-top: 16px;
}

.modal-content {
	padding: 16px 0;

	.color-picker {
		display: flex;
		gap: 12px;
		flex-wrap: wrap;

		.color-option {
			width: 36px;
			height: 36px;
			border-radius: 8px;
			border: 2px solid transparent;
			cursor: pointer;

			&.selected {
				border-color: #1F2937;
			}
		}
	}
}
</style>
