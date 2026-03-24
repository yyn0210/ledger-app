<template>
	<view class="login-page">
		<!-- Logo 区域 -->
		<view class="logo-section">
			<view class="logo-icon">📒</view>
			<text class="app-name">简洛账本</text>
			<text class="app-slogan">精简生活，逻辑理财</text>
		</view>

		<!-- 登录表单 -->
		<view class="form-section">
			<u-form :model="formData" label-width="0">
				<u-form-item>
					<u-input
						v-model="formData.phone"
						type="number"
						maxlength="11"
						placeholder="请输入手机号"
						:clearable="true"
					>
						<template #prefix>
							<u-icon name="phone" size="20"></u-icon>
						</template>
					</u-input>
				</u-form-item>

				<u-form-item>
					<u-input
						v-model="formData.code"
						type="number"
						maxlength="6"
						placeholder="请输入验证码"
						:clearable="true"
					>
						<template #prefix>
							<u-icon name="shield" size="20"></u-icon>
						</template>
						<template #suffix>
							<text
								class="code-btn"
								:class="{ disabled: countdown > 0 }"
								@click="sendCode"
							>
								{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
							</text>
						</template>
					</u-input>
				</u-form-item>

				<u-form-item>
					<u-checkbox-group v-model="agreed">
						<u-checkbox :checked="agreed" @change="agreed = $event">
							<text class="agree-text">
								我已阅读并同意
								<text class="link" @click.stop="openAgreement('user')">《用户协议》</text>
								和
								<text class="link" @click.stop="openAgreement('privacy')">《隐私政策》</text>
							</text>
						</u-checkbox>
					</u-checkbox-group>
				</u-form-item>

				<u-button
					type="primary"
					size="large"
					:disabled="!canSubmit"
					@click="handleLogin"
					:custom-style="{
						height: '50px',
						borderRadius: '25px',
						fontSize: '16px',
						fontWeight: '600',
						marginTop: '20px'
					}"
				>
					登录/注册
				</u-button>
			</u-form>
		</view>

		<!-- 其他登录方式 -->
		<view class="other-login">
			<text class="other-label">其他登录方式</text>
			<view class="other-icons">
				<view class="other-icon" @click="wechatLogin">
					<u-icon name="wechat-fill" size="32" color="#07c160"></u-icon>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { ref, computed } from 'vue';

export default {
	data() {
		return {
			formData: {
				phone: '',
				code: ''
			},
			agreed: false,
			countdown: 0,
			timer: null
		};
	},
	computed: {
		canSubmit() {
			return (
				this.formData.phone.length === 11 &&
				this.formData.code.length === 6 &&
				this.agreed
			);
		}
	},
	onUnload() {
		if (this.timer) clearInterval(this.timer);
	},
	methods: {
		sendCode() {
			if (this.countdown > 0 || this.formData.phone.length !== 11) return;
			
			// 模拟发送验证码
			uni.showToast({ title: '验证码已发送', icon: 'success' });
			this.countdown = 60;
			
			this.timer = setInterval(() => {
				this.countdown--;
				if (this.countdown <= 0) {
					clearInterval(this.timer);
					this.timer = null;
				}
			}, 1000);
		},
		handleLogin() {
			if (!this.agreed) {
				uni.showToast({ title: '请先同意用户协议', icon: 'none' });
				return;
			}
			
			// 模拟登录
			uni.showLoading({ title: '登录中...' });
			
			setTimeout(() => {
				uni.hideLoading();
				// 保存登录状态
				uni.setStorageSync('token', 'mock-token-' + Date.now());
				uni.setStorageSync('userInfo', {
					phone: this.formData.phone,
					nickname: '用户' + this.formData.phone.slice(-4)
				});
				
				uni.showToast({ title: '登录成功', icon: 'success' });
				
				// 跳转到首页
				setTimeout(() => {
					uni.switchTab({ url: '/pages/index/index' });
				}, 1500);
			}, 1000);
		},
		wechatLogin() {
			uni.showToast({ title: '微信登录开发中', icon: 'none' });
		},
		openAgreement(type) {
			uni.showToast({ title: '协议页面开发中', icon: 'none' });
		}
	}
};
</script>

<style lang="scss" scoped>
.login-page {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 60px 30px;
}

.logo-section {
	text-align: center;
	margin-bottom: 60px;

	.logo-icon {
		font-size: 80px;
		margin-bottom: 16px;
	}

	.app-name {
		display: block;
		font-size: 28px;
		font-weight: 600;
		color: #fff;
		margin-bottom: 8px;
	}

	.app-slogan {
		display: block;
		font-size: 14px;
		color: rgba(255, 255, 255, 0.8);
	}
}

.form-section {
	background: #fff;
	border-radius: 20px;
	padding: 30px 24px;
}

.code-btn {
	font-size: 14px;
	color: #4F46E5;
	padding: 4px 8px;
	cursor: pointer;

	&.disabled {
		color: #999;
	}
}

.agree-text {
	font-size: 13px;
	color: #666;

	.link {
		color: #4F46E5;
		text-decoration: underline;
	}
}

.other-login {
	margin-top: 60px;
	text-align: center;

	.other-label {
		font-size: 14px;
		color: rgba(255, 255, 255, 0.8);
		margin-bottom: 20px;
		display: block;
	}

	.other-icons {
		display: flex;
		justify-content: center;
		gap: 30px;
	}

	.other-icon {
		width: 56px;
		height: 56px;
		background: rgba(255, 255, 255, 0.2);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}
}
</style>
