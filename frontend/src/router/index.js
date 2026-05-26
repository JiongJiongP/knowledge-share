import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/content/create',
    name: 'ContentCreate',
    component: () => import('@/views/ContentCreate.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/content/:id',
    name: 'ContentDetail',
    component: () => import('@/views/ContentDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/content/:id/edit',
    name: 'ContentEdit',
    component: () => import('@/views/ContentEdit.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/groups',
    name: 'Groups',
    component: () => import('@/views/GroupList.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/group/:id',
    name: 'GroupDetail',
    component: () => import('@/views/GroupDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/group/:id/manage',
    name: 'GroupManage',
    component: () => import('@/views/GroupManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/tags',
    name: 'AdminTags',
    component: () => import('@/views/admin/TagManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/sensitive-words',
    name: 'AdminSensitiveWords',
    component: () => import('@/views/admin/SensitiveWordManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/analytics',
    name: 'AdminAnalytics',
    component: () => import('@/views/admin/AnalyticsDashboard.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/audit',
    name: 'AdminAudit',
    component: () => import('@/views/admin/AuditCenter.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/audit',
    name: 'AuditCenter',
    redirect: '/admin/audit',
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/FavoritesPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/NotificationsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/templates',
    name: 'Templates',
    component: () => import('@/views/TemplatesPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/departments',
    name: 'AdminDepartments',
    component: () => import('@/views/admin/DepartmentManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/settings',
    name: 'AdminSettings',
    component: () => import('@/views/admin/SystemSettings.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
