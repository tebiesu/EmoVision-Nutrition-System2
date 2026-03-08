<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">工作台总览</div>
        <h2 class="page-title">今日饮食与情绪总览</h2>
        <p class="page-subtitle">将档案、饮食记录、风险评估和建议摘要收在一屏里，适合作为演示主流程。</p>
      </div>
    </section>

    <section class="metric-grid">
      <div class="metric-card glass-card">
        <div class="metric-label">今日餐次</div>
        <div class="metric-value">{{ summary.todayMealCount || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">今日总热量</div>
        <div class="metric-value">{{ summary.dailySummary?.totalCalories || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">平均情绪分</div>
        <div class="metric-value">{{ summary.dailySummary?.avgEmotionScore || 0 }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">最新风险等级</div>
        <div class="metric-value">{{ latestRisk }}</div>
      </div>
    </section>

    <section class="section-grid">
      <div class="page-card">
        <div class="page-eyebrow">最新建议</div>
        <h3>{{ latestRecommendation.summaryText || '暂时还没有建议' }}</h3>
        <p class="page-subtitle">
          {{ latestRecommendation.recommendationText || '录入一条餐次后，这里会显示系统建议。' }}
        </p>
      </div>
      <div class="page-card">
        <div class="page-eyebrow">每日总结</div>
        <p class="page-subtitle">{{ summary.dailySummary?.summaryText || '今天还没有形成日报总结。' }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDashboardSummaryApi } from '@/api/dashboard'

const summary = ref<any>({})
const latestRecommendation = computed(() => summary.value.latestMeal?.recommendation || {})

const riskLabelMap: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险'
}

const latestRisk = computed(() => riskLabelMap[summary.value.latestMeal?.assessment?.riskLevel] || '暂无')

onMounted(async () => {
  summary.value = await getDashboardSummaryApi()
})
</script>
