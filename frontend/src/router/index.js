import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { requiresAuth: false }
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
        component: () => import('@/views/Dashboard/index.vue')
      },
      {
        path: 'transaction',
        name: 'Transaction',
        component: () => import('@/views/Transaction/List.vue')
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('@/views/Book/index.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics/index.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings/index.vue')
      },
      {
        path: 'budget',
        name: 'Budget',
        component: () => import('@/views/budget/BudgetList.vue')
      },
      {
        path: 'budget/create',
        name: 'BudgetCreate',
        component: () => import('@/views/budget/BudgetForm.vue')
      },
      {
        path: 'budget/:id/edit',
        name: 'BudgetEdit',
        component: () => import('@/views/budget/BudgetForm.vue')
      },
      {
        path: 'budget/:id',
        name: 'BudgetDetail',
        component: () => import('@/views/budget/BudgetDetail.vue')
      },
      {
        path: 'recurring',
        name: 'RecurringBill',
        component: () => import('@/views/recurring/RecurringList.vue')
      },
      {
        path: 'recurring/create',
        name: 'RecurringBillCreate',
        component: () => import('@/views/recurring/RecurringForm.vue')
      },
      {
        path: 'recurring/:id/edit',
        name: 'RecurringBillEdit',
        component: () => import('@/views/recurring/RecurringForm.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

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
