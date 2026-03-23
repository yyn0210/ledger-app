/**
 * Mock 数据服务
 * 用于前端开发和测试，模拟后端 API 响应
 */

// 模拟延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// Mock 数据
const mockData = {
  // 用户信息
  user: {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    email: 'test@example.com',
    phone: '13800138000',
    avatar: ''
  },

  // 账本列表
  books: [
    { id: 1, name: '日常账本', description: '记录日常收支', color: '#3385ff', icon: 'Wallet', memberCount: 1, recordCount: 156, isDefault: true },
    { id: 2, name: '旅行账本', description: '记录旅行花费', color: '#52c41a', icon: 'Plane', memberCount: 1, recordCount: 28, isDefault: false },
    { id: 3, name: '装修账本', description: '记录装修支出', color: '#ff9900', icon: 'Home', memberCount: 1, recordCount: 45, isDefault: false }
  ],

  // 分类列表
  categories: {
    expense: [
      { id: 1, name: '餐饮', type: 'expense', color: '#ff9900', icon: 'FastFood', sort: 1, isDefault: true },
      { id: 2, name: '交通', type: 'expense', color: '#3385ff', icon: 'Car', sort: 2, isDefault: true },
      { id: 3, name: '购物', type: 'expense', color: '#ff6b6b', icon: 'Cart', sort: 3, isDefault: true },
      { id: 4, name: '娱乐', type: 'expense', color: '#9b59b6', icon: 'Beer', sort: 4, isDefault: true },
      { id: 5, name: '居住', type: 'expense', color: '#2ecc71', icon: 'Home', sort: 5, isDefault: true }
    ],
    income: [
      { id: 11, name: '工资', type: 'income', color: '#2ecc71', icon: 'Cash', sort: 1, isDefault: true },
      { id: 12, name: '奖金', type: 'income', color: '#f39c12', icon: 'Gift', sort: 2, isDefault: true },
      { id: 13, name: '投资', type: 'income', color: '#3498db', icon: 'TrendingUp', sort: 3, isDefault: true }
    ]
  },

  // 账户列表
  accounts: [
    { id: 1, name: '钱包', type: 'cash', typeName: '现金', color: '#52c41a', icon: 'Cash', balance: 1500.00, note: '' },
    { id: 2, name: '招商银行储蓄卡', type: 'bank', typeName: '银行卡', color: '#3385ff', icon: 'Card', balance: 25680.50, note: '尾号 8888' },
    { id: 3, name: '支付宝', type: 'alipay', typeName: '支付宝', color: '#1677ff', icon: 'LogoApple', balance: 8920.30, note: '' },
    { id: 4, name: '微信零钱', type: 'wechat', typeName: '微信', color: '#07c160', icon: 'LogoApple', balance: 2350.80, note: '' },
    { id: 5, name: '信用卡', type: 'credit', typeName: '信用卡', color: '#ff6b6b', icon: 'Card', balance: -3200.00, note: '账单日每月 15 号' }
  ],

  // 交易记录
  transactions: [
    { id: 1, type: 'expense', amount: 68.00, categoryId: 1, categoryName: '餐饮', bookId: 1, bookName: '日常账本', accountId: 3, accountName: '支付宝', date: '2026-03-23', note: '午餐' },
    { id: 2, type: 'expense', amount: 5.00, categoryId: 2, categoryName: '交通', bookId: 1, bookName: '日常账本', accountId: 1, accountName: '钱包', date: '2026-03-23', note: '地铁' },
    { id: 3, type: 'income', amount: 8500.00, categoryId: 11, categoryName: '工资', bookId: 1, bookName: '日常账本', accountId: 2, accountName: '招商银行储蓄卡', date: '2026-03-01', note: '3 月工资' }
  ],

  // 统计数据
  stats: {
    monthly: { totalExpense: 3280.50, totalIncome: 8500.00, balance: 5219.50, dailyExpense: 109.35, balanceRate: 61.4 },
    category: [
      { categoryId: 1, categoryName: '餐饮', categoryIcon: 'FastFood', categoryColor: '#ff9900', amount: 1280.50, percent: 39.0 },
      { categoryId: 3, categoryName: '购物', categoryIcon: 'Cart', categoryColor: '#ff6b6b', amount: 850.00, percent: 25.9 },
      { categoryId: 2, categoryName: '交通', categoryIcon: 'Car', categoryColor: '#3385ff', amount: 420.00, percent: 12.8 }
    ]
  }
}

