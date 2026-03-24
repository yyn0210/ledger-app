<template>
	<view class="voice-page">
		<!-- 录音区域 -->
		<view class="record-section">
			<!-- 未录音状态 -->
			<view class="record-idle" v-if="!isRecording && !transcript">
				<view class="record-icon-large">
					<u-icon name="mic" size="80" color="#ec4899"></u-icon>
				</view>
				<text class="record-title">点击开始语音记账</text>
				<text class="record-desc">说出消费金额和分类，例如："今天打车花了 35 元"</text>
				<u-button
					type="primary"
					size="large"
					@click="startRecording"
					:custom-style="{
						marginTop: '30px',
						width: '200px',
						height: '55px',
						borderRadius: '28px'
					}"
				>
					<template #icon>
						<u-icon name="mic" size="20"></u-icon>
					</template>
					开始录音
				</u-button>
			</view>

			<!-- 录音中状态 -->
			<view class="record-active" v-else-if="isRecording">
				<view class="record-animation">
					<view class="pulse-ring"></view>
					<view class="pulse-ring delay-1"></view>
					<view class="pulse-ring delay-2"></view>
					<u-icon name="mic" size="60" color="#fff"></u-icon>
				</view>
				<text class="recording-title">正在录音中...</text>
				<text class="recording-desc">请说出您的消费内容</text>
				<u-button
					type="error"
					size="large"
					@click="stopRecording"
					:custom-style="{
						marginTop: '30px',
						width: '180px',
						height: '50px',
						borderRadius: '25px'
					}"
				>
					<template #icon>
						<u-icon name="close" size="18"></u-icon>
					</template>
					停止录音
				</u-button>
			</view>

			<!-- 处理中状态 -->
			<view class="record-processing" v-else-if="isProcessing">
				<u-loading-icon size="40"></u-loading-icon>
				<text class="processing-text">正在识别语音...</text>
			</view>

			<!-- 识别结果 -->
			<view class="record-result" v-else-if="transcript">
				<view class="result-header">
					<text class="result-title">识别结果</text>
					<u-button
						size="small"
						@click="resetRecording"
						:custom-style="{
							background: '#f3f4f6',
							border: 'none'
						}"
					>
						重新录音
					</u-button>
				</view>

				<!-- 语音文本 -->
				<u-alert
					title="您说的话"
					type="info"
					:closeable="false"
					:show-icon="true"
				>
					<text class="transcript-text">{{ transcript }}</text>
				</u-alert>

				<!-- 解析结果 -->
				<view class="parsed-section" v-if="transcript">
					<text class="parsed-title">语义解析</text>
					
					<view class="form-section">
						<view class="form-row">
							<text class="form-label">交易类型</text>
							<view class="form-value">
								<u-radio-group v-model="formData.type">
									<u-radio :name="'expense'">支出</u-radio>
									<u-radio :name="'income'">收入</u-radio>
								</u-radio-group>
							</view>
						</view>
						
						<view class="form-row">
							<text class="form-label">金额</text>
							<view class="form-value">
								<u-input
									v-model="formData.amount"
									type="digit"
									placeholder="请输入金额"
									:border="true"
								>
									<template #prefix>
										<text>¥</text>
									</template>
								</u-input>
							</view>
						</view>
						
						<view class="form-row">
							<text class="form-label">分类</text>
							<view class="form-value">
								<u-select
									v-model="formData.categoryId"
									:options="categoryOptions"
									placeholder="请选择分类"
								></u-select>
							</view>
						</view>
						
						<view class="form-row">
							<text class="form-label">日期</text>
							<view class="form-value">
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
									:border="true"
									placeholder="选择日期"
								></u-input>
							</view>
						</view>
						
						<view class="form-row">
							<text class="form-label">备注</text>
							<view class="form-value">
								<u-input
									v-model="formData.note"
									type="textarea"
									placeholder="请输入备注"
									:maxlength="200"
									:border="true"
								></u-input>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 提交按钮 -->
		<view class="submit-bar" v-if="transcript && !isProcessing">
			<u-button
				type="primary"
				size="large"
				@click="handleSubmit"
				:loading="submitting"
				:custom-style="{
					borderRadius: '25px'
				}"
			>
				<template #icon>
					<u-icon name="checkmark-circle" size="18"></u-icon>
				</template>
				确认记账
			</u-button>
		</view>

		<!-- 使用示例 -->
		<view class="examples-section">
			<text class="examples-title">语音记账示例</text>
			<view class="examples-list">
				<view class="example-item">
					<u-tag text="支出" type="info" size="mini"></u-tag>
					<text class="example-text">"今天中午吃饭花了 58 元"</text>
				</view>
				<view class="example-item">
					<u-tag text="支出" type="info" size="mini"></u-tag>
					<text class="example-text">"打车回家 35 块钱"</text>
				</view>
				<view class="example-item">
					<u-tag text="收入" type="success" size="mini"></u-tag>
					<text class="example-text">"收到工资 15000 元"</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getCategoryList } from '@/api/category';

