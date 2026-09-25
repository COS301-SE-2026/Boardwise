export type GameArchitectMode = 'scale' | 'create'
export type ScaleDirection = 'up' | 'down'
export type GameDifficulty = 'easier' | 'same' | 'harder'
export type PlayerRange = '1-2' | '3-4' | '5-6' | '7+'

export interface ScalePreferences {
  direction: ScaleDirection
  difficulty: GameDifficulty
  playerRange: PlayerRange
  targetPlayerCount?: number
}


export interface GameArchitectGame {
  id: string
  title: string
  description?: string
  imageUrl?: string
  genres: string[]
}

export interface GenerateGameRequest {
  mode: GameArchitectMode
  sourceGameIds: string[]
  surpriseMe: boolean
  scalePreferences?: ScalePreferences
}

export interface GeneratedGameConcept {
  id?: string
  title: string
  description: string
  mechanics?: string[]
  components?: string[]
  setup?: string
  rules?: string[]
}

export interface GenerateGameResponse {
  message: string
  jobId?: string
  status?: 'queued' | 'processing' 
  game?: GeneratedGameConcept
}

const GENERATE_GAME_ENDPOINT = 'game-architect/generate'

export const GameArchitectService = {
  generateGame(payload: GenerateGameRequest) {
    const { $fastApi } = useNuxtApp()

    return $fastApi<GenerateGameResponse>(GENERATE_GAME_ENDPOINT, {
      method: 'POST',
      body: payload
    })
  }
}
