import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request.js'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('youda_token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)

  function login(loginToken, info) {
    token.value = loginToken
    userInfo.value = info
    localStorage.setItem('youda_token', loginToken)
    localStorage.setItem('youda_userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('youda_token')
    localStorage.removeItem('youda_userInfo')
  }

  async function fetchUserInfo() {
    try {
      const data = await request.get('/user/info')
      userInfo.value = data
      localStorage.setItem('youda_userInfo', JSON.stringify(data))
      return data
    } catch (error) {
      logout()
      throw error
    }
  }

  function init() {
    const savedInfo = localStorage.getItem('youda_userInfo')
    if (savedInfo && token.value) {
      try {
        userInfo.value = JSON.parse(savedInfo)
      } catch {
        logout()
      }
    }
  }

  init()

  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    login,
    logout,
    fetchUserInfo
  }
})
