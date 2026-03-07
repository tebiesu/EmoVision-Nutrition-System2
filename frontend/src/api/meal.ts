import request from '@/utils/request'
import type { MealRecord } from '@/types/api'

export const listMealsApi = () => request.get('/meals') as Promise<MealRecord[]>
export const createMealApi = (data: Record<string, unknown>) => request.post('/meals', data) as Promise<MealRecord>
