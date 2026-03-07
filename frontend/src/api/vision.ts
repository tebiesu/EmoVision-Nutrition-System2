import request from '@/utils/request'
import type { VisionCandidate } from '@/types/api'

export const createVisionTaskApi = (data: { imageUrl?: string; description: string }) =>
  request.post('/vision/tasks', data) as Promise<{ taskId: number; status: string; errorMessage?: string; candidates: VisionCandidate[] }>

export const getVisionTaskApi = (taskId: number) =>
  request.get(`/vision/tasks/${taskId}`) as Promise<{ taskId: number; status: string; errorMessage?: string; candidates: VisionCandidate[] }>
