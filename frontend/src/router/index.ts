import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'

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
      { path: 'assistant', component: () => import('@/views/assistant/NutritionAssistantPage.vue') },
      { path: 'admin', component: () => import('@/views/admin/AdminDashboardPage.vue'), meta: { requiresAdmin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  const authPages = ['/login', '/register']

  if (!authStore.isAuthenticated && !authPages.includes(to.path)) {
    return '/login'
  }

  if (authStore.isAuthenticated && authPages.includes(to.path)) {
    return '/dashboard'
  }

  if (authStore.isAuthenticated) {
    await authStore.hydrateUser()
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      return '/dashboard'
    }
  }

  return true
})

export default router