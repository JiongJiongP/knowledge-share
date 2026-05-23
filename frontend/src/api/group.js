import request from '@/utils/request'

export function getGroupList(params) {
  return request.get('/groups', { params })
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
