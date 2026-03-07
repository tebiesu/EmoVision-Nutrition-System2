import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi } from '@/api/auth'
import type { UserInfo } from '@/types/api'

const TOKEN_KEY = 'health-assistant-token'
const USER_KEY = 'health-assistant-user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: (JSON.parse(localStorage.getItem(USER_KEY) || '{}') as UserInfo)
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.userInfo?.roleCode === 'ADMIN'
  },
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
    },
    async login(payload: { username: string; password: string }) {
      const response = await loginApi(payload)
      this.setToken(response.accessToken)
      this.setUserInfo(response.userInfo)
      return response
    },
    async hydrateUser() {
      if (!this.token) return
      if (this.userInfo?.id) return
      const user = await getCurrentUserApi()
      this.setUserInfo(user)
    },
    clearAuth() {
      this.token = ''
      this.userInfo = {} as UserInfo
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
