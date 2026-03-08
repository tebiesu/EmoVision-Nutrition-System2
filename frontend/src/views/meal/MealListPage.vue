<template>
  <div class="page-shell meal-page">
    <section class="page-header">
      <div>
        <div class="page-eyebrow">饮食流程</div>
        <h2 class="page-title">饮食记录与识别确认</h2>
        <p class="page-subtitle">
          先记录这一餐，再通过图片识别生成候选食物，最后确认营养项并生成评估。
          当前链路是“AI 自动识别 + 人工核对兜底”。
        </p>
      </div>
    </section>

    <section class="page-card meal-card">
      <div class="meal-grid">
        <div class="meal-form-block">
          <div class="block-title">基础信息</div>
          <el-form label-position="top">
            <el-form-item label="餐次">
              <el-select v-model="form.mealType" placeholder="请选择餐次" style="width: 100%">
                <el-option
                  v-for="option in mealTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="进食时间">
              <el-date-picker
                v-model="form.eatenAt"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="请选择进食时间"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="饮食描述">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="例如：鸡胸肉、米饭、蔬菜沙拉和一杯无糖酸奶"
              />
            </el-form-item>

            <el-form-item label="上传图片">
              <input type="file" accept="image/*" @change="handleFileChange" />
              <div v-if="uploadedFileName" class="upload-success">
                已上传：{{ uploadedFileName }}
              </div>
              <div v-if="form.imageUrl" class="image-preview">
                <img :src="form.imageUrl" alt="上传预览" />
              </div>
            </el-form-item>

            <el-form-item label="当前情绪">
              <el-select v-model="form.emotionLabel" placeholder="请选择当前情绪" style="width: 100%">
                <el-option
                  v-for="option in emotionOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="情绪强度">
              <el-slider v-model="form.selfRating" :min="1" :max="5" :step="1" show-stops />
              <div class="slider-hint">1 分表示状态较低，5 分表示状态很好。</div>
            </el-form-item>
          </el-form>

          <div class="block-title block-title--sub">辅助问卷</div>
          <div class="questionnaire-card">
            <div class="questionnaire-grid">
              <div>
                <label class="questionnaire-label">进食节奏</label>
                <el-select v-model="questionnaire.eatingPace" style="width: 100%">
                  <el-option v-for="option in paceOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </div>
              <div>
                <label class="questionnaire-label">当前饱腹感</label>
                <el-select v-model="questionnaire.fullness" style="width: 100%">
                  <el-option
                    v-for="option in fullnessOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>
              <div>
                <label class="questionnaire-label">饮品情况</label>
                <el-select v-model="questionnaire.drinkChoice" style="width: 100%">
                  <el-option
                    v-for="option in drinkOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>
              <div>
                <label class="questionnaire-label">触发因素</label>
                <el-select v-model="questionnaire.triggerTags" multiple collapse-tags collapse-tags-tooltip style="width: 100%">
                  <el-option
                    v-for="option in triggerOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>
            </div>
            <div class="questionnaire-summary">
              问卷摘要：{{ questionnaireSummary }}
            </div>
          </div>
        </div>

        <div class="recognition-panel">
          <div class="block-title">识别候选</div>
          <div class="recognition-summary">
            上传图片后会自动发起识别。视觉模型会尽量识别食物、分量、热量和三大营养素，
            如果模型不可用，会自动降级为低置信候选并提醒你手动核对。
          </div>

          <div class="recognition-actions">
            <el-button class="soft-button" :disabled="uploading || recognizing || !canRecognize" @click="startRecognition(false)">
              {{ recognizing ? '识别中...' : '重新识别' }}
            </el-button>
            <el-button class="soft-button soft-button--secondary" @click="addManualItem">手动添加食物</el-button>
          </div>

          <div class="recognition-status" :class="`recognition-status--${recognition.status.toLowerCase()}`">
            <strong>{{ recognitionTitle }}</strong>
            <span>{{ recognition.message }}</span>
          </div>

          <div class="candidate-guide">
            <span>分量：这一项大概吃了多少</span>
            <span>热量：单位是千卡</span>
            <span>蛋白质、脂肪、碳水：单位是克</span>
          </div>

          <div v-if="!items.length" class="empty-hint">
            还没有候选食物。你可以先上传图片自动识别，也可以手动补录。
          </div>

          <div v-else class="candidate-list">
            <div class="candidate-card" v-for="(item, index) in items" :key="index">
              <div class="candidate-top">
                <div class="candidate-name-row">
                  <el-input v-model="item.foodName" placeholder="食物名称，例如：鸡胸肉沙拉" />
                  <span v-if="item.confidence !== undefined" class="confidence-chip">
                    置信度 {{ Math.round(item.confidence * 100) }}%
                  </span>
                </div>
                <el-button text type="danger" @click="removeItem(index)">删除</el-button>
              </div>

              <div class="candidate-metrics">
                <div class="metric-box">
                  <span>分量</span>
                  <el-input-number v-model="item.amount" :min="0" />
                </div>
                <div class="metric-box">
                  <span>单位</span>
                  <el-input v-model="item.unit" placeholder="克 / 毫升 / 份" />
                </div>
                <div class="metric-box">
                  <span>热量（千卡）</span>
                  <el-input-number v-model="item.calories" :min="0" />
                </div>
                <div class="metric-box">
                  <span>蛋白质（克）</span>
                  <el-input-number v-model="item.protein" :min="0" />
                </div>
                <div class="metric-box">
                  <span>脂肪（克）</span>
                  <el-input-number v-model="item.fat" :min="0" />
                </div>
                <div class="metric-box">
                  <span>碳水（克）</span>
                  <el-input-number v-model="item.carbs" :min="0" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="submit-row">
        <el-button class="soft-button" @click="handleCreateMeal">保存餐次并生成评估</el-button>
      </div>
    </section>

    <section class="page-card">
      <div class="page-eyebrow">历史餐次</div>
      <div class="history-list">
        <div class="history-card" v-for="meal in meals" :key="meal.id">
          <div class="history-title">{{ mealTypeLabel(meal.mealType) }} · {{ formatDateTime(meal.eatenAt) }}</div>
          <p>{{ meal.description }}</p>
          <p>风险：{{ riskLabel(meal.assessment.riskLevel) }} ｜ 总热量：{{ meal.assessment.totalCalories }} 千卡</p>
          <p>建议：{{ meal.recommendation.recommendationText }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMealsApi, createMealApi } from '@/api/meal'
