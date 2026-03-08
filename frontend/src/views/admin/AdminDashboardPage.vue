<template>
  <div class="page-shell admin-page">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">管理看板</div>
        <h2 class="page-title">系统监控与 AI 配置总览</h2>
        <p class="page-subtitle">
          用于查看接口审计、识别任务数量、AI 渠道配置和当前运行状态。
        </p>
      </div>
    </section>

    <section class="metric-grid">
      <div class="metric-card glass-card">
        <div class="metric-label">用户数</div>
        <div class="metric-value">{{ overview.userCount || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">餐次记录</div>
        <div class="metric-value">{{ overview.mealCount || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">识别任务</div>
        <div class="metric-value">{{ overview.visionTaskCount || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">AI 状态</div>
        <div class="metric-value">{{ aiConfig.enabled ? '已启用' : '未启用' }}</div>
      </div>
    </section>

    <section class="section-grid admin-grid">
      <div class="page-card">
        <div class="page-eyebrow">AI 渠道配置</div>
        <div class="config-grid">
          <div class="config-item">
            <span>渠道</span>
            <strong>{{ aiConfig.channelName || '未配置' }}</strong>
          </div>
          <div class="config-item">
            <span>对话模型</span>
            <strong>{{ aiConfig.chatModel || '未配置' }}</strong>
          </div>
          <div class="config-item">
            <span>识图模型</span>
            <strong>{{ aiConfig.visionModel || '未配置' }}</strong>
          </div>
          <div class="config-item">
            <span>请求超时</span>
            <strong>{{ aiConfig.timeoutSeconds || 0 }} 秒</strong>
          </div>
          <div class="config-item config-item--full">
            <span>Base URL</span>
            <strong>{{ aiConfig.baseUrl || '未配置' }}</strong>
          </div>
          <div class="config-item config-item--full">
            <span>密钥状态</span>
            <strong>{{ aiConfig.hasApiKey ? aiConfig.maskedApiKey : '未配置' }}</strong>
          </div>
        </div>
        <p class="admin-tip">
          当前配置来源仍是后端环境变量。管理端先做“可视化查看”，后续如需在线修改，再补安全的持久化和加密存储。
        </p>
      </div>

      <div class="page-card">
        <div class="page-eyebrow">审计日志</div>
        <div class="table-like admin-log-list" style="margin-top: 16px">
          <div
            class="table-row admin-log-row"
            v-for="log in logs"
            :key="`${log.traceId}-${log.createdAt}`"
          >
            <span>{{ log.traceId }}</span>
            <span>{{ log.requestUri }}</span>
            <span>{{ log.requestMethod }}</span>
            <span>{{ log.responseCode }}</span>
            <span>{{ log.durationMs }}ms</span>
            <span>{{ log.createdAt }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAdminOverviewApi, getAiConfigApi, getAuditLogsApi, type AdminAiConfig } from '@/api/admin'

const overview = ref<any>({})
const logs = ref<any[]>([])
const aiConfig = ref<AdminAiConfig>({
  enabled: false,
  channelName: '',
  baseUrl: '',
  hasApiKey: false,
  maskedApiKey: '',
  chatModel: '',
  visionModel: '',
  timeoutSeconds: 0
})

onMounted(async () => {
  const [overviewData, aiConfigData, logData] = await Promise.all([
    getAdminOverviewApi(),
    getAiConfigApi(),
    getAuditLogsApi()
  ])
  overview.value = overviewData
  aiConfig.value = aiConfigData
  logs.value = logData
})
</script>

<style scoped>
.admin-page {
  gap: 24px;
}

.admin-grid {
  align-items: start;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.config-item {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-item--full {
  grid-column: 1 / -1;
}

.config-item span {
  color: var(--color-text-sub);
  font-size: 12px;
}

.admin-tip {
  margin-top: 16px;
  line-height: 1.8;
  color: var(--color-text-sub);
}

.admin-log-list {
  gap: 10px;
}

.admin-log-row {
  grid-template-columns: 1.15fr 1.9fr 0.8fr 0.8fr 0.8fr 1.1fr;
}

@media (max-width: 1100px) {
  .config-grid {
    grid-template-columns: 1fr;
  }

  .admin-log-row {
    grid-template-columns: 1fr;
  }
}
</style>
