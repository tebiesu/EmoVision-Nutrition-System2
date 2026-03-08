<template>
  <div class="main-layout">
    <aside class="main-layout__sidebar glass-card">
      <div class="sidebar-atmosphere"></div>

      <section class="brand-block">
        <div class="brand-mark">EH</div>
        <div>
          <div class="brand-title">情绪饮食助手</div>
          <div class="brand-subtitle">把情绪、饮食、识别与建议整理进同一条健康闭环</div>
        </div>
      </section>

      <section class="overview-panel">
        <div class="overview-panel__eyebrow">今日节律</div>
        <div class="overview-panel__title">记录、识别、分析、对话四条线协同工作，形成持续可追踪的健康决策链。</div>
        <div class="overview-panel__tags">
          <span>饮食记录</span>
          <span>图像识别</span>
          <span>趋势分析</span>
          <span>营养助手</span>
        </div>
      </section>

      <nav class="nav-list">
        <RouterLink
          v-for="item in navigation"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'nav-item--active': isActive(item.path) }"
          :aria-current="isActive(item.path) ? 'page' : undefined"
        >
          <div class="nav-item__line"></div>
          <div class="nav-item__icon">{{ item.icon }}</div>
          <div class="nav-item__copy">
            <span class="nav-item__label">{{ item.label }}</span>
            <span class="nav-item__hint">{{ item.hint }}</span>
          </div>
          <span class="nav-item__tail">{{ item.serial }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer glass-card">
        <div class="user-chip">
          <strong>{{ displayName }}</strong>
          <span>{{ roleLabel }}</span>
        </div>
        <el-button class="soft-button soft-button--secondary" @click="handleLogout">退出登录</el-button>
      </div>
    </aside>

    <div class="main-layout__body">
      <header class="main-layout__header glass-card">
        <div>
          <div class="page-eyebrow">健康闭环</div>
          <h1 class="header-title">让每一餐、每一次情绪波动和每一条建议都能被追踪与解释</h1>
        </div>
        <div class="header-meta">
          <div class="badge-chip">{{ roleLabel }}</div>
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
import { useRoute, useRouter } from 'vue-router'
import { logoutApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navigation = [
  { path: '/dashboard', label: '工作台', hint: '今日概览与核心提醒', icon: '◌', serial: '01' },
  { path: '/profile', label: '健康档案', hint: '目标、身体数据与饮食边界', icon: '◎', serial: '02' },
  { path: '/meals', label: '饮食记录', hint: '上传、识别、确认与评估', icon: '◍', serial: '03' },
  { path: '/analysis/trends', label: '趋势分析', hint: '热量、情绪与营养结构变化', icon: '◐', serial: '04' },
  { path: '/assistant', label: '营养助手', hint: '结合档案和近期饮食给出建议', icon: '✦', serial: '05' },
  { path: '/admin', label: '管理监控', hint: '审计日志、AI 配置与系统运行概览', icon: '▣', serial: '06' }
]

const displayName = computed(() => authStore.userInfo.nickname || authStore.userInfo.username || '访客')
const roleLabel = computed(() => (authStore.userInfo.roleCode === 'ADMIN' ? '管理员' : '普通用户'))
const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

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
  grid-template-columns: 300px 1fr;
  gap: 24px;
  padding: 24px;
}

.main-layout__sidebar {
  position: relative;
  overflow: hidden;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: linear-gradient(180deg, rgba(248, 249, 244, 0.92), rgba(239, 242, 233, 0.88));
}

.sidebar-atmosphere {
  position: absolute;
  inset: auto -96px 22% auto;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(215, 185, 141, 0.34), rgba(215, 185, 141, 0));
  pointer-events: none;
}

.brand-block {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-mark {
  width: 58px;
  height: 58px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #7da482 0%, #a8bea3 100%);
  color: #fff;
  font-weight: 700;
  font-size: 24px;
  box-shadow: 0 18px 30px rgba(125, 164, 130, 0.22);
}

.brand-title {
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--color-text-sub);
  line-height: 1.7;
}

.overview-panel {
  position: relative;
  padding: 18px;
  border-radius: 24px;
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.84), rgba(244, 247, 240, 0.7)),
    radial-gradient(circle at top right, rgba(125, 164, 130, 0.18), transparent 42%);
  border: 1px solid rgba(125, 164, 130, 0.12);
}

.overview-panel__eyebrow {
  font-size: 11px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--color-primary-deep);
}

.overview-panel__title {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text-main);
}

.overview-panel__tags {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.overview-panel__tags span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--color-primary-deep);
  font-size: 12px;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-item {
  position: relative;
  padding: 16px 16px 16px 18px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid rgba(125, 164, 130, 0.08);
  text-decoration: none;
  color: inherit;
  transition:
    transform var(--transition-smooth),
    box-shadow var(--transition-smooth),
    background var(--transition-smooth),
    border-color var(--transition-smooth);
}

.nav-item:hover {
  transform: translateX(3px);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 14px 24px rgba(125, 164, 130, 0.1);
}

.nav-item--active {
  transform: translateX(0);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(244, 247, 240, 0.88));
  border-color: rgba(125, 164, 130, 0.22);
  box-shadow: 0 18px 30px rgba(125, 164, 130, 0.14);
}

.nav-item__line {
  position: absolute;
  left: 10px;
  top: 14px;
  bottom: 14px;
  width: 4px;
  border-radius: 999px;
  background: transparent;
  transition: background var(--transition-smooth), transform var(--transition-smooth);
}

.nav-item--active .nav-item__line {
  background: linear-gradient(180deg, #7da482 0%, #d6b78c 100%);
}

.nav-item__icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  flex: none;
  background: linear-gradient(140deg, rgba(125, 164, 130, 0.16), rgba(215, 185, 141, 0.18));
  color: var(--color-primary-deep);
  font-size: 18px;
}

.nav-item--active .nav-item__icon {
  background: linear-gradient(140deg, rgba(125, 164, 130, 0.22), rgba(215, 185, 141, 0.3));
  box-shadow: inset 0 0 0 1px rgba(125, 164, 130, 0.14);
}

.nav-item__copy {
  flex: 1;
  min-width: 0;
}

.nav-item__label {
  display: block;
  font-weight: 700;
}

.nav-item__hint {
  display: block;
  margin-top: 4px;
  color: var(--color-text-sub);
  font-size: 12px;
  line-height: 1.5;
}

.nav-item__tail {
  color: rgba(95, 133, 101, 0.55);
  font-size: 12px;
  letter-spacing: 0.16em;
}

.sidebar-footer {
  margin-top: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.user-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-chip span {
  color: var(--color-text-sub);
  font-size: 13px;
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
  line-height: 1.4;
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
  0%,
  100% {
    transform: scale(1);
    opacity: 0.7;
  }

  50% {
    transform: scale(1.35);
    opacity: 1;
  }
}

@media (max-width: 1100px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}
</style>
