<template>
  <div class="main-layout">
    <header class="main-layout__header">
      <div class="main-layout__title">个人饮食健康智能助手</div>
    </header>
    <main class="main-layout__content">
      <router-view />
    </main>
    <nav class="main-layout__tabbar">
      <router-link to="/dashboard">工作台</router-link>
      <router-link to="/meals">饮食</router-link>
      <router-link to="/analysis/trends">趋势</router-link>
      <router-link to="/profile">我的</router-link>
    </nav>
  </div>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7f2;
}

.main-layout__header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 20px;
}

.main-layout__title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.main-layout__content {
  flex: 1;
  padding: 16px;
}

.main-layout__tabbar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
}

.main-layout__tabbar a {
  text-align: center;
  color: #6b7280;
  text-decoration: none;
  font-size: 14px;
}

.main-layout__tabbar a.router-link-active {
  color: #16a34a;
  font-weight: 600;
}
</style>
