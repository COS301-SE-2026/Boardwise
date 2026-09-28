import { computed, ref } from 'vue'

import {
  GameArchitectService,
  type GameArchitectGame,
  type GameArchitectMode,
  type GameDifficulty,
  type PlayerRange,

  type GenerateGameResponse
} from '~/services/gameArchitectService'
import { userService } from '~/services/userService'

const MAX_SELECTED_GAMES = 2
const ACTIVE_JOB_STORAGE_KEY =
  'boardwise:game-architect-active-job'

const normaliseGame = (game: any): GameArchitectGame => {
  let genres: string[] = []
    
  if (Array.isArray(game.genres)) {
    genres = game.genres
  } else if (game.genre) {
    genres = [game.genre]
  }

  return {
    id: String(game.id),
    title: game.title ?? 'Untitled game',
    description: game.description ?? '',
    imageUrl:
      game.imageUrl ??
      game.imageURL ??
      '/images/default-listing.png',
    genres
  }
}

const getErrorMessage = (error: unknown): string | undefined => {
  if (typeof error === 'object' && error !== null) {
    const candidate = error as {
      data?: { message?: string }
      message?: string
    }

    return candidate.data?.message || candidate.message
  }

  return undefined
}

export const useGameArchitect = () => {
  const step = ref(1)
  const mode = ref<GameArchitectMode | null>(null)
  const games = ref<GameArchitectGame[]>([])
  const selectedGames = ref<GameArchitectGame[]>([])
  const playerRange = ref<PlayerRange | null>(null)
  const difficulty = ref<GameDifficulty | null>(null)
  const surpriseMe = ref(false)
  const loadingGames = ref(false)
  const generating = ref(false)
  const error = ref('')
  const result = ref<GenerateGameResponse | null>(null)
  const generationInProgress = ref(false)
  const activeJobId = ref<string | null>(null)

  const isScaleMode = computed(() => mode.value === 'scale')

  const canContinue = computed(() => {
    if (step.value === 1) {
      return mode.value !== null && !generationInProgress.value
    }
    if (step.value === 2) {
      if (isScaleMode.value) {
        return selectedGames.value.length === 1
      }
    return (
        surpriseMe.value ||
        (
          selectedGames.value.length > 0 &&
          selectedGames.value.length <=
            MAX_SELECTED_GAMES
        )
      )
    }

    if (step.value === 3) {
      if (!isScaleMode.value) {
        return true
      }

      return (
        difficulty.value !== null &&
        playerRange.value !== null
      )
    }

    return false
  })

  const loadOwnedGames = async () => {
    loadingGames.value = true
    error.value = ''

    try {
      const profile = await userService.getCurrentUser()
      games.value = (profile.games ?? []).map(normaliseGame)
    } catch (err: unknown) {
      games.value = []
      error.value = getErrorMessage(err) || 'Could not load your game collection.'
    } finally {
      loadingGames.value = false
    }
  }

  const chooseMode = (nextMode: GameArchitectMode) => {
    if (generationInProgress.value) return

    if (mode.value !== nextMode) {
      selectedGames.value = []
      surpriseMe.value = false
      difficulty.value = null
      playerRange.value = null
    }

    mode.value = nextMode
  }

  const loadActiveGeneration = () => {
  if (!import.meta.client) return

  const storedJob = localStorage.getItem(ACTIVE_JOB_STORAGE_KEY)
  if (!storedJob) return

  try {
    const job = JSON.parse(storedJob) as {
      jobId?: string
      status?: string
    }

    if (job.status === 'queued' || job.status === 'processing') {
      generationInProgress.value = true
      activeJobId.value = job.jobId ?? null
      return
    }

    localStorage.removeItem(ACTIVE_JOB_STORAGE_KEY)
  } catch {
    localStorage.removeItem(ACTIVE_JOB_STORAGE_KEY)
  }
}
  const isSelected = (gameId: string) =>
    selectedGames.value.some(game => game.id === gameId)

  const toggleGame = (game: GameArchitectGame) => {
    surpriseMe.value = false

    if (isSelected(game.id)) {
      selectedGames.value = selectedGames.value.filter(
        selected => selected.id !== game.id
      )
      return
    }

    if (isScaleMode.value) {
      selectedGames.value = [game]
      return
    }

    if (selectedGames.value.length < MAX_SELECTED_GAMES) {
      selectedGames.value = [...selectedGames.value, game]
    }
  }

  const chooseSurpriseMe = () => {
    if (isScaleMode.value) return

    surpriseMe.value = true
    selectedGames.value = []
  }

  const next = () => {
    if (!canContinue.value || step.value >= 3) return
    step.value += 1
    error.value = ''
  }

  const back = () => {
    if (step.value <= 1) return
    step.value -= 1
    error.value = ''
  }

  const generate = async () => {
    if (!mode.value || !canContinue.value || generationInProgress.value) return

    generating.value = true
    error.value = ''
    result.value = null

    try {
      result.value = await GameArchitectService.generateGame({
      mode: mode.value,
      sourceGameIds: selectedGames.value.map(game => game.id),
      surpriseMe: surpriseMe.value,
      ...(isScaleMode.value
        ? {
            scalePreferences: {
              difficulty: difficulty.value!,
              playerRange: playerRange.value!
            }
          }
        : {})
    })
    if (
      result.value.status === 'queued' ||
      result.value.status === 'processing'
    ) {
      generationInProgress.value = true
      activeJobId.value = result.value.jobId ?? null

  if (import.meta.client) {
    localStorage.setItem(
      ACTIVE_JOB_STORAGE_KEY,
      JSON.stringify({
        jobId: result.value.jobId,
        status: result.value.status
      })
    )
  }
}

  step.value = 4
} catch (err: unknown) {
  error.value =
    getErrorMessage(err) ||
    'Boarley could not generate your game. Please try again.'
} finally {
  generating.value = false
}}

  const reset = () => {
    step.value = 1
    mode.value = null
    selectedGames.value = []
    playerRange.value = null
    difficulty.value = null
    surpriseMe.value = false
    generating.value = false
    error.value = ''
    result.value = null
  }

  return {
    step,
    mode,
    games,
    selectedGames,
    difficulty,
    playerRange,
    surpriseMe,
    loadingGames,
    generating,
    error,
    result,
    maxSelectedGames: MAX_SELECTED_GAMES,
    isScaleMode,
    canContinue,
    generationInProgress,
    activeJobId,
    loadActiveGeneration,
    loadOwnedGames,
    chooseMode,
    isSelected,
    toggleGame,
    chooseSurpriseMe,
    next,
    back,
    generate,
    reset
  }
}
