<template>
  <router-view v-slot="{ Component, route }">
    <transition name="app-fade-slide" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
</template>

<style>
.app-fade-slide-enter-active,
.app-fade-slide-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.app-fade-slide-enter-from,
.app-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(18px);
}
</style>
