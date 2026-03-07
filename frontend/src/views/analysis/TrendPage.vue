<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">Trends</div>
        <h2 class="page-title">趋势分析</h2>
        <p class="page-subtitle">热量趋势、情绪趋势、营养雷达和关联散点统一展示。</p>
      </div>
      <div v-if="!trendData.hasEnoughData" class="badge-chip">数据不足时展示空态</div>
    </section>

    <section class="section-grid">
      <div class="page-card"><div ref="calorieChartRef" style="height: 320px"></div></div>
      <div class="page-card"><div ref="emotionChartRef" style="height: 320px"></div></div>
      <div class="page-card"><div ref="radarChartRef" style="height: 320px"></div></div>
      <div class="page-card"><div ref="correlationChartRef" style="height: 320px"></div></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { getTrendApi } from '@/api/analysis'

const trendData = ref<any>({ calorieTrend: [], emotionTrend: [], nutritionRadar: {}, correlation: [], hasEnoughData: false })
const calorieChartRef = ref<HTMLDivElement>()
const emotionChartRef = ref<HTMLDivElement>()
const radarChartRef = ref<HTMLDivElement>()
const correlationChartRef = ref<HTMLDivElement>()

const renderCharts = async () => {
  await nextTick()
  echarts.init(calorieChartRef.value!).setOption({
    xAxis: { type: 'category', data: trendData.value.calorieTrend.map((item: any) => item.date) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: trendData.value.calorieTrend.map((item: any) => item.value), areaStyle: {} }]
  })
  echarts.init(emotionChartRef.value!).setOption({
    xAxis: { type: 'category', data: trendData.value.emotionTrend.map((item: any) => item.date) },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{ type: 'line', smooth: true, data: trendData.value.emotionTrend.map((item: any) => item.value) }]
  })
  echarts.init(radarChartRef.value!).setOption({
    radar: { indicator: [{ name: 'Protein', max: 200 }, { name: 'Fat', max: 200 }, { name: 'Carbs', max: 300 }, { name: 'Balance', max: 100 }] },
    series: [{ type: 'radar', data: [{ value: [trendData.value.nutritionRadar.protein || 0, trendData.value.nutritionRadar.fat || 0, trendData.value.nutritionRadar.carbs || 0, trendData.value.nutritionRadar.balance || 0] }] }]
  })
  echarts.init(correlationChartRef.value!).setOption({
    xAxis: { type: 'value', name: 'Emotion' },
    yAxis: { type: 'value', name: 'Calories' },
    series: [{ type: 'scatter', data: trendData.value.correlation.map((item: any) => [item.emotionScore, item.calories]) }]
  })
}

onMounted(async () => {
  trendData.value = await getTrendApi()
  await renderCharts()
})
</script>
