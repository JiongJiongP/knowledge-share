import request from '@/utils/request'

export function getTags() {
  return request.get('/tags')
}

export function getContentTags(contentId) {
  return request.get(`/contents/${contentId}/tags`)
}

export function createTag(data) {
  return request.post('/admin/tags', data)
}

export function updateTag(id, data) {
  return request.put(`/admin/tags/${id}`, data)
}

export function deleteTag(id) {
  return request.delete(`/admin/tags/${id}`)
}

export function setContentTags(contentId, tagIds) {
  return request.put(`/contents/${contentId}/tags`, { tagIds })
}
