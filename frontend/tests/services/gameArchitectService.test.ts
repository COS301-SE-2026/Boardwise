import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mockNuxtImport } from '@nuxt/test-utils/runtime'

const fastApiMock = vi.fn()

mockNuxtImport('useNuxtApp', () => {
  return () => ({ $fastApi: fastApiMock })
})

const { GameArchitectService } = await import('~/services/gameArchitectService')

describe('GameArchitectService', () => {
  beforeEach(() => {
    fastApiMock.mockReset()
  })

  it('sends the approved game architect payload to the AI gateway', async () => {
    const payload = {
      mode: 'create' as const,
      sourceGameIds: ['game-1', 'game-2'],
      prompt: 'Create a cooperative family mystery game.',
      surpriseMe: false
    }

    fastApiMock.mockResolvedValue({
      message: 'Game generation queued',
      jobId: 'job-1',
      status: 'queued'
    })

    await GameArchitectService.generateGame(payload)

    expect(fastApiMock).toHaveBeenCalledWith('game-architect/generate', {
      method: 'POST',
      body: payload
    })
  })
})
