import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register/index.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'home' }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('@/views/Book/index.vue'),
        meta: { title: '账本管理', icon: 'book' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category/index.vue'),
        meta: { title: '分类管理', icon: 'apps' }
      },
      {
        path: 'account',
        name: 'Account',
        component: () => import('@/views/Account/index.vue'),
        meta: { title: '账户管理', icon: 'wallet' }
      },
      {
        path: 'transaction',
        name: 'Transaction',
        component: () => import('@/views/Transaction/index.vue'),
        meta: { title: '交易记录', icon: 'swap-horizontal' }
      },
      {
        path: 'budget',
        name: 'Budget',
        component: () => import('@/views/Budget/index.vue'),
        meta: { title: '预算管理', icon: 'pie-chart' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics/index.vue'),
        meta: { title: '统计分析', icon: 'trending-up' }
      },
      {
        path: 'transaction',
        name: 'Transaction',
        redirect: '/transaction/list',
        meta: { title: '交易管理', icon: 'swap-horizontal' },
        children: [
          {
            path: 'list',
            name: 'TransactionList',
            component: () => import('@/views/Transaction/List.vue'),
            meta: { title: '交易列表' }
          },
          {
            path: 'create',
            name: 'TransactionCreate',
            component: () => import('@/views/Transaction/Create.vue'),
            meta: { title: '新建交易' }
          },
          {
            path: 'edit/:id',
            name: 'TransactionEdit',
            component: () => import('@/views/Transaction/Edit.vue'),
            meta: { title: '编辑交易' }
          }
        ]
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics/index.vue'),
        meta: { title: '统计分析', icon: 'pie-chart' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings/index.vue'),
        meta: { title: '设置', icon: 'settings' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
