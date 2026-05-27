import request from '@/utils/request'

export function getUserList() {
  return request.get('/admin/users')
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUserRole(userId, role) {
  return request.put(`/admin/users/${userId}/role`, { role })
}
