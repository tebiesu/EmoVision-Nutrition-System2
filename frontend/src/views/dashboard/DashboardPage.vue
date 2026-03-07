<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">Dashboard</div>
        <h2 class="page-title">今日饮食与情绪总览</h2>
        <p class="page-subtitle">将档案、饮食记录、风险评估和建议摘要收在一屏里，适合演示主流程。</p>
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
        <div class="page-eyebrow">Latest Recommendation</div>
        <h3>{{ latestRecommendation.summaryText || '暂无建议' }}</h3>
        <p class="page-subtitle">{{ latestRecommendation.recommendationText || '录入一条餐次后，这里会展示系统建议。' }}</p>
      </div>
      <div class="page-card">
        <div class="page-eyebrow">Daily Summary</div>
        <p class="page-subtitle">{{ summary.dailySummary?.summaryText || '今天还没有形成日总结。' }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDashboardSummaryApi } from '@/api/dashboard'

const summary = ref<any>({})
const latestRecommendation = computed(() => summary.value.latestMeal?.recommendation || {})
const latestRisk = computed(() => summary.value.latestMeal?.assessment?.riskLevel || 'N/A')

onMounted(async () => {
  summary.value = await getDashboardSummaryApi()
})
</script>
