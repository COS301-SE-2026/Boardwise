import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const apiMock = vi.fn()

mockNuxtImport('useNuxtApp', () => {
  return () => ({ $api: apiMock })
})

const { AuthService } = await import('~/services/authService')

describe('AuthService', () => {
  beforeEach(() => {
    apiMock.mockReset()
  })

  it('register calls $api with correct endpoint and body', async () => {
    apiMock.mockResolvedValue({ message: 'ok', accessToken: 'token123' })

    const result = await AuthService.register({ email: 'testuser@up.com', password: '123!@#TE$T' })

    expect(apiMock).toHaveBeenCalledWith('auth/register', {
      method: 'POST',
      body: { email: 'testuser@up.com', password: '123!@#TE$T' },
    })
    expect(result).toEqual({ message: 'ok', accessToken: 'token123' })
  })

  it('login calls $api with correct endpoint and body', async () => {
    apiMock.mockResolvedValue({ message: 'ok', accessToken: 'token456' })

    const result = await AuthService.login({ email: 'testuser@up.com', password: '123!@#TE$T' })

    expect(apiMock).toHaveBeenCalledWith('auth/login', {
      method: 'POST',
      body: { email: 'testuser@up.com', password: '123!@#TE$T' },
    })
    expect(result.accessToken).toBe('token456')
  })

  it('logout calls $api with DELETE method', async () => {
    apiMock.mockResolvedValue({})

    await AuthService.logout()

    expect(apiMock).toHaveBeenCalledWith('auth/logout', {
      method: 'DELETE',
    })
  })
})