import { createVisionTaskApi, getVisionTaskApi, type VisionTaskResult } from '@/api/vision'
import { uploadFileApi } from '@/api/file'
import type { MealItem, MealRecord } from '@/types/api'

type EditableMealItem = MealItem & { confidence?: number }

const mealTypeOptions = [
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '加餐', value: 'snack' }
]

const emotionOptions = [
  { label: '平静', value: '平静' },
  { label: '开心', value: '开心' },
  { label: '压力大', value: '压力大' },
  { label: '疲惫', value: '疲惫' },
  { label: '焦虑', value: '焦虑' }
]

const paceOptions = [
  { label: '从容进食', value: '从容进食' },
  { label: '吃得较快', value: '吃得较快' },
  { label: '边做事边吃', value: '边做事边吃' }
]

const fullnessOptions = [
  { label: '七八分饱', value: '七八分饱' },
  { label: '还没太饱', value: '还没太饱' },
  { label: '明显过饱', value: '明显过饱' }
]

const drinkOptions = [
  { label: '白水 / 茶', value: '白水或茶' },
  { label: '无糖饮料', value: '无糖饮料' },
  { label: '含糖饮料', value: '含糖饮料' },
  { label: '没有饮品', value: '没有饮品' }
]

const triggerOptions = [
  { label: '加班', value: '加班' },
  { label: '熬夜', value: '熬夜' },
  { label: '运动后', value: '运动后' },
  { label: '情绪波动', value: '情绪波动' }
]

const riskLabelMap: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险'
}

const meals = ref<MealRecord[]>([])
const items = ref<EditableMealItem[]>([])
const uploading = ref(false)
const uploadedFileName = ref('')
const recognizing = ref(false)
const recognition = reactive({
  status: 'IDLE',
  message: '上传图片后会自动开始识别，也可以手动重新识别。'
})

