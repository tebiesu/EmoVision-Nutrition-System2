export type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
  traceId: string
}

export type UserInfo = {
  id: number
  username: string
  nickname?: string
  roleCode: string
}

export type Profile = {
  userId: number
  age: number
  gender: string
  heightCm: number
  weightKg: number
  activityLevel: string
  goal: string
  allergies?: string
  tabooFoods?: string
  medicalConditions?: string
  bmi: number
  bmr: number
}

export type VisionCandidate = {
  id?: number
  foodName: string
  confidence: number
  amount: number
  unit: string
  calories: number
  protein: number
  fat: number
  carbs: number
}

export type MealItem = {
  foodName: string
  amount: number
  unit: string
  calories: number
  protein: number
  fat: number
  carbs: number
  source: string
  confirmed: boolean
}

export type MealRecord = {
  id: number
  mealType: string
  eatenAt: string
  date: string
  description: string
  imageUrl?: string
  recognitionTaskId?: number
  items: MealItem[]
  emotion: {
    selfRating: number
    textContent: string
    sentimentLabel: string
    sentimentScore: number
  }
  assessment: {
    totalCalories: number
    totalProtein: number
    totalFat: number
    totalCarbs: number
    structureScore: number
    riskLevel: string
    riskTags: string[]
    evidenceText: string
  }
  recommendation: {
    aiEnhanced: boolean
    fallbackMode: boolean
    summaryText: string
    recommendationText: string
  }
}
