import request from '@/utils/request'

export function getContentList(params) {
  return request.get('/contents', { params })
}

export function getContent(id) {
  return request.get(`/contents/${id}`)
}

export function createContent(data) {
  return request.post('/contents', data)
}

export function updateContent(id, data) {
  return request.put(`/contents/${id}`, data)
}

export function publishContent(id) {
  return request.post(`/contents/${id}/publish`)
}

export function submitAudit(id) {
  return request.post(`/contents/${id}/submit-audit`)
}

export function saveDraft(id, data) {
  return request.post(`/contents/${id}/draft`, data)
}

export function deleteContent(id) {
  return request.delete(`/contents/${id}`)
}

export function schedulePublish(id, scheduledAt) {
  return request.post(`/contents/${id}/schedule`, { scheduledAt })
}
