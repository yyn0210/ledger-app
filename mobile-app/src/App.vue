<script>
export default {
	onLaunch: function() {
		console.log('App Launch - 简洛账本');
	},
	onShow: function() {
		console.log('App Show');
	},
	onHide: function() {
		console.log('App Hide');
	}
};
</script>

<style>
/* 全局公共样式 */

page {
	background-color: #F5F6F7;
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
	font-size: 14px;
	line-height: 1.5;
	color: #1F2937;
}

/* 公共类 */
.container {
	padding: 16px;
	min-height: 100vh;
	box-sizing: border-box;
}

.card {
	background: #fff;
	border-radius: 12px;
	padding: 16px;
	margin-bottom: 16px;
}

/* 主题色 */
.text-primary {
	color: #4F46E5;
}

.text-success {
	color: #10B981;
}

.text-error {
	color: #EF4444;
}

.text-warning {
	color: #F59E0B;
}

.text-info {
	color: #3B82F6;
}

/* 收入支出颜色 */
.income {
	color: #10B981;
}

.expense {
	color: #EF4444;
}
</style>
