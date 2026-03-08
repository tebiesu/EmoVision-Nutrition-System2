import request from '@/utils/request'

export type AdminAiConfig = {
  enabled: boolean
  channelName: string
  baseUrl: string
  hasApiKey: boolean
  maskedApiKey: string
  chatModel: string
  visionModel: string
  timeoutSeconds: number
}

export const getAdminOverviewApi = () => request.get('/admin/overview') as Promise<any>
export const getAuditLogsApi = () => request.get('/admin/audit-logs') as Promise<any[]>
export const getAiConfigApi = () => request.get('/admin/ai-config') as Promise<AdminAiConfig>
