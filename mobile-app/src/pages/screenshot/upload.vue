<template>
	<view class="screenshot-page">
		<!-- 上传区域 -->
		<view class="upload-section" v-if="images.length === 0">
			<view class="upload-area" @click="chooseImages">
				<view class="upload-icon">
					<u-icon name="images" size="60" color="#06b6d4"></u-icon>
				</view>
				<text class="upload-title">上传账单截图</text>
				<text class="upload-desc">支持微信/支付宝账单截图，最多 10 张</text>
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

		<!-- 图片列表和识别结果 -->
		<view class="content-section" v-else>
			<!-- 图片列表 -->
			<view class="images-card">
				<view class="card-header">
					<text class="card-title">已上传截图（{{ images.length }}张）</text>
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
				<view class="image-grid">
					<view
						v-for="(img, index) in images"
						:key="index"
						class="image-item"
						:class="{ active: currentIndex === index }"
						@click="selectImage(index)"
					>
						<u-image
							:src="img.preview"
							mode="aspectFill"
							width="100"
							height="100"
							:show-loading="true"
						></u-image>
						<view class="image-status">
							<u-tag
								:text="img.scanned ? '已识别' : '待识别'"
								:type="img.scanned ? 'success' : 'default'"
								size="mini"
								plain
							></u-tag>
						</view>
					</view>
				</view>
				<view class="scan-button">
					<u-button
						v-if="!allScanned"
						type="primary"
						size="large"
						@click="scanAllImages"
						:loading="isScanning"
						:custom-style="{
							borderRadius: '25px'
						}"
					>
						<template #icon>
							<u-icon name="scan" size="18"></u-icon>
						</template>
						批量识别
					</u-button>
				</view>
			</view>

			<!-- 识别结果 -->
			<view class="result-card" v-if="currentImage && currentImage.scanned">
				<view class="card-header">
					<text class="card-title">识别结果（{{ currentIndex + 1 }}/{{ images.length }}）</text>
					<text class="card-desc">已识别 {{ extractedData.length }} 条交易</text>
				</view>

				<!-- 交易列表 -->
				<view class="transaction-list">
					<view
						v-for="(item, idx) in extractedData"
						:key="idx"
						class="transaction-item"
					>
						<view class="transaction-header">
							<text class="transaction-date">{{ item.date }}</text>
							<u-tag
								:text="item.type === 'income' ? '收入' : '支出'"
								:type="item.type === 'income' ? 'success' : 'error'"
								size="mini"
							></u-tag>
						</view>
						<view class="transaction-body">
							<text class="transaction-note">{{ item.note || '无备注' }}</text>
							<text
								class="transaction-amount"
								:class="item.type"
							>
								{{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
							</text>
						</view>
						<view class="transaction-actions">
							<u-button
								size="small"
								type="primary"
								@click="confirmTransaction(item)"
								:custom-style="{
									borderRadius: '15px'
								}"
							>
								确认导入
							</u-button>
						</view>
					</view>
				</view>

				<!-- 批量操作 -->
				<view class="batch-actions" v-if="extractedData.length > 0">
					<u-button
						type="primary"
						size="large"
						@click="confirmAllTransactions"
						:loading="submitting"
						:custom-style="{
							borderRadius: '25px'
						}"
					>
						<template #icon>
							<u-icon name="checkmark-circle" size="18"></u-icon>
						</template>
						全部导入（{{ extractedData.length }}条）
					</u-button>
				</view>
			</view>

			<!-- 未识别提示 -->
			<view class="empty-card" v-else-if="images.length > 0 && !isScanning">
				<u-empty
					mode="data"
					text='点击"批量识别"开始 OCR 识别'
				></u-empty>
			</view>

			<!-- 扫描中 -->
			<view class="scanning-card" v-if="isScanning">
				<u-loading-icon size="40"></u-loading-icon>
				<text class="scanning-text">正在批量识别中...</text>
			</view>
		</view>
	</view>
</template>