export default {
	data() {
		return {
			isRecording: false,
			isProcessing: false,
			transcript: '',
			submitting: false,
			showDate: false,
			currentDate: Date.now(),
			formData: {
				type: 'expense',
				amount: '',
				categoryId: null,
				note: '',
				date: Date.now()
			},
			categories: {
				expense: [],
				income: []
			}
		};
	},
	computed: {
		categoryOptions() {
			const list = this.formData.type === 'income' 
				? this.categories.income 
				: this.categories.expense;
			return list.map(c => ({ label: c.name, value: c.id }));
		},
		formattedDate() {
			const date = new Date(this.formData.date);
			return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
		}
	},
	onLoad() {
		this.loadCategories();
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
		
		// 开始录音
		startRecording() {
			this.isRecording = true;
			this.transcript = '';
			this.formData = {
				type: 'expense',
				amount: '',
				categoryId: null,
				note: '',
				date: Date.now()
			};
			
			// 使用 uni-app 语音识别插件（模拟）
			// 实际项目中可使用 uni.getRecorderManager() 或第三方语音识别 SDK
			setTimeout(() => {
				this.stopRecording();
			}, 3000);
		},
		
		// 停止录音
		stopRecording() {
			this.isRecording = false;
			this.isProcessing = true;
			
			// 模拟语音识别
			setTimeout(() => {
				const mockText = '今天打车花了 35 元';
				this.transcript = mockText;
				this.parseVoiceText(mockText);
				this.isProcessing = false;
				
				uni.showToast({
					title: '识别完成',
					icon: 'success'
				});
			}, 1500);
		},
		
		// 解析语音文本
		parseVoiceText(text) {
			// 简单的正则提取
			const amountMatch = text.match(/[\d,]+\.?\d*/)?.[0];
			const amount = amountMatch ? amountMatch.replace(/,/g, '') : '';
			
			// 判断类型
			const isIncome = text.includes('收入') || text.includes('工资') || text.includes('收到');
			
			// 自动填充
			this.formData.amount = amount;
			this.formData.type = isIncome ? 'income' : 'expense';
			this.formData.note = text;
			
			// 尝试匹配分类
			if (text.includes('吃饭') || text.includes('餐')) {
				this.formData.categoryId = 1; // 餐饮
			} else if (text.includes('打车') || text.includes('交通')) {
				this.formData.categoryId = 2; // 交通
			} else if (text.includes('购物') || text.includes('超市')) {
				this.formData.categoryId = 3; // 购物
			}
		},
		
		// 重置录音
		resetRecording() {
			this.isRecording = false;
			this.isProcessing = false;
			this.transcript = '';
			this.formData = {
				type: 'expense',
				amount: '',
				categoryId: null,
				note: '',
				date: Date.now()
			};
		},
		
		// 日期选择确认
		onDateConfirm(e) {
			this.formData.date = e.value;
			this.showDate = false;
		},
		
		// 提交记账
		handleSubmit() {
			if (!this.formData.amount) {
				uni.showToast({
					title: '请输入金额',
					icon: 'none'
				});
				return;
			}
			
			if (!this.formData.categoryId) {
				uni.showToast({
					title: '请选择分类',
					icon: 'none'
				});
				return;
			}
			
			this.submitting = true;
			
			// Mock 提交
			setTimeout(() => {
				this.submitting = false;
				uni.showToast({
					title: '记账成功！',
					icon: 'success',
					duration: 2000
				});
				
				// 重置
				setTimeout(() => {
					this.resetRecording();
				}, 1500);
			}, 1000);
		}
	}
};
</script>

