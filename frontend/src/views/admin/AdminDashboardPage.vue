<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">Admin</div>
        <h2 class="page-title">轻量化管理看板</h2>
        <p class="page-subtitle">用于演示接口统计、识别任务数量和 traceId 审计链路。</p>
      </div>
    </section>

    <section class="metric-grid">
      <div class="metric-card glass-card"><div class="metric-label">用户数</div><div class="metric-value">{{ overview.userCount || 0 }}</div></div>
      <div class="metric-card glass-card"><div class="metric-label">餐次记录</div><div class="metric-value">{{ overview.mealCount || 0 }}</div></div>
      <div class="metric-card glass-card"><div class="metric-label">识别任务</div><div class="metric-value">{{ overview.visionTaskCount || 0 }}</div></div>
    </section>

    <section class="page-card">
      <div class="page-eyebrow">Audit Logs</div>
      <div class="table-like" style="margin-top: 16px">
        <div class="table-row" v-for="log in logs" :key="`${log.traceId}-${log.createdAt}`" style="grid-template-columns: 1.2fr 2fr 1fr 1fr 1fr 1.5fr">
          <span>{{ log.traceId }}</span>
          <span>{{ log.requestUri }}</span>
          <span>{{ log.requestMethod }}</span>
          <span>{{ log.responseCode }}</span>
          <span>{{ log.durationMs }}ms</span>
          <span>{{ log.createdAt }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAdminOverviewApi, getAuditLogsApi } from '@/api/admin'

const overview = ref<any>({})
const logs = ref<any[]>([])

onMounted(async () => {
  overview.value = await getAdminOverviewApi()
  logs.value = await getAuditLogsApi()
})
</script>