const form = reactive({
  mealType: 'lunch',
  eatenAt: new Date().toISOString().slice(0, 19),
  description: '',
  imageUrl: '',
  recognitionTaskId: undefined as number | undefined,
  selfRating: 4,
  emotionLabel: '平静'
})

const questionnaire = reactive({
  eatingPace: '从容进食',
  fullness: '七八分饱',
  drinkChoice: '白水或茶',
  triggerTags: [] as string[]
})

const canRecognize = computed(() => Boolean(form.imageUrl || form.description.trim()))

const questionnaireSummary = computed(() => {
  const parts = [
    `进食节奏：${questionnaire.eatingPace}`,
    `饱腹感：${questionnaire.fullness}`,
    `饮品：${questionnaire.drinkChoice}`
  ]
  if (questionnaire.triggerTags.length) {
    parts.push(`触发因素：${questionnaire.triggerTags.join('、')}`)
  }
  return parts.join('；')
})

const recognitionTitle = computed(() => {
  const map: Record<string, string> = {
    IDLE: '等待识别',
    PENDING: '任务已创建',
    PROCESSING: 'AI 正在识别图片',
    SUCCESS: '识别已完成',
    EMPTY: '未识别到可用候选',
    FAILED: '识别失败'
  }
  return map[recognition.status] || '识别状态'
})

const mealTypeLabel = (value: string) => mealTypeOptions.find((item) => item.value === value)?.label || value
const riskLabel = (value: string) => riskLabelMap[value] || value
const formatDateTime = (value: string) => value.replace('T', ' ')

const loadMeals = async () => {
  meals.value = await listMealsApi()
}

const buildRecognitionDescription = () => {
  const parts = [
    form.description.trim(),
    `餐次：${mealTypeLabel(form.mealType)}`,
    `情绪：${form.emotionLabel}`,
    `问卷：${questionnaireSummary.value}`
  ]
  return parts.filter(Boolean).join('；')
}

const buildEmotionText = () => {
  return `${form.emotionLabel}；${questionnaireSummary.value}`
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const applyCandidates = (task: VisionTaskResult) => {
  items.value = task.candidates.map((candidate) => ({
    foodName: candidate.foodName,
    amount: candidate.amount,
    unit: candidate.unit,
    calories: candidate.calories,
    protein: candidate.protein,
    fat: candidate.fat,
    carbs: candidate.carbs,
    source: 'VISION',
    confirmed: true,
    confidence: candidate.confidence
  }))
}

const pollRecognitionTask = async (taskId: number) => {
  for (let attempt = 0; attempt < 20; attempt += 1) {
    const task = await getVisionTaskApi(taskId)
    recognition.status = task.status

    if (task.status === 'SUCCESS') {
      applyCandidates(task)
      recognition.message = task.errorMessage || `已生成 ${task.candidates.length} 个候选，请逐项核对。`
      if (task.errorMessage) {
        ElMessage.warning(task.errorMessage)
      } else {
        ElMessage.success('识别完成，请确认候选食物信息')
      }
      return
    }

    if (task.status === 'EMPTY') {
      items.value = []
      recognition.message = task.errorMessage || '没有识别到候选食物，请手动补录。'
      ElMessage.warning(recognition.message)
      return
    }

    if (task.status === 'FAILED') {
      throw new Error(task.errorMessage || '识别失败，请稍后重试')
    }

    recognition.message = task.status === 'PROCESSING' ? '模型正在分析图片与描述，请稍候。' : '任务已进入队列。'
    await sleep(1200)
  }

  throw new Error('识别超时，请稍后重试')
}

const startRecognition = async (fromUpload: boolean) => {
  if (!canRecognize.value) {
    ElMessage.warning('请先上传图片或填写饮食描述')
    return
  }

  recognizing.value = true
  recognition.status = 'PENDING'
  recognition.message = fromUpload ? '图片上传成功，正在创建识别任务。' : '正在重新识别，请稍候。'

  try {
    const task = await createVisionTaskApi({
      imageUrl: form.imageUrl || undefined,
      description: buildRecognitionDescription()
    })
    form.recognitionTaskId = task.taskId
    await pollRecognitionTask(task.taskId)
  } catch (error: any) {
    recognition.status = 'FAILED'
    recognition.message = error?.message || '识别失败，请稍后重试'
    ElMessage.error(recognition.message)
  } finally {
    recognizing.value = false
  }
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const uploaded = await uploadFileApi(file)
    uploadedFileName.value = file.name
    form.imageUrl = uploaded.url
    ElMessage.success('图片上传成功，开始自动识别')
    await startRecognition(true)
  } finally {
    uploading.value = false
  }
}

