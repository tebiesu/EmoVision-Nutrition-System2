import request from '@/utils/request'

export type NutritionChatPayload = {
  message: string
  includeProfile: boolean
  includeRecentMeals: boolean
  includeLatestAssessment: boolean
}

export type NutritionChatResult = {
  usedAi: boolean
  reply: string
  context: string
  sources: string[]
  error?: string
}

export const nutritionChatApi = (payload: NutritionChatPayload) =>
  request.post('/assistant/nutrition-chat', payload) as Promise<NutritionChatResult>
