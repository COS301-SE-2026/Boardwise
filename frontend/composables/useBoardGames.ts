import { ref } from 'vue'
import { BoardGameService, type BoardGame, type OtherGameInfo } from '~/services/boardgameService'

export const useBoardGames = () => {
    const games = ref<BoardGame[]>([])
    const genres = ref<string[]>([])
    const isLoading = ref<boolean>(false)
    const error = ref<string>('')

    const searchGames = async (query?: string) => {
        isLoading.value = true
        error.value = ''

        try {
            const data = await BoardGameService.getBoardgames(query)
            games.value = data.boardGames
        } catch (err: any) {
            error.value = err.data?.message || 'Failed to load games'
            games.value = []
        } finally {
            isLoading.value = false
        }
    }

    const searchGenres = async (query?: string) => {
        error.value = ''

        try {
            const data = await BoardGameService.getGenres(query)
            console.log('genres API response:', data) // ← add this

            genres.value = data.genres??[]
        } catch (err: any) {
            error.value = err.data?.message || 'Failed to load genres'
            genres.value = []
        }
    }

    const addBoardgame = async (gameInfo: OtherGameInfo, image: File) => {
        error.value = ''

        try {
            const data = await BoardGameService.addBoardgame(gameInfo, image)
            games.value.unshift(data.data)
            return data.data
        } catch (err: any) {
            error.value = err.data?.message || 'Failed to add game'
            throw err
        }
    }

    return {
        games,
        genres,
        isLoading,
        error,
        searchGames,
        searchGenres,
        addBoardgame
    }
}