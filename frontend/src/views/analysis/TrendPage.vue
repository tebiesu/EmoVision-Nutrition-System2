<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">趋势洞察</div>
        <h2 class="page-title">趋势分析</h2>
        <p class="page-subtitle">热量趋势、情绪趋势、营养雷达和关联散点统一展示。</p>
      </div>
      <div v-if="!trendData.hasEnoughData" class="badge-chip">数据不足时将显示简化图表</div>
    </section>

    <section class="section-grid trend-grid">
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

const trendData = ref<any>({
  calorieTrend: [],
  emotionTrend: [],
  nutritionRadar: {},
  correlation: [],
  hasEnoughData: false
})

const calorieChartRef = ref<HTMLDivElement>()
const emotionChartRef = ref<HTMLDivElement>()
const radarChartRef = ref<HTMLDivElement>()
const correlationChartRef = ref<HTMLDivElement>()

const renderCharts = async () => {
  await nextTick()

  echarts.init(calorieChartRef.value!).setOption({
    title: { text: '热量趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendData.value.calorieTrend.map((item: any) => item.date) },
    yAxis: { type: 'value', name: '热量' },
    series: [
      {
        name: '热量',
        type: 'line',
        smooth: true,
        data: trendData.value.calorieTrend.map((item: any) => item.value),
        areaStyle: {}
      }
    ]
  })

  echarts.init(emotionChartRef.value!).setOption({
    title: { text: '情绪趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendData.value.emotionTrend.map((item: any) => item.date) },
    yAxis: { type: 'value', min: 0, max: 5, name: '情绪分' },
    series: [
      {
        name: '情绪分',
        type: 'line',
        smooth: true,
        data: trendData.value.emotionTrend.map((item: any) => item.value)
      }
    ]
  })

  echarts.init(radarChartRef.value!).setOption({
    title: { text: '营养雷达' },
    radar: {
      indicator: [
        { name: '蛋白质', max: 200 },
        { name: '脂肪', max: 200 },
        { name: '碳水', max: 300 },
        { name: '均衡度', max: 100 }
      ]
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [
              trendData.value.nutritionRadar.protein || 0,
              trendData.value.nutritionRadar.fat || 0,
              trendData.value.nutritionRadar.carbs || 0,
              trendData.value.nutritionRadar.balance || 0
            ]
          }
        ]
      }
    ]
  })

  echarts.init(correlationChartRef.value!).setOption({
    title: { text: '情绪与热量关联' },
    tooltip: { trigger: 'item' },
    xAxis: { type: 'value', name: '情绪分' },
    yAxis: { type: 'value', name: '热量' },
    series: [
      {
        name: '关联点',
        type: 'scatter',
        data: trendData.value.correlation.map((item: any) => [item.emotionScore, item.calories])
      }
    ]
  })
}

onMounted(async () => {
  trendData.value = await getTrendApi()
  await renderCharts()
})
</script>

<style scoped>
.trend-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 1100px) {
  .trend-grid {
    grid-template-columns: 1fr;
  }
}
</style>
