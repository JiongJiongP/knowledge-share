import request from '@/utils/request'

export function getStatsOverview() {
  return request.get('/stats/overview')
}
