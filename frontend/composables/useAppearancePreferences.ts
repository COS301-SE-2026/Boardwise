import { computed, ref } from 'vue'
import { useTheme } from 'vuetify'

export type BoardwiseTheme = 'light' | 'dark' | 'system'
export type ContentDensity = 'comfortable' | 'compact'
export type GameView = 'grid' | 'list'

interface AppearancePreferences {
  theme: BoardwiseTheme
  density: ContentDensity
  gameView: GameView
  reduceMotion: boolean
}

const STORAGE_KEY = 'boardwise-appearance'

const preferences = ref<AppearancePreferences>({
  theme: 'system',
  density: 'comfortable',
  gameView: 'grid',
  reduceMotion: false
})

const loaded = ref(false)

export const useAppearancePreferences = () => {
  const vuetifyTheme = useTheme()

  const getSystemTheme = (): 'light' | 'dark' => {
    if (!import.meta.client) return 'light'

    return window.matchMedia('(prefers-color-scheme: dark)').matches
      ? 'dark'
      : 'light'
  }

  const resolvedTheme = computed(() => {
    if (preferences.value.theme === 'system') {
      return getSystemTheme()
    }

    return preferences.value.theme
  })

  const applyPreferences = () => {
    const themeName =
      resolvedTheme.value === 'dark'
        ? 'boardwiseDark'
        : 'boardwise'

    vuetifyTheme.global.name.value = themeName

    if (!import.meta.client) return

    document.documentElement.dataset.density =
      preferences.value.density

    document.documentElement.dataset.gameView =
      preferences.value.gameView

    document.documentElement.classList.toggle(
      'reduce-motion',
      preferences.value.reduceMotion
    )
  }

  const loadPreferences = () => {
    if (!import.meta.client || loaded.value) return

    const stored = localStorage.getItem(STORAGE_KEY)

    if (stored) {
      try {
        preferences.value = {
          ...preferences.value,
          ...JSON.parse(stored)
        }
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }

    loaded.value = true
    applyPreferences()
  }

  const savePreferences = (
    nextPreferences: AppearancePreferences
  ) => {
    preferences.value = {
      ...nextPreferences
    }

    if (import.meta.client) {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(preferences.value)
      )
    }

    applyPreferences()
  }

  const resetPreferences = () => {
    preferences.value = {
      theme: 'system',
      density: 'comfortable',
      gameView: 'grid',
      reduceMotion: false
    }

    if (import.meta.client) {
      localStorage.removeItem(STORAGE_KEY)
    }

    applyPreferences()
  }

  return {
    preferences,
    resolvedTheme,
    loadPreferences,
    savePreferences,
    resetPreferences
  }
}