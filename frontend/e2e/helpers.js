import { test as base, expect as pwExpect } from '@playwright/test'

const STORAGE_STATE_PATH = '.auth/user.json'
const ADMIN_STATE_PATH = '.auth/admin.json'

export const test = base
export const expect = pwExpect

export const ROUTES = {
  login: '/login',
  home: '/',
  contentCreate: '/content/create',
  contentDetail: (id) => `/content/${id}`,
  contentEdit: (id) => `/content/${id}/edit`,
  groups: '/groups',
  groupDetail: (id) => `/group/${id}`,
  groupManage: (id) => `/group/${id}/manage`,
  favorites: '/favorites',
  notifications: '/notifications',
  templates: '/templates',
  adminTags: '/admin/tags',
  adminUsers: '/admin/users',
  adminSensitiveWords: '/admin/sensitive-words',
  adminAudit: '/admin/audit',
  adminAnalytics: '/admin/analytics',
  adminDepartments: '/admin/departments',
  adminSettings: '/admin/settings',
}

export async function login(page, username = 'admin', password = 'admin123') {
  await page.goto(ROUTES.login)
  await page.fill('input[placeholder="用户名 / 工号"]', username)
  await page.fill('input[placeholder="密码"]', password)
  await page.click('button:has-text("登 录")')
  await page.waitForURL(ROUTES.home, { timeout: 10000 })
}

export async function loginAndGetState(page, username = 'admin', password = 'admin123') {
  await login(page, username, password)
  return await page.context().storageState()
}

export { STORAGE_STATE_PATH, ADMIN_STATE_PATH }
