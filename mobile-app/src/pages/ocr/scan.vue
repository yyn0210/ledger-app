<template>
	<view class="ocr-page">
		<!-- 上传区域 -->
		<view class="upload-section" v-if="!hasImage">
			<view class="upload-area" @click="chooseImage">
				<view class="upload-icon">
					<u-icon name="camera" size="60" color="#8b5cf6"></u-icon>
				</view>
				<text class="upload-title">拍照或上传图片</text>
				<text class="upload-desc">支持小票/账单 OCR 识别</text>
				<u-button
					type="primary"
					size="large"
					:custom-style="{
						marginTop: '20px',
						width: '200px',
						borderRadius: '25px'
					}"
				>
					<template #icon>
						<u-icon name="image" size="18"></u-icon>
					</template>
					选择图片
				</u-button>
			</view>
		</view>

		<!-- 图片预览和识别结果 -->
		<view class="content-section" v-else>
			<!-- 图片预览 -->
			<view class="image-preview-card">
				<view class="card-header">
					<text class="card-title">原始图片</text>
					<u-button
						size="small"
						@click="resetUpload"
						:custom-style="{
							background: '#f3f4f6',
							border: 'none'
						}"
					>
						重新上传
					</u-button>
				</view>
				<view class="image-preview">
					<u-image
						:src="imagePreview"
						mode="aspectFit"
						width="100%"
						height="300"
						:show-loading="true"
					></u-image>
				</view>
			</view>

			<!-- OCR 识别结果 -->
			<view class="result-card">
				<view class="card-header">
					<text class="card-title">识别结果</text>
					<u-button
						v-if="!isScanning"
						size="small"
						type="primary"
						@click="handleScan"
						:custom-style="{
							border: 'none'
						}"
					>
						重新识别
					</u-button>
				</view>

				<!-- 扫描中 -->
				<view v-if="isScanning" class="scanning">
					<u-loading-icon size="40"></u-loading-icon>
					<text class="scanning-text">正在识别中...</text>
				</view>

				<!-- 识别表单 -->
				<view v-else-if="ocrResult" class="scan-form">
					<u-form :model="formData" label-width="80">
						<u-form-item label="交易类型">
							<u-radio-group v-model="formData.type">
								<u-radio :name="'expense'">支出</u-radio>
								<u-radio :name="'income'">收入</u-radio>
							</u-radio-group>
						</u-form-item>

						<u-form-item label="金额">
							<u-input-number
								v-model="formData.amount"
								placeholder="请输入金额"
								:min="0.01"
								:precision="2"
							>
								<template #prefix>¥</template>
							</u-input-number>
						</u-form-item>

						<u-form-item label="分类">
							<u-select
								v-model="formData.categoryId"
								:options="categoryOptions"
								placeholder="请选择分类"
								@click="showCategorySelect = true"
							></u-select>
						</u-form-item>

						<u-form-item label="日期">
							<u-datetime-picker
								v-model="formData.date"
								mode="date"
								:show="showDateSelect"
								@confirm="confirmDate"
								@cancel="showDateSelect = false"
							></u-datetime-picker>
							<u-input
								:model-value="formatDate(formData.date)"
								placeholder="选择日期"
								@click="showDateSelect = true"
							></u-input>
						</u-form-item>

						<u-form-item label="备注">
							<u-input
								v-model="formData.note"
								type="textarea"
								placeholder="请输入备注"
								:maxlength="200"
								:rows="3"
							></u-input>
						</u-form-item>

						<!-- OCR 原始文本 -->
						<u-form-item label="识别文本">
							<u-input
								v-model="ocrResult.text"
								type="textarea"
								:rows="4"
								placeholder="识别的原始文本"
							></u-input>
						</u-form-item>
					</u-form>
				</view>

				<!-- 识别失败 -->
				<view v-else-if="scanError" class="error-state">
					<u-empty mode="data" text="识别失败，请重试"></u-empty>
					<u-button type="primary" @click="handleScan">重新识别</u-button>
				</view>

				<!-- 未识别 -->
				<view v-else class="empty-state">
					<u-empty mode="data" text='点击"重新识别"开始 OCR'></u-empty>
				</view>
			</view>
		</view>

		<!-- 提交按钮 -->
		<view class="submit-bar" v-if="ocrResult && !isScanning">
			<u-button
				type="error"
				size="large"
				@click="resetUpload"
				:custom-style="{
					flex: 1,
					marginRight: '10px',
					borderRadius: '25px'
				}"
			>
				取消
			</u-button>
			<u-button
				type="primary"
				size="large"
				:loading="submitting"
				@click="handleSubmit"
				:custom-style="{
					flex: 1,
					marginLeft: '10px',
					borderRadius: '25px'
				}"
			>
				确认记账
			</u-button>
		</view>

		<!-- 分类选择器 -->
		<u-popup v-model="showCategorySelect" mode="bottom" :round="20">
			<view class="category-picker">
				<view class="picker-header">
					<text class="picker-title">选择分类</text>
					<u-icon name="close" size="20" @click="showCategorySelect = false"></u-icon>
				</view>
				<view class="category-grid">
					<view
						v-for="category in currentCategories"
						:key="category.id"
						class="category-item"
						@click="selectCategory(category)"
					>
						<view
							class="category-icon"
							:style="{ backgroundColor: category.color }"
						>
							<u-icon :name="category.icon" size="24" color="#fff"></u-icon>
						</view>
						<text class="category-name">{{ category.name }}</text>
					</view>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
