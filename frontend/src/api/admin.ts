import request from '@/utils/request'

export const getAdminOverviewApi = () => request.get('/admin/overview') as Promise<any>
export const getAuditLogsApi = () => request.get('/admin/audit-logs') as Promise<any[]>
