import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
} from '@/utils/auth'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const info = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => info.value?.roles?.includes('ADMIN'))

  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    setToken(res.data.token)
    const userRes = await getCurrentUser()
    info.value = userRes.data
    setUser(userRes.data)
  }

  function logout() {
    token.value = null
    info.value = null
    removeToken()
    window.location.href = '/login'
  }

  return { token, info, isLoggedIn, isAdmin, login, logout }
})