<script>
import { ref, reactive, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

export default {
	data() {
		return {
			images: [],
			currentIndex: 0,
			isScanning: false,
			submitting: false,
			extractedData: []
		};
	},
	computed: {
		currentImage() {
			return this.images[this.currentIndex];
		},
		allScanned() {
			return this.images.every(img => img.scanned);
		}
	},
	onLoad() {
		// 初始化
	},
	methods: {
		// 选择图片
		chooseImages() {
			uni.chooseImage({
				count: 10,
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.images = res.tempFilePaths.map(path => ({
						path,
						preview: path,
						scanned: false,
						result: []
					}));
					
					// 自动开始识别
					this.scanAllImages();
				}
			});
		},
		
		// 选择图片
		selectImage(index) {
			this.currentIndex = index;
			if (this.images[index].scanned) {
				this.extractedData = this.images[index].result || [];
			}
		},
		
		// 批量识别
		scanAllImages() {
			if (this.images.length === 0) return;
			
			this.isScanning = true;
			
			// 模拟批量 OCR 识别
			let completed = 0;
			
			const scanNext = () => {
				if (completed >= this.images.length) {
					this.isScanning = false;
					const total = this.images.reduce((sum, img) => sum + (img.result?.length || 0), 0);
					uni.showToast({
						title: `识别完成！共 ${total} 条交易`,
						icon: 'success'
					});
					return;
				}
				
				// 模拟识别延迟
				setTimeout(() => {
					const mockResult = this.generateMockTransactions();
					this.images[completed].scanned = true;
					this.images[completed].result = mockResult;
					
					if (completed === this.currentIndex) {
						this.extractedData = mockResult;
					}
					
					completed++;
					scanNext();
				}, 500);
			};
			
			scanNext();
		},
		
		// 生成 Mock 交易数据
		generateMockTransactions() {
			const count = Math.floor(Math.random() * 5) + 3; // 3-7 条
			const transactions = [];
			
			for (let i = 0; i < count; i++) {
				transactions.push({
					id: Date.now() + i,
					type: Math.random() > 0.8 ? 'income' : 'expense',
					amount: (Math.random() * 500 + 10).toFixed(2),
					date: `2026-03-${String(Math.floor(Math.random() * 24) + 1).padStart(2, '0')}`,
					note: ['餐饮', '交通', '购物', '娱乐', '工资', '其他'][Math.floor(Math.random() * 6)],
					categoryId: Math.floor(Math.random() * 6) + 1
				});
			}
			
			return transactions;
		},
		
		// 确认单条交易
		confirmTransaction(item) {
			this.submitting = true;
			
			// Mock 提交
			setTimeout(() => {
				this.submitting = false;
				uni.showToast({
					title: '导入成功！',
					icon: 'success'
				});
				
				// 移除已导入的交易
				this.extractedData = this.extractedData.filter(t => t.id !== item.id);
			}, 500);
		},
		
		// 批量导入
		confirmAllTransactions() {
			if (this.extractedData.length === 0) {
				uni.showToast({
					title: '没有可导入的交易',
					icon: 'none'
				});
				return;
			}
			
			this.submitting = true;
			
			// Mock 批量提交
			setTimeout(() => {
				this.submitting = false;
				uni.showToast({
					title: `成功导入 ${this.extractedData.length} 条交易！`,
					icon: 'success',
					duration: 2000
				});
				
				// 重置
				setTimeout(() => {
					this.resetUpload();
				}, 1500);
			}, 1000);
		},
		
		// 重置上传
		resetUpload() {
			this.images = [];
			this.currentIndex = 0;
			this.extractedData = [];
			this.isScanning = false;
		}
	}
};
</script>

<style lang="scss" scoped>
.screenshot-page {
	min-height: 100vh;
	background: #F5F6F7;
	padding-bottom: 20px;
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
	background: #f0f9ff;
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

.images-card,
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
	
	.card-title {
		font-size: 16px;
		font-weight: 600;
		color: #1f2937;
	}
	
	.card-desc {
		font-size: 13px;
		color: #6b7280;
	}
}

.image-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 12px;
	margin-bottom: 16px;
}

.image-item {
	position: relative;
	border-radius: 8px;
	overflow: hidden;
	cursor: pointer;
	border: 2px solid transparent;
	transition: all 0.3s;
	
	&.active {
		border-color: #06b6d4;
	}
}

.image-status {
	position: absolute;
	top: 4px;
	right: 4px;
}

.scan-button {
	display: flex;
	justify-content: center;
}

.transaction-list {
	display: flex;
	flex-direction: column;
	gap: 12px;
	max-height: 300px;
	overflow-y: auto;
}

.transaction-item {
	padding: 12px;
	background: #f9fafb;
	border-radius: 8px;
}

.transaction-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8px;
}

.transaction-date {
	font-size: 13px;
	color: #6b7280;
}

.transaction-body {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8px;
}

.transaction-note {
	font-size: 14px;
	color: #1f2937;
}

.transaction-amount {
	font-size: 16px;
	font-weight: 600;
	
	&.income {
		color: #10b981;
	}
	
	&.expense {
		color: #ef4444;
	}
}

.transaction-actions {
	display: flex;
	justify-content: flex-end;
}

.batch-actions {
	margin-top: 20px;
	padding-top: 20px;
	border-top: 1px solid #e5e7eb;
	display: flex;
	justify-content: center;
}

.empty-card,
.scanning-card {
	background: #fff;
	border-radius: 12px;
	padding: 40px 20px;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.scanning-text {
	margin-top: 16px;
	font-size: 14px;
	color: #6b7280;
}
</style>
