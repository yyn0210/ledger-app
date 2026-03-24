<template>
	<view class="create-page">
		<!-- 顶部导航 -->
		<view class="header">
			<u-icon name="arrow-left" size="24" color="#fff" @click="goBack"></u-icon>
			<text class="title">快速记账</text>
			<view style="width: 24px"></view>
		</view>

		<!-- 表单区域 -->
		<view class="form-section">
			<!-- 类型切换 -->
			<view class="type-switch">
				<view
					class="type-btn expense"
					:class="{ active: formData.type === 'expense' }"
					@click="formData.type = 'expense'"
				>
					<u-icon name="close-circle" size="20"></u-icon>
					<text>支出</text>
				</view>
				<view
					class="type-btn income"
					:class="{ active: formData.type === 'income' }"
					@click="formData.type = 'income'"
				>
					<u-icon name="checkmark-circle" size="20"></u-icon>
					<text>收入</text>
				</view>
			</view>

			<!-- 金额输入 -->
			<view class="amount-section">
				<text class="amount-symbol">¥</text>
				<input
					class="amount-input"
					type="digit"
					v-model="formData.amount"
					placeholder="0.00"
					:focus="true"
				/>
			</view>

			<!-- 分类选择 -->
			<view class="form-item">
				<view class="form-label">
					<u-icon name="grid" size="20" color="#666"></u-icon>
					<text>分类</text>
				</view>
				<view class="form-value" @click="showCategoryPicker = true">
					<text v-if="formData.categoryName" class="selected-text">{{ formData.categoryName }}</text>
					<text v-else class="placeholder">选择分类</text>
					<u-icon name="arrow-right" size="16" color="#999"></u-icon>
				</view>
			</view>

			<!-- 账户选择 -->
			<view class="form-item">
				<view class="form-label">
					<u-icon name="wallet" size="20" color="#666"></u-icon>
					<text>账户</text>
				</view>
				<view class="form-value" @click="showAccountPicker = true">
					<text v-if="formData.accountName" class="selected-text">{{ formData.accountName }}</text>
					<text v-else class="placeholder">选择账户</text>
					<u-icon name="arrow-right" size="16" color="#999"></u-icon>
				</view>
			</view>

			<!-- 日期选择 -->
			<view class="form-item">
				<view class="form-label">
					<u-icon name="calendar" size="20" color="#666"></u-icon>
					<text>日期</text>
				</view>
				<view class="form-value" @click="showDatePicker = true">
					<text class="selected-text">{{ formData.date }}</text>
					<u-icon name="arrow-right" size="16" color="#999"></u-icon>
				</view>
			</view>

			<!-- 备注输入 -->
			<view class="form-item">
				<view class="form-label">
					<u-icon name="edit-pen" size="20" color="#666"></u-icon>
					<text>备注</text>
				</view>
				<input
					class="form-input"
					v-model="formData.note"
					placeholder="输入备注（可选）"
					maxlength="50"
				/>
			</view>

			<!-- 图片上传 -->
			<view class="form-item">
				<view class="form-label">
					<u-icon name="camera" size="20" color="#666"></u-icon>
					<text>凭证</text>
				</view>
				<view class="image-upload" @click="chooseImage">
					<u-upload
						:fileList="imageList"
						@afterRead="afterRead"
						@delete="deleteImage"
						:preview="true"
						:maxCount="3"
					></u-upload>
				</view>
			</view>

			<!-- 语音输入 -->
			<view class="voice-input" @click="startVoiceInput">
				<u-icon name="mic" size="24" color="#4F46E5"></u-icon>
				<text>语音输入</text>
			</view>
		</view>

		<!-- 底部按钮 -->
		<view class="footer">
			<u-button
				type="primary"
				size="large"
				:disabled="!canSubmit"
				@click="handleSubmit"
				:custom-style="{
					height: '50px',
					borderRadius: '25px',
					fontSize: '16px',
					fontWeight: '600'
				}"
			>
				保存
			</u-button>
		</view>

		<!-- 分类选择器 -->
		<u-popup v-model="showCategoryPicker" mode="bottom" :round="20">
			<view class="picker-header">
				<text class="picker-title">选择分类</text>
				<u-icon name="close" size="20" @click="showCategoryPicker = false"></u-icon>
			</view>
			<view class="category-grid">
				<view
					v-for="cat in categoryList"
					:key="cat.id"
					class="category-item"
					:class="{ selected: formData.categoryId === cat.id }"
					@click="selectCategory(cat)"
				>
					<view class="category-icon" :style="{ backgroundColor: cat.color }">
						<u-icon :name="cat.icon" size="24" color="#fff"></u-icon>
					</view>
					<text class="category-name">{{ cat.name }}</text>
				</view>
			</view>
		</u-popup>

		<!-- 账户选择器 -->
		<u-popup v-model="showAccountPicker" mode="bottom" :round="20">
			<view class="picker-header">
				<text class="picker-title">选择账户</text>
				<u-icon name="close" size="20" @click="showAccountPicker = false"></u-icon>
			</view>
			<view class="account-list">
				<view
					v-for="acc in accountList"
					:key="acc.id"
					class="account-item"
					:class="{ selected: formData.accountId === acc.id }"
					@click="selectAccount(acc)"
				>
					<view class="account-icon" :style="{ backgroundColor: acc.color }">
						<u-icon :name="acc.icon" size="20" color="#fff"></u-icon>
					</view>
					<view class="account-info">
						<text class="account-name">{{ acc.name }}</text>
						<text class="account-balance">余额：¥{{ acc.balance }}</text>
					</view>
					<u-icon v-if="formData.accountId === acc.id" name="checkmark-circle" size="20" color="#4F46E5"></u-icon>
				</view>
			</view>
		</u-popup>

		<!-- 日期选择器 -->
		<u-popup v-model="showDatePicker" mode="bottom" :round="20">
			<view class="picker-header">
				<text class="picker-title">选择日期</text>
				<u-button text @click="showDatePicker = false">完成</u-button>
			</view>
			<u-datetime-picker
				v-model="currentDate"
				mode="date"
				:show-title="false"
				@confirm="confirmDate"
				@cancel="showDatePicker = false"
			></u-datetime-picker>
		</u-popup>
	</view>
