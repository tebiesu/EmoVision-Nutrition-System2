import request from '@/utils/request'

export const uploadFileApi = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData) as Promise<{
    fileId: number
    url: string
    contentType: string
    sizeBytes: number
  }>
}