<style lang="scss" scoped>
.voice-page {
	min-height: 100vh;
	background: #F5F6F7;
	padding: 20px;
	padding-bottom: 40px;
}

.record-section {
	background: #fff;
	border-radius: 16px;
	padding: 40px 20px;
	margin-bottom: 16px;
}

.record-idle,
.record-active,
.record-processing,
.record-result {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.record-icon-large {
	width: 120px;
	height: 120px;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
	border-radius: 50%;
	margin-bottom: 20px;
}

.record-title {
	font-size: 18px;
	font-weight: 600;
	color: #333;
	margin-bottom: 8px;
	text-align: center;
}

.record-desc {
	font-size: 14px;
	color: #6b7280;
	text-align: center;
}

.record-active {
	padding: 20px 0;
}

.record-animation {
	position: relative;
	width: 120px;
	height: 120px;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 20px;
}

.pulse-ring {
	position: absolute;
	width: 100%;
	height: 100%;
	border-radius: 50%;
	background: rgba(236, 72, 153, 0.3);
	animation: pulse 1.5s ease-out infinite;
}

.pulse-ring.delay-1 {
	animation-delay: 0.5s;
}

.pulse-ring.delay-2 {
	animation-delay: 1s;
}

@keyframes pulse {
	0% {
		transform: scale(0.5);
		opacity: 1;
	}
	100% {
		transform: scale(1.5);
		opacity: 0;
	}
}

.recording-title {
	font-size: 18px;
	font-weight: 600;
	color: #333;
	margin-bottom: 8px;
}

.recording-desc {
	font-size: 14px;
	color: #6b7280;
}

.record-processing {
	padding: 40px 0;
}

.processing-text {
	margin-top: 16px;
	font-size: 14px;
	color: #6b7280;
}

.result-header {
	width: 100%;
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;
}

.result-title {
	font-size: 16px;
	font-weight: 600;
	color: #333;
}

.transcript-text {
	font-size: 15px;
	line-height: 1.6;
	color: #1f2937;
}

.parsed-section {
	width: 100%;
	margin-top: 20px;
}

.parsed-title {
	font-size: 16px;
	font-weight: 600;
	color: #333;
	margin-bottom: 16px;
	display: block;
}

.form-section {
	width: 100%;
}

.form-row {
	margin-bottom: 16px;
	
	.form-label {
		font-size: 14px;
		color: #6b7280;
		margin-bottom: 8px;
		display: block;
	}
	
	.form-value {
		width: 100%;
	}
}

.submit-bar {
	background: #fff;
	border-radius: 12px;
	padding: 16px 20px;
	margin-bottom: 16px;
	display: flex;
	justify-content: center;
}

.examples-section {
	background: #fff;
	border-radius: 12px;
	padding: 16px 20px;
}

.examples-title {
	font-size: 15px;
	font-weight: 600;
	color: #333;
	margin-bottom: 12px;
	display: block;
}

.examples-list {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.example-item {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 10px;
	background: #f9fafb;
	border-radius: 8px;
}

.example-text {
	font-size: 13px;
	color: #1f2937;
	font-style: italic;
}
</style>
