import request from '@/utils/request'

export function getTodoCounts() {
  return request.get('/todo/counts')
}
