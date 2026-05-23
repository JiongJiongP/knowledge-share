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
