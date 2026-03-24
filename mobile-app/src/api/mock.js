/**
 * Mock API 工具
 * 用于前端开发阶段模拟后端 API 响应
 */

// Mock 数据生成工具
export function mockDelay(ms = 300) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

// Mock 账本数据
export async function getBooks() {
	await mockDelay();
	return {
		code: 200,
		data: [
			{ id: 1, name: '日常账本', description: '记录日常生活开支', color: '#4F46E5', icon: 'book', isCurrent: true },
			{ id: 2, name: '旅行账本', description: '旅行费用专用', color: '#10B981', icon: 'map', isCurrent: false },
			{ id: 3, name: '装修账本', description: '新房装修预算', color: '#F59E0B', icon: 'home', isCurrent: false }
		]
	};
}

// Mock 交易数据
export async function getTransactions(params = {}) {
	await mockDelay();
	return {
		code: 200,
		data: {
			list: [
				{ id: 1, name: '午餐', category: '餐饮', amount: 35, type: 'expense', date: '2026-03-24', time: '12:30', categoryColor: '#EF4444', categoryIcon: 'shopping-cart' },
				{ id: 2, name: '工资', category: '工作', amount: 15000, type: 'income', date: '2026-03-23', time: '09:00', categoryColor: '#10B981', categoryIcon: 'money' },
				{ id: 3, name: '地铁', category: '交通', amount: 6, type: 'expense', date: '2026-03-23', time: '08:30', categoryColor: '#3B82F6', categoryIcon: 'bus' },
				{ id: 4, name: '咖啡', category: '餐饮', amount: 28, type: 'expense', date: '2026-03-22', time: '14:00', categoryColor: '#F59E0B', categoryIcon: 'cup' },
				{ id: 5, name: '超市购物', category: '购物', amount: 256, type: 'expense', date: '2026-03-21', time: '18:00', categoryColor: '#8B5CF6', categoryIcon: 'bag' }
			],
			total: 5,
			page: 1,
			pageSize: 20
		}
	};
}

// Mock 统计数据
export async function getStatistics(period = 'month') {
	await mockDelay();
	return {
		code: 200,
		data: {
			period,
			income: 15000,
			expense: 8500,
			balance: 6500,
			categoryStats: [
				{ category: '餐饮', amount: 2500, percentage: 29.4 },
				{ category: '交通', amount: 500, percentage: 5.9 },
				{ category: '购物', amount: 3500, percentage: 41.2 },
				{ category: '娱乐', amount: 1000, percentage: 11.8 },
				{ category: '其他', amount: 1000, percentage: 11.7 }
			]
		}
	};
}

// Mock 用户数据
export async function getUserInfo() {
	await mockDelay();
	return {
		code: 200,
		data: {
			id: 1,
			username: '简洛用户',
			avatar: '',
			description: '精简生活，逻辑理财',
			totalDays: 30,
			totalTransactions: 156,
			totalAmount: 23500
		}
	};
}

// Mock 添加交易
export async function addTransaction(data) {
	await mockDelay();
	return {
		code: 200,
		data: {
			id: Date.now(),
			...data,
			createdAt: new Date().toISOString()
		},
		message: '添加成功'
	};
}
