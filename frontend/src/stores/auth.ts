import { defineStore } from 'pinia'

type UserInfo = {
  id?: number
  username?: string
  nickname?: string
  roleCode?: string
}

const TOKEN_KEY = 'health-assistant-token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: {} as UserInfo
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
    },
    clearAuth() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
