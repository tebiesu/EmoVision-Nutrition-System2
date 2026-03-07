import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/auth/LoginPage.vue')
  },
  {
    path: '/register',
    component: () => import('@/views/auth/RegisterPage.vue')
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('@/views/dashboard/DashboardPage.vue') },
      { path: 'profile', component: () => import('@/views/profile/ProfilePage.vue') },
      { path: 'meals', component: () => import('@/views/meal/MealListPage.vue') },
      { path: 'analysis/trends', component: () => import('@/views/analysis/TrendPage.vue') },
      { path: 'admin', component: () => import('@/views/admin/AdminDashboardPage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('health-assistant-token')
  const authPages = ['/login', '/register']

  if (!token && !authPages.includes(to.path)) {
    return '/login'
  }

  if (token && authPages.includes(to.path)) {
    return '/dashboard'
  }

  return true
})

export default router
