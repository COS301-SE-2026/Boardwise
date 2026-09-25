import { ref } from 'vue'
import { BoardGameService, type BoardGame, type OnboardingDTO, type OtherGameInfo } from '~/services/boardgameService'

export const useBoardGames = () => {
    const games = ref<BoardGame[]>([])
    const genres = ref<string[]>([])
    const isLoading = ref<boolean>(false)
    const error = ref<string>('')
    const topNgenres = ref<string[]>([])
    const topUserGames = ref<OnboardingDTO[]>([]);
    const topGamesInDBBasedOnGenres = ref<OnboardingDTO[]>([]);
    
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

    const getTopNGenresFromUsersPreferences = async (n: number) =>{
        isLoading.value = true;
        try{
            const res = await BoardGameService.getTopNGenresFromUsersPreferences(n);
            topNgenres.value = res;
            return topNgenres
        }
        catch(err: any){
            error.value = err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const getPopularBoardgamesFromUserPrefrences = async () =>{
        isLoading.value = true;
        error.value = '';
        try{    
            topUserGames.value = await BoardGameService.getPopularBoardgamesFromUserPreferences();
        }
        catch(err: any){
            error.value = err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const getPopularGamesBasedOnGenres = async(genres :string[]) =>{// 
        isLoading.value = true;
        try{
            topGamesInDBBasedOnGenres.value = await BoardGameService.getPopularGamesBasedOnGenres(genres);
            console.log("Most popular games in db based on genres: ", topGamesInDBBasedOnGenres.value);

        }
        catch(err: any){
            error.value = err;
        }
        finally{
            isLoading.value = false;
        }
    }

    return {
        games,
        genres,
        isLoading,
        error,
        searchGames,
        searchGenres,
        addBoardgame,
        getTopNGenresFromUsersPreferences,
        topNgenres,
        getPopularBoardgamesFromUserPrefrences,
        topUserGames,// returns 8 games as requested
        getPopularGamesBasedOnGenres,
        topGamesInDBBasedOnGenres
    }
}