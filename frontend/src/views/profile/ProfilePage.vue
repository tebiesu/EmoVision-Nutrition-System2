<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">Profile</div>
        <h2 class="page-title">健康档案</h2>
        <p class="page-subtitle">用于驱动 BMI、BMR 和饮食建议的基础个人信息。</p>
      </div>
    </section>

    <section class="page-card">
      <el-form :model="form" label-position="top" class="table-like">
        <div class="section-grid">
          <div>
            <el-form-item label="年龄"><el-input-number v-model="form.age" :min="10" :max="100" /></el-form-item>
            <el-form-item label="性别">
              <el-select v-model="form.gender"><el-option label="male" value="male" /><el-option label="female" value="female" /></el-select>
            </el-form-item>
            <el-form-item label="身高(cm)"><el-input-number v-model="form.heightCm" :min="80" :max="240" /></el-form-item>
            <el-form-item label="体重(kg)"><el-input-number v-model="form.weightKg" :min="20" :max="200" /></el-form-item>
          </div>
          <div>
            <el-form-item label="活动水平"><el-input v-model="form.activityLevel" /></el-form-item>
            <el-form-item label="目标"><el-input v-model="form.goal" placeholder="fat-loss / maintain / muscle-gain" /></el-form-item>
            <el-form-item label="过敏信息"><el-input v-model="form.allergies" /></el-form-item>
            <el-form-item label="忌口"><el-input v-model="form.tabooFoods" /></el-form-item>
            <el-form-item label="基础疾病"><el-input v-model="form.medicalConditions" /></el-form-item>
          </div>
        </div>
        <el-button class="soft-button" @click="handleSave">保存档案</el-button>
      </el-form>
    </section>

    <section class="metric-grid">
      <div class="metric-card glass-card">
        <div class="metric-label">BMI</div>
        <div class="metric-value">{{ profile?.bmi || '--' }}</div>
      </div>
      <div class="metric-card glass-card">
        <div class="metric-label">BMR</div>
        <div class="metric-value">{{ profile?.bmr || '--' }}</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfileApi, saveProfileApi } from '@/api/profile'
import type { Profile } from '@/types/api'

const profile = ref<Profile | null>(null)
const form = reactive({
  age: 22,
  gender: 'male',
  heightCm: 175,
  weightKg: 68,
  activityLevel: 'moderate',
  goal: 'maintain',
  allergies: '',
  tabooFoods: '',
  medicalConditions: ''
})

const loadProfile = async () => {
  try {
    const data = await getProfileApi()
    profile.value = data
    Object.assign(form, data)
  } catch {
    profile.value = null
  }
}

const handleSave = async () => {
  profile.value = await saveProfileApi(form)
  ElMessage.success('档案已保存')
}

onMounted(loadProfile)
</script>
