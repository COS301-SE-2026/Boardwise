import { computed, ref } from 'vue'

import {
  GameArchitectService,
  type GameArchitectGame,
  type GameArchitectMode,
  type GameDifficulty,
  type ScaleDirection,
  type GenerateGameResponse
} from '~/services/gameArchitectService'
import { userService } from '~/services/userService'

const MAX_SELECTED_GAMES = 3

const normaliseGame = (game: any): GameArchitectGame => ({
  id: String(game.id),
  title: game.title ?? 'Untitled game',
  description: game.description ?? '',
  imageUrl: game.imageUrl ?? game.imageURL ?? '/images/default-listing.png',
  genres: game.genres ?? game.genre ?? []
})

const getErrorMessage = (error: unknown) => {
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
  const prompt = ref('')
  const scaleDirection = ref<ScaleDirection | null>(null)
  const difficulty = ref<GameDifficulty | null>(null)
  const targetPlayerCount = ref<number | null>(null)
  const surpriseMe = ref(false)
  const loadingGames = ref(false)
  const generating = ref(false)
  const error = ref('')
  const result = ref<GenerateGameResponse | null>(null)

  const isScaleMode = computed(() => mode.value === 'scale')

  const canContinue = computed(() => {
    if (step.value === 1) return mode.value !== null

    if (step.value === 2) {
      return surpriseMe.value || selectedGames.value.length > 0
    }

    if (step.value === 3) {
      if (isScaleMode.value) {
        return scaleDirection.value !== null && difficulty.value !== null
      }

      return prompt.value.trim().length >= 10
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
    if (mode.value !== nextMode) {
      selectedGames.value = []
      surpriseMe.value = false
      scaleDirection.value = null
      difficulty.value = null
      targetPlayerCount.value = null
      prompt.value = ''
    }

    mode.value = nextMode
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
    if (!mode.value || !canContinue.value) return

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
                direction: scaleDirection.value!,
                difficulty: difficulty.value!,
                ...(targetPlayerCount.value !== null
                  ? { targetPlayerCount: targetPlayerCount.value }
                  : {})
              }
            }
          : { blueprintNotes: prompt.value.trim() 
      })
    })
      step.value = 4
    } catch (err: unknown) {
      error.value = getErrorMessage(err) || 'Boarley could not generate your game. Please try again.'
    } finally {
      generating.value = false
    }
  }

  const reset = () => {
    step.value = 1
    mode.value = null
    selectedGames.value = []
    prompt.value = ''
    scaleDirection.value = null
    difficulty.value = null
    targetPlayerCount.value = null
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
    prompt,
    scaleDirection,
    difficulty,
    targetPlayerCount,
    surpriseMe,
    loadingGames,
    generating,
    error,
    result,
    maxSelectedGames: MAX_SELECTED_GAMES,
    isScaleMode,
    canContinue,
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