</template>

<script>
import { ref, reactive, computed } from 'vue';

export default {
	data() {
		return {
			formData: reactive({
				type: 'expense',
				amount: '',
				categoryId: null,
				categoryName: '',
				accountId: null,
				accountName: '',
				date: this.formatDate(new Date()),
				note: '',
				images: []
			}),
			imageList: [],
			showCategoryPicker: false,
			showAccountPicker: false,
			showDatePicker: false,
			currentDate: Date.now(),
			categoryList: [],
			accountList: []
		};
	},
	computed: {
		canSubmit() {
			return (
				this.formData.amount &&
				parseFloat(this.formData.amount) > 0 &&
				this.formData.categoryId &&
				this.formData.accountId
			);
		}
	},
	onLoad() {
		this.loadCategories();
		this.loadAccounts();
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},
		formatDate(date) {
			const d = new Date(date);
			const year = d.getFullYear();
			const month = String(d.getMonth() + 1).padStart(2, '0');
			const day = String(d.getDate()).padStart(2, '0');
			return `${year}-${month}-${day}`;
		},
		loadCategories() {
			// Mock 分类数据
			this.categoryList = [
				{ id: 1, name: '餐饮', icon: 'fastfood', color: '#ff9900' },
				{ id: 2, name: '交通', icon: 'car', color: '#3385ff' },
				{ id: 3, name: '购物', icon: 'cart', color: '#ff6b6b' },
				{ id: 4, name: '娱乐', icon: 'beer', color: '#9b59b6' },
				{ id: 5, name: '居住', icon: 'home', color: '#52c41a' },
				{ id: 6, name: '医疗', icon: 'heart', color: '#e74c3c' },
				{ id: 7, name: '教育', icon: 'school', color: '#16a085' },
				{ id: 8, name: '其他', icon: 'bag', color: '#34495e' }
			];
		},
		loadAccounts() {
			// Mock 账户数据
			this.accountList = [
				{ id: 1, name: '钱包', icon: 'wallet', color: '#52c41a', balance: 1500.00 },
				{ id: 2, name: '招商银行卡', icon: 'card', color: '#3385ff', balance: 25680.50 },
				{ id: 3, name: '支付宝', icon: 'logo-alipay', color: '#1677ff', balance: 8920.30 },
				{ id: 4, name: '微信零钱', icon: 'wechat', color: '#07c160', balance: 2350.80 }
			];
		},
		selectCategory(cat) {
			this.formData.categoryId = cat.id;
			this.formData.categoryName = cat.name;
			this.showCategoryPicker = false;
		},
		selectAccount(acc) {
			this.formData.accountId = acc.id;
			this.formData.accountName = acc.name;
			this.showAccountPicker = false;
		},
		confirmDate(e) {
			this.formData.date = this.formatDate(e.detail.value);
			this.showDatePicker = false;
		},
		chooseImage() {
			uni.chooseImage({
				count: 3 - this.imageList.length,
				sourceType: ['album', 'camera'],
				success: (res) => {
					res.tempFilePaths.forEach(path => {
						this.imageList.push({ url: path });
						this.formData.images.push(path);
					});
				}
			});
		},
		afterRead(event) {
			const { file } = event;
			this.imageList.push(file);
		},
		deleteImage(event) {
			this.imageList.splice(event.index, 1);
		},
		startVoiceInput() {
			uni.showToast({ title: '语音输入开发中', icon: 'none' });
		},
		handleSubmit() {
			uni.showLoading({ title: '保存中...' });
			
			setTimeout(() => {
				uni.hideLoading();
				uni.showToast({ title: '记账成功', icon: 'success' });
				
				// 清空表单
				this.formData.amount = '';
				this.formData.categoryId = null;
				this.formData.categoryName = '';
				this.formData.note = '';
				this.imageList = [];
				this.formData.images = [];
				
				// 返回上一页或跳转
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			}, 1000);
		}
	}
};
</script>

