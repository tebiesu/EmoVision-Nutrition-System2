// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/api/auth', () => ({
  loginApi: vi.fn(async () => ({
    accessToken: 'token-1',
    tokenType: 'Bearer',
    expiresIn: 7200,
    userInfo: { id: 1, username: 'demo', roleCode: 'USER', nickname: 'Demo' }
  })),
  getCurrentUserApi: vi.fn(async () => ({ id: 1, username: 'demo', roleCode: 'USER', nickname: 'Demo' }))
}))

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('stores token and user info after login', async () => {
    const store = useAuthStore()
    await store.login({ username: 'demo', password: '123456' })

    expect(store.token).toBe('token-1')
    expect(store.userInfo.username).toBe('demo')
  })

  it('clears auth state', () => {
    const store = useAuthStore()
    store.setToken('token-1')
    store.setUserInfo({ id: 1, username: 'demo', roleCode: 'USER' })

    store.clearAuth()

    expect(store.token).toBe('')
    expect(store.userInfo.username).toBeUndefined()
  })
})
