import request from '@/utils/request'
import type { Profile } from '@/types/api'

export const getProfileApi = () => request.get('/profile') as Promise<Profile>
export const saveProfileApi = (data: Omit<Profile, 'userId' | 'bmi' | 'bmr'>) => request.put('/profile', data) as Promise<Profile>
