import request from '@/utils/request'

export const loginApi = (data: { username: string; password: string }) => request.post('/auth/login', data)
export const registerApi = (data: { username: string; password: string; nickname?: string }) => request.post('/auth/register', data)
export const getCurrentUserApi = () => request.get('/auth/me')