// API Mock 实现
export const mockApi = {
  auth: {
    login: async (data) => {
      await delay()
      if (data.username && data.password === '123456') {
        return { code: 200, message: '登录成功', data: { token: 'mock-jwt-token-' + Date.now(), expiresIn: 7200, user: mockData.user } }
      }
      throw { code: 401, message: '用户名或密码错误' }
    },
    register: async (data) => {
      await delay()
      return { code: 200, message: '注册成功', data: { id: Date.now(), username: data.username } }
    },
    logout: async () => { await delay(); return { code: 200, message: '登出成功' } },
    me: async () => { await delay(); return { code: 200, data: mockData.user } }
  },

  books: {
    list: async (params) => { await delay(); return { code: 200, data: { list: mockData.books, total: mockData.books.length, page: params?.page || 1, pageSize: params?.pageSize || 10 } } },
    create: async (data) => { await delay(); const newBook = { id: Date.now(), ...data, memberCount: 1, recordCount: 0, isDefault: false }; mockData.books.push(newBook); return { code: 200, message: '创建成功', data: newBook } },
    update: async (id, data) => { await delay(); const index = mockData.books.findIndex(b => b.id === id); if (index > -1) { mockData.books[index] = { ...mockData.books[index], ...data }; return { code: 200, message: '更新成功', data: mockData.books[index] } }; throw { code: 404, message: '账本不存在' } },
    delete: async (id) => { await delay(); const index = mockData.books.findIndex(b => b.id === id); if (index > -1) { if (mockData.books[index].isDefault) throw { code: 403, message: '默认账本不可删除' }; mockData.books.splice(index, 1); return { code: 200, message: '删除成功' } }; throw { code: 404, message: '账本不存在' } }
  },

  categories: {
    list: async (params) => { await delay(); const type = params?.type || 'expense'; return { code: 200, data: { defaults: mockData.categories[type].filter(c => c.isDefault), customs: mockData.categories[type].filter(c => !c.isDefault) } } },
    create: async (data) => { await delay(); const newCategory = { id: Date.now(), ...data, isDefault: false }; mockData.categories[data.type].push(newCategory); return { code: 200, message: '创建成功', data: newCategory } },
    update: async (id, data) => { await delay(); const type = data.type || 'expense'; const index = mockData.categories[type].findIndex(c => c.id === id); if (index > -1) { mockData.categories[type][index] = { ...mockData.categories[type][index], ...data }; return { code: 200, message: '更新成功', data: mockData.categories[type][index] } }; throw { code: 404, message: '分类不存在' } },
    delete: async (id) => { await delay(); const allCategories = [...mockData.categories.expense, ...mockData.categories.income]; const category = allCategories.find(c => c.id === id); if (category) { if (category.isDefault) throw { code: 403, message: '默认分类不可删除' }; const type = category.type; const index = mockData.categories[type].findIndex(c => c.id === id); mockData.categories[type].splice(index, 1); return { code: 200, message: '删除成功' } }; throw { code: 404, message: '分类不存在' } }
  },

  accounts: {
    list: async () => { await delay(); return { code: 200, data: mockData.accounts } },
    summary: async () => { await delay(); const totalAssets = mockData.accounts.filter(a => a.balance >= 0).reduce((sum, a) => sum + a.balance, 0); const totalDebt = Math.abs(mockData.accounts.filter(a => a.balance < 0).reduce((sum, a) => sum + a.balance, 0)); return { code: 200, data: { totalAssets: totalAssets.toFixed(2), totalDebt: totalDebt.toFixed(2), netAssets: (totalAssets - totalDebt).toFixed(2) } } },
    create: async (data) => { await delay(); const newAccount = { id: Date.now(), ...data }; mockData.accounts.push(newAccount); return { code: 200, message: '创建成功', data: newAccount } },
    update: async (id, data) => { await delay(); const index = mockData.accounts.findIndex(a => a.id === id); if (index > -1) { mockData.accounts[index] = { ...mockData.accounts[index], ...data }; return { code: 200, message: '更新成功', data: mockData.accounts[index] } }; throw { code: 404, message: '账户不存在' } },
    delete: async (id) => { await delay(); const index = mockData.accounts.findIndex(a => a.id === id); if (index > -1) { mockData.accounts.splice(index, 1); return { code: 200, message: '删除成功' } }; throw { code: 404, message: '账户不存在' } }
  },

  transactions: {
    list: async (params) => { await delay(); let list = [...mockData.transactions]; if (params?.bookId) list = list.filter(t => t.bookId === params.bookId); if (params?.type) list = list.filter(t => t.type === params.type); const page = params?.page || 1; const pageSize = params?.pageSize || 20; const total = list.length; list = list.slice((page - 1) * pageSize, page * pageSize); return { code: 200, data: { list, total, page, pageSize } } },
    create: async (data) => { await delay(); const newTransaction = { id: Date.now(), ...data }; mockData.transactions.unshift(newTransaction); return { code: 200, message: '记账成功', data: newTransaction } },
    update: async (id, data) => { await delay(); const index = mockData.transactions.findIndex(t => t.id === id); if (index > -1) { mockData.transactions[index] = { ...mockData.transactions[index], ...data }; return { code: 200, message: '更新成功', data: mockData.transactions[index] } }; throw { code: 404, message: '交易记录不存在' } },
    delete: async (id) => { await delay(); const index = mockData.transactions.findIndex(t => t.id === id); if (index > -1) { mockData.transactions.splice(index, 1); return { code: 200, message: '删除成功' } }; throw { code: 404, message: '交易记录不存在' } }
  },

  stats: {
    monthly: async (params) => { await delay(); return { code: 200, data: mockData.stats.monthly } },
    category: async (params) => { await delay(); return { code: 200, data: mockData.stats.category } }
  }
}

export default mockData
