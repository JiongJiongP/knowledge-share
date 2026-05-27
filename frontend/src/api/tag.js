import request from '@/utils/request'

let tagsCache = null
let tagsCacheTime = 0
const TAGS_CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

export function getTags() {
  const now = Date.now()
  if (tagsCache && now - tagsCacheTime < TAGS_CACHE_TTL) {
    return Promise.resolve({ data: tagsCache })
  }
  return request.get('/tags').then(res => {
    tagsCache = res.data
    tagsCacheTime = now
    return res
  })
}

export function clearTagsCache() {
  tagsCache = null
  tagsCacheTime = 0
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
