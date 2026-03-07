<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">Meal Flow</div>
        <h2 class="page-title">饮食记录与识别确认</h2>
        <p class="page-subtitle">支持上传图片、发起识别任务、确认候选食物、录入情绪并生成评估建议。</p>
      </div>
    </section>

    <section class="page-card">
      <div class="section-grid">
        <div>
          <el-form label-position="top">
            <el-form-item label="餐次"><el-input v-model="form.mealType" placeholder="breakfast / lunch / dinner / snack" /></el-form-item>
            <el-form-item label="进食时间"><el-date-picker v-model="form.eatenAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
            <el-form-item label="饮食描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="例如：chicken rice salad" /></el-form-item>
            <el-form-item label="上传图片"><input type="file" accept="image/*" @change="handleFileChange" /></el-form-item>
            <el-form-item label="情绪分值"><el-slider v-model="form.selfRating" :min="1" :max="5" :step="1" show-stops /></el-form-item>
            <el-form-item label="情绪补充"><el-input v-model="form.emotionText" placeholder="happy / stress / timeout-emotion / fallback-ai" /></el-form-item>
          </el-form>
        </div>
        <div>
          <div class="page-card" style="padding: 20px">
            <div class="page-eyebrow">Recognition</div>
            <div style="display: flex; gap: 12px; margin: 16px 0">
              <el-button class="soft-button" @click="handleRecognize">发起识别</el-button>
              <el-button class="soft-button soft-button--secondary" @click="addManualItem">手动添加食物</el-button>
            </div>
            <div class="table-like">
              <div class="table-row" v-for="(item, index) in items" :key="index">
                <el-input v-model="item.foodName" placeholder="食物名" />
                <el-input-number v-model="item.amount" :min="0" />
                <el-input v-model="item.unit" placeholder="g/ml" />
                <el-input-number v-model="item.calories" :min="0" />
                <el-input-number v-model="item.protein" :min="0" />
                <el-input-number v-model="item.fat" :min="0" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div style="margin-top: 20px">
        <el-button class="soft-button" @click="handleCreateMeal">保存餐次并生成评估</el-button>
      </div>
    </section>

    <section class="page-card">
      <div class="page-eyebrow">Meal History</div>
      <div class="table-like" style="margin-top: 16px">
        <div class="page-card" v-for="meal in meals" :key="meal.id" style="padding: 18px">
          <strong>{{ meal.mealType }}</strong> · {{ meal.eatenAt }}
          <p>{{ meal.description }}</p>
          <p>风险：{{ meal.assessment.riskLevel }} | 热量：{{ meal.assessment.totalCalories }}</p>
          <p>建议：{{ meal.recommendation.recommendationText }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMealsApi, createMealApi } from '@/api/meal'
import { createVisionTaskApi } from '@/api/vision'
import { uploadFileApi } from '@/api/file'
import type { MealItem, MealRecord } from '@/types/api'

const meals = ref<MealRecord[]>([])
const form = reactive({
  mealType: 'lunch',
  eatenAt: new Date().toISOString().slice(0, 19),
  description: 'chicken rice salad',
  imageUrl: '',
  recognitionTaskId: undefined as number | undefined,
  selfRating: 4,
  emotionText: 'calm'
})
const items = ref<MealItem[]>([])

const loadMeals = async () => {
  meals.value = await listMealsApi()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const uploaded = await uploadFileApi(file)
  form.imageUrl = uploaded.url
  ElMessage.success('图片上传成功')
}

const handleRecognize = async () => {
  const result = await createVisionTaskApi({ imageUrl: form.imageUrl, description: form.description })
  form.recognitionTaskId = result.taskId
  items.value = result.candidates.map((candidate) => ({
    foodName: candidate.foodName,
    amount: candidate.amount,
    unit: candidate.unit,
    calories: candidate.calories,
    protein: candidate.protein,
    fat: candidate.fat,
    carbs: candidate.carbs,
    source: 'VISION',
    confirmed: true
  }))
  if (!items.value.length) {
    ElMessage.warning('识别为空，请手动补录')
  }
}

const addManualItem = () => {
  items.value.push({ foodName: '', amount: 100, unit: 'g', calories: 0, protein: 0, fat: 0, carbs: 0, source: 'MANUAL', confirmed: true })
}

const handleCreateMeal = async () => {
  await createMealApi({ ...form, items: items.value })
  ElMessage.success('餐次已保存并生成评估')
  items.value = []
  await loadMeals()
}

onMounted(loadMeals)
</script>
