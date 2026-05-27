import request from '@/utils/request'

let groupListCache = null
let groupListCacheTime = 0
const GROUP_LIST_CACHE_TTL = 3 * 60 * 1000 // 3分钟缓存

export function getGroupList(params) {
  // 只有无参数或默认参数时才使用缓存
  const isDefaultParams = !params || Object.keys(params).length === 0 || (params.size === 100 && Object.keys(params).length === 1)
  const now = Date.now()
  if (isDefaultParams && groupListCache && now - groupListCacheTime < GROUP_LIST_CACHE_TTL) {
    return Promise.resolve({ data: groupListCache })
  }
  return request.get('/groups', { params }).then(res => {
    if (isDefaultParams) {
      groupListCache = res.data
      groupListCacheTime = now
    }
    return res
  })
}

export function clearGroupListCache() {
  groupListCache = null
  groupListCacheTime = 0
}

export function getGroup(id) {
  return request.get(`/groups/${id}`)
}

export function createGroup(data) {
  return request.post('/groups', data)
}

export function joinGroup(id) {
  return request.post(`/groups/${id}/join`)
}

export function getGroupMembers(id) {
  return request.get(`/groups/${id}/members`)
}

export function getPendingMembers(id) {
  return request.get(`/groups/${id}/members/pending`)
}

export function approveMember(groupId, userId, action) {
  return request.put(`/groups/${groupId}/members/${userId}`, { action })
}

export function removeMember(groupId, userId) {
  return request.delete(`/groups/${groupId}/members/${userId}`)
}
