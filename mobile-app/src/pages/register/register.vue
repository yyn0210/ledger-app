<template>
	<view class="register-page">
		<!-- 头部 -->
		<view class="header">
			<u-icon name="arrow-left" size="24" color="#fff" @click="goBack"></u-icon>
			<text class="title">注册账号</text>
			<view style="width: 24px"></view>
		</view>

		<!-- 表单区域 -->
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
					<u-input
						v-model="formData.password"
						type="password"
						placeholder="请设置登录密码（6-16 位）"
						:clearable="true"
					>
						<template #prefix>
							<u-icon name="lock" size="20"></u-icon>
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
					@click="handleRegister"
					:custom-style="{
						height: '50px',
						borderRadius: '25px',
						fontSize: '16px',
						fontWeight: '600',
						marginTop: '20px'
					}"
				>
					注册
				</u-button>
			</u-form>

			<!-- 已有账号 -->
			<view class="login-link">
				<text class="link-text">已有账号？</text>
				<text class="link" @click="goToLogin">立即登录</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			formData: {
				phone: '',
				code: '',
				password: ''
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
				this.formData.password.length >= 6 &&
				this.agreed
			);
		}
	},
	onUnload() {
		if (this.timer) clearInterval(this.timer);
	},
	methods: {
		goBack() {
			uni.navigateBack();
		},
		sendCode() {
			if (this.countdown > 0 || this.formData.phone.length !== 11) return;
			
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
		handleRegister() {
			if (!this.agreed) {
				uni.showToast({ title: '请先同意用户协议', icon: 'none' });
				return;
			}
			
			uni.showLoading({ title: '注册中...' });
			
			setTimeout(() => {
				uni.hideLoading();
				uni.setStorageSync('token', 'mock-token-' + Date.now());
				uni.setStorageSync('userInfo', {
					phone: this.formData.phone,
					nickname: '用户' + this.formData.phone.slice(-4)
				});
				
				uni.showToast({ title: '注册成功', icon: 'success' });
				
				setTimeout(() => {
					uni.switchTab({ url: '/pages/index/index' });
				}, 1500);
			}, 1000);
		},
		goToLogin() {
			uni.navigateBack();
		},
		openAgreement(type) {
			uni.showToast({ title: '协议页面开发中', icon: 'none' });
		}
	}
};
</script>

<style lang="scss" scoped>
.register-page {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40px 20px 20px;

	.title {
		font-size: 20px;
		font-weight: 600;
		color: #fff;
	}
}

.form-section {
	background: #fff;
	border-radius: 20px 20px 0 0;
	padding: 30px 24px;
	min-height: calc(100vh - 100px);
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

.login-link {
	margin-top: 30px;
	text-align: center;

	.link-text {
		font-size: 14px;
		color: #999;
		margin-right: 8px;
	}

	.link {
		font-size: 14px;
		color: #4F46E5;
	}
}
</style>
