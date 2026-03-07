import request from '@/utils/request'

export const getDashboardSummaryApi = () => request.get('/dashboard/summary') as Promise<any>
