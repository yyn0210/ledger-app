import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
<<<<<<< HEAD
    meta: { requiresAuth: false }
=======
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register/index.vue'),
    meta: { requiresAuth: false, title: '注册' }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
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
<<<<<<< HEAD
        component: () => import('@/views/Dashboard/index.vue')
=======
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'home' }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      },
      {
        path: 'transaction',
        name: 'Transaction',
<<<<<<< HEAD
        component: () => import('@/views/Transaction/List.vue')
=======
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
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      },
      {
        path: 'book',
        name: 'Book',
<<<<<<< HEAD
        component: () => import('@/views/Book/index.vue')
=======
        component: () => import('@/views/Book/index.vue'),
        meta: { title: '账本管理', icon: 'book' }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      },
      {
        path: 'statistics',
        name: 'Statistics',
<<<<<<< HEAD
        component: () => import('@/views/Statistics/index.vue')
=======
        component: () => import('@/views/Statistics/index.vue'),
        meta: { title: '统计分析', icon: 'pie-chart' }
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
      },
      {
        path: 'settings',
        name: 'Settings',
<<<<<<< HEAD
        component: () => import('@/views/Settings/index.vue')
      }
    ]
=======
        component: () => import('@/views/Settings/index.vue'),
        meta: { title: '设置', icon: 'settings' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

<<<<<<< HEAD
=======
// 路由守卫
>>>>>>> 1d48db51b0bcbb5434e8d88420eea15f9c38acc3
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
