import request from '@/utils/request'

export const getTrendApi = () => request.get('/analysis/trends') as Promise<any>