const addManualItem = () => {
  items.value.push({
    foodName: '',
    amount: 1,
    unit: '份',
    calories: 0,
    protein: 0,
    fat: 0,
    carbs: 0,
    source: 'MANUAL',
    confirmed: true
  })
}

const removeItem = (index: number) => {
  items.value.splice(index, 1)
}

const handleCreateMeal = async () => {
  if (!items.value.length) {
    ElMessage.warning('请先确认至少一种食物')
    return
  }

  await createMealApi({
    mealType: form.mealType,
    eatenAt: form.eatenAt,
    description: form.description,
    imageUrl: form.imageUrl,
    recognitionTaskId: form.recognitionTaskId,
    selfRating: form.selfRating,
    emotionText: buildEmotionText(),
    items: items.value.map(({ confidence, ...item }) => item)
  })

  ElMessage.success('餐次已保存并生成评估')
  items.value = []
  form.imageUrl = ''
  form.recognitionTaskId = undefined
  uploadedFileName.value = ''
  recognition.status = 'IDLE'
  recognition.message = '本餐已保存。你可以继续上传下一张图片。'
  await loadMeals()
}

onMounted(loadMeals)
</script>

<style scoped>
.meal-page {
  gap: 24px;
}

.meal-card {
  padding: 28px;
}

.meal-grid {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 24px;
}

.block-title {
  font-size: 14px;
  letter-spacing: 0.18em;
  color: var(--color-primary-deep);
  text-transform: uppercase;
  margin-bottom: 16px;
}

.block-title--sub {
  margin-top: 6px;
}

.questionnaire-card,
.recognition-panel {
  padding: 22px;
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(248, 251, 245, 0.92), rgba(243, 246, 239, 0.9));
  border: 1px solid rgba(125, 164, 130, 0.12);
}

.questionnaire-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.questionnaire-label {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--color-text-sub);
  font-size: 13px;
}

.questionnaire-summary {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  color: var(--color-primary-deep);
  line-height: 1.7;
}

.recognition-summary {
  margin-bottom: 16px;
  line-height: 1.7;
  color: var(--color-text-sub);
}

.recognition-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.recognition-status {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(125, 164, 130, 0.1);
}

.recognition-status strong {
  font-size: 14px;
}

.recognition-status span {
  color: var(--color-text-sub);
  line-height: 1.6;
}

.recognition-status--success {
  border-color: rgba(125, 164, 130, 0.22);
}

.recognition-status--failed {
  border-color: rgba(214, 109, 92, 0.26);
}

.candidate-guide {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.66);
  color: var(--color-primary-deep);
  font-size: 13px;
}

.empty-hint {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--color-text-sub);
  line-height: 1.7;
}

.candidate-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.candidate-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 26px rgba(125, 164, 130, 0.08);
}

.candidate-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.candidate-name-row {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.confidence-chip {
  align-self: flex-start;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(125, 164, 130, 0.12);
  color: var(--color-primary-deep);
  font-size: 12px;
}

.candidate-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-box {
  padding: 12px;
  border-radius: 18px;
  background: rgba(245, 247, 241, 0.9);
}

.metric-box span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-text-sub);
}

.submit-row {
  margin-top: 24px;
}

.upload-success,
.slider-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-sub);
}

.image-preview {
  margin-top: 12px;
  overflow: hidden;
  border-radius: 18px;
  max-width: 260px;
}

.image-preview img {
  display: block;
  width: 100%;
  object-fit: cover;
}

.history-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-card {
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
}

.history-title {
  font-weight: 700;
  margin-bottom: 10px;
}

@media (max-width: 1100px) {
  .meal-grid,
  .questionnaire-grid,
  .candidate-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
