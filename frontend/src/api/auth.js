import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function getCurrentUser() {
  return request.get('/auth/me')
}

export function ssoLogin() {
  return request.get('/auth/sso/redirect')
}

export function ldapLogin() {
  return request.get('/auth/ldap/redirect')
}
