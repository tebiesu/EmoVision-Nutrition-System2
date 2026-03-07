import request from '@/utils/request'
import type { UserInfo } from '@/types/api'

export type LoginPayload = { username: string; password: string }
export type LoginResult = {
  accessToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export const loginApi = (data: LoginPayload) => request.post('/auth/login', data) as Promise<LoginResult>
export const registerApi = (data: { username: string; password: string; nickname?: string; phone?: string; email?: string }) => request.post('/auth/register', data) as Promise<{ userId: number }>
export const getCurrentUserApi = () => request.get('/auth/me') as Promise<UserInfo>
export const logoutApi = () => request.post('/auth/logout') as Promise<void>
