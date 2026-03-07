<template>
  <div class="main-layout">
    <aside class="main-layout__sidebar glass-card">
      <div class="brand-block">
        <div class="brand-mark">EH</div>
        <div>
          <div class="brand-title">EmoVision</div>
          <div class="brand-subtitle">Nutrition System</div>
        </div>
      </div>
      <nav class="nav-list">
        <RouterLink v-for="item in navigation" :key="item.path" :to="item.path" class="nav-item">
          <span class="nav-item__label">{{ item.label }}</span>
          <span class="nav-item__hint">{{ item.hint }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer glass-card">
        <div class="user-chip">{{ displayName }}</div>
        <el-button class="soft-button soft-button--secondary" @click="handleLogout">退出登录</el-button>
      </div>
    </aside>

    <div class="main-layout__body">
      <header class="main-layout__header glass-card">
        <div>
          <div class="page-eyebrow">Wellness Loop</div>
          <h1 class="header-title">基于情绪与饮食的个体化健康追踪</h1>
        </div>
        <div class="header-meta">
          <div class="badge-chip">{{ authStore.userInfo.roleCode || 'USER' }}</div>
          <div class="trace-dot"></div>
        </div>
      </header>

      <main class="main-layout__content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const navigation = [
  { path: '/dashboard', label: '工作台', hint: '今日概览' },
  { path: '/profile', label: '健康档案', hint: '目标与体征' },
  { path: '/meals', label: '饮食记录', hint: '上传与评估' },
  { path: '/analysis/trends', label: '趋势分析', hint: '图表洞察' },
  { path: '/admin', label: '管理监控', hint: '日志与链路' }
]

const displayName = computed(() => authStore.userInfo.nickname || authStore.userInfo.username || 'Guest')

const handleLogout = async () => {
  if (authStore.token) {
    await logoutApi().catch(() => undefined)
  }
  authStore.clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  padding: 24px;
}

.main-layout__sidebar {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-mark {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, #9cb59f 100%);
  color: white;
  font-weight: 700;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  font-size: 13px;
  color: var(--color-text-sub);
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-item {
  padding: 14px 16px;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: transform var(--transition-smooth), background var(--transition-smooth);
}

.nav-item:hover,
.nav-item.router-link-active {
  background: rgba(125, 164, 130, 0.14);
  transform: translateX(4px);
}

.nav-item__label {
  font-weight: 600;
}

.nav-item__hint {
  color: var(--color-text-sub);
  font-size: 13px;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-chip {
  font-weight: 600;
}

.main-layout__body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.main-layout__header {
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  margin: 8px 0 0;
  font-size: 28px;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.trace-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8cc084 0%, #d7b98d 100%);
  animation: breathe 2.8s ease-in-out infinite;
}

.main-layout__content {
  min-height: 0;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.35); opacity: 1; }
}

@media (max-width: 1100px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}
</style>