import { ref, reactive, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

export default {
	data() {
		return {
			hasImage: false,
			imageFile: null,
			imagePreview: '',
			isScanning: false,
			scanError: false,
			ocrResult: null,
			submitting: false,
			showCategorySelect: false,
			showDateSelect: false,
			categories: { expense: [], income: [] },
			formData: {
				type: 'expense',
				amount: null,
				categoryId: null,
				accountId: null,
				date: Date.now(),
				note: ''
			}
		};
	},
	computed: {
		categoryOptions() {
			const list = this.formData.type === 'income' ? this.categories.income : this.categories.expense;
			return (list || []).map(c => ({ label: c.name, value: c.id }));
		},
		currentCategories() {
			return this.formData.type === 'income' ? this.categories.income : this.categories.expense;
		}
	},
	onLoad() {
		this.loadCategories();
	},
	methods: {
		// 选择图片
		chooseImage() {
			uni.showActionSheet({
				itemList: ['拍照', '从相册选择'],
				success: (res) => {
					if (res.tapIndex === 0) {
						this.takePhoto();
					} else {
						this.chooseFromAlbum();
					}
				}
			});
		},
		
		// 拍照
		takePhoto() {
			uni.chooseImage({
				count: 1,
				sourceType: ['camera'],
				success: (res) => {
					this.handleImageSelected(res.tempFilePaths[0]);
				}
			});
		},
		
		// 从相册选择
		chooseFromAlbum() {
			uni.chooseImage({
				count: 1,
				sourceType: ['album'],
				success: (res) => {
					this.handleImageSelected(res.tempFilePaths[0]);
				}
			});
		},
		
		// 处理选择的图片
		handleImageSelected(path) {
			this.imageFile = path;
			this.hasImage = true;
			this.imagePreview = path;
			
			// 自动开始识别
			this.handleScan();
		},
		
		// OCR 识别
		handleScan() {
			if (!this.imageFile) return;
			
			this.isScanning = true;
			this.scanError = false;
			
			// 模拟 OCR 识别（实际项目调用 OCR API）
			setTimeout(() => {
				this.isScanning = false;
				
				// Mock 识别结果
				this.ocrResult = {
					text: '测试小票\n金额：158.00 元\n日期：2026-03-24\n商户：某某餐厅',
					amount: 158.00,
					date: Date.now(),
					merchant: '某某餐厅'
				};
				
				// 自动填充识别结果
				if (this.ocrResult.amount) {
					this.formData.amount = this.ocrResult.amount;
				}
				if (this.ocrResult.note) {
					this.formData.note = this.ocrResult.merchant;
				}
				
				uni.showToast({
					title: '识别成功',
					icon: 'success'
				});
			}, 1500);
		},
		
		// 选择分类
		selectCategory(category) {
			this.formData.categoryId = category.id;
			this.showCategorySelect = false;
		},
		
		// 确认日期
		confirmDate(e) {
			this.formData.date = e.value;
			this.showDateSelect = false;
		},
		
		// 格式化日期
		formatDate(timestamp) {
			if (!timestamp) return '';
			const date = new Date(timestamp);
			return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
		},
		
		// 加载分类
		loadCategories() {
			// Mock 分类数据
			this.categories.expense = [
				{ id: 1, name: '餐饮', color: '#EF4444', icon: 'fastfood' },
				{ id: 2, name: '交通', color: '#3B82F6', icon: 'car' },
				{ id: 3, name: '购物', color: '#F59E0B', icon: 'cart' },
				{ id: 4, name: '娱乐', color: '#8B5CF6', icon: 'game-controller' },
				{ id: 5, name: '医疗', color: '#10B981', icon: 'heart' },
				{ id: 6, name: '其他', color: '#6B7280', icon: 'grid' }
			];
			this.categories.income = [
				{ id: 7, name: '工资', color: '#10B981', icon: 'cash' },
				{ id: 8, name: '奖金', color: '#3B82F6', icon: 'gift' },
				{ id: 9, name: '其他', color: '#6B7280', icon: 'grid' }
			];
		},
		
		// 重置上传
		resetUpload() {
			this.hasImage = false;
			this.imageFile = null;
			this.imagePreview = '';
			this.ocrResult = null;
			this.scanError = false;
			this.formData.amount = null;
			this.formData.categoryId = null;
			this.formData.note = '';
			this.formData.date = Date.now();
		},
		
		// 提交记账
		handleSubmit() {
			if (!this.formData.amount) {
				uni.showToast({ title: '请输入金额', icon: 'none' });
				return;
			}
			if (!this.formData.categoryId) {
				uni.showToast({ title: '请选择分类', icon: 'none' });
				return;
			}
			
			this.submitting = true;
			
			// Mock 提交
			setTimeout(() => {
				this.submitting = false;
				uni.showToast({ title: '记账成功', icon: 'success' });
				
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
.ocr-page {
	min-height: 100vh;
	background: #F5F6F7;
	padding-bottom: 80px;
}

.upload-section {
	padding: 60px 20px;
}

.upload-area {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60px 20px;
	background: #fff;
	border-radius: 16px;
	border: 2px dashed #e5e7eb;
}

.upload-icon {
	width: 100px;
	height: 100px;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f5f3ff;
	border-radius: 50%;
	margin-bottom: 20px;
}

.upload-title {
	font-size: 16px;
	font-weight: 600;
	color: #333;
	margin-bottom: 8px;
}

.upload-desc {
	font-size: 13px;
	color: #6b7280;
}

.content-section {
	padding: 16px;
}

.image-preview-card,
.result-card {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
	margin-bottom: 16px;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;
}

.card-title {
	font-size: 16px;
	font-weight: 600;
	color: #1f2937;
}

.image-preview {
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f9fafb;
	border-radius: 8px;
	overflow: hidden;
}

.scanning {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40px 0;
}

.scanning-text {
	margin-top: 16px;
	font-size: 14px;
	color: #6b7280;
}

.scan-form {
	padding: 10px 0;
}

.error-state,
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20px 0;
}

.submit-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	padding: 12px 16px;
	background: #fff;
	box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
	z-index: 100;
}

.category-picker {
	padding: 20px;
}

.picker-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.picker-title {
	font-size: 16px;
	font-weight: 600;
	color: #1f2937;
}

.category-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
}

.category-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8px;
}

.category-icon {
	width: 56px;
	height: 56px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 14px;
}

.category-name {
	font-size: 12px;
	color: #6b7280;
}
</style>
