import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 20000
})

request.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

request.interceptors.response.use(
  ((response: { data: ApiEnvelope<unknown> }) => {
    const payload = response.data
    if (payload.code !== 0) {
      ElMessage.error(payload.message)
      return Promise.reject(new Error(payload.message))
    }
    return payload.data
  }) as never,
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '请求失败，请稍后重试')
    if (error.response?.status === 401) {
      useAuthStore().clearAuth()
    }
    return Promise.reject(error)
  }
)

export default request
