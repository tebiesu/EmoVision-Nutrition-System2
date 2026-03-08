import request from '@/utils/request'
import type { VisionCandidate } from '@/types/api'

export type VisionTaskResult = {
  taskId: number
  status: 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'EMPTY' | 'FAILED'
  errorMessage?: string
  createdAt?: string
  finishedAt?: string
  candidates: VisionCandidate[]
}

export const createVisionTaskApi = (data: { imageUrl?: string; description?: string }) =>
  request.post('/vision/tasks', data) as Promise<VisionTaskResult>

export const getVisionTaskApi = (taskId: number) =>
  request.get(`/vision/tasks/${taskId}`) as Promise<VisionTaskResult>