<style lang="scss" scoped>
.create-page {
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

.form-section {
	background: #fff;
	border-radius: 20px 20px 0 0;
	padding: 20px;
	margin-top: -20px;
}

.type-switch {
	display: flex;
	gap: 16px;
	margin-bottom: 24px;

	.type-btn {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8px;
		padding: 12px;
		border-radius: 12px;
		background: #F5F6F7;
		font-size: 15px;
		color: #666;
		transition: all 0.3s;

		&.expense.active {
			background: #FEF2F2;
			color: #EF4444;
		}

		&.income.active {
			background: #F0FDF4;
			color: #10B981;
		}
	}
}

.amount-section {
	display: flex;
	align-items: baseline;
	justify-content: center;
	padding: 30px 0;
	border-bottom: 1px solid #F3F4F6;

	.amount-symbol {
		font-size: 24px;
		color: #EF4444;
		font-weight: 600;
		margin-right: 8px;
	}

	.amount-input {
		font-size: 48px;
		font-weight: 600;
		color: #1F2937;
		text-align: center;
		flex: 1;
		max-width: 300px;
	}
}

.form-item {
	display: flex;
	align-items: center;
	padding: 16px 0;
	border-bottom: 1px solid #F3F4F6;

	.form-label {
		display: flex;
		align-items: center;
		gap: 8px;
		width: 80px;

		text {
			font-size: 15px;
			color: #666;
		}
	}

	.form-value {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 12px 16px;
		background: #F9FAFB;
		border-radius: 10px;

		.selected-text {
			font-size: 15px;
			color: #1F2937;
		}

		.placeholder {
			font-size: 15px;
			color: #9CA3AF;
		}
	}

	.form-input {
		flex: 1;
		font-size: 15px;
		padding: 12px 16px;
		background: #F9FAFB;
		border-radius: 10px;
	}

	.image-upload {
		flex: 1;
	}
}

.voice-input {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8px;
	padding: 20px;
	margin-top: 20px;
	background: #EEF2FF;
	border-radius: 12px;
	color: #4F46E5;
	font-size: 15px;
}

.footer {
	padding: 20px;
	background: #fff;
}

.picker-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16px 20px;
	border-bottom: 1px solid #F3F4F6;

	.picker-title {
		font-size: 16px;
		font-weight: 600;
		color: #1F2937;
	}
}

.category-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
	padding: 20px;

	.category-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8px;
		padding: 12px 8px;
		border-radius: 12px;
		transition: all 0.3s;

		&.selected {
			background: #EEF2FF;
		}

		.category-icon {
			width: 48px;
			height: 48px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 14px;
		}

		.category-name {
			font-size: 13px;
			color: #666;
		}
	}
}

.account-list {
	padding: 20px;

	.account-item {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 16px;
		border-radius: 12px;
		transition: all 0.3s;

		&.selected {
			background: #EEF2FF;
		}

		.account-icon {
			width: 40px;
			height: 40px;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 10px;
		}

		.account-info {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 4px;

			.account-name {
				font-size: 15px;
				color: #1F2937;
			}

			.account-balance {
				font-size: 13px;
				color: #9CA3AF;
			}
		}
	}
}
</style>
