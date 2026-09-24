export interface BoardGame{
    id: string
    title: string
    description?: string
    imageUrl?: string 
    genres?: string[]
}

export interface GetBoardGamesResponse{
    message:string
    boardGames: BoardGame[]
}

export interface GetGenresResponse{
    genres: never[]
    message: string
    result: string[]
}

export interface OtherGameInfo{
    title: string
    description: string
    genres: string[]
}

export interface OnboardingDTO{
    id: string
    title: string,
    imageUrl:string
}

export interface AddBoardGameResponse{
    message: string
    data: BoardGame
}

export const BoardGameService = {
    // GET /api/boardgames/?query=

    getBoardgames(query?: string){
        const { $api } = useNuxtApp();
        return $api<GetBoardGamesResponse>('boardgames/',{
            method: 'GET',
            query: query? {query} :{}
        });
    },

    // GET /api/boardgames/genres?query=
    getGenres(query?: string) {
        const { $api } = useNuxtApp()
        return $api<GetGenresResponse>('boardgames/genres', {
            method: 'GET',
            query: query ? { query } : {}
        })
    },

    // POST /api/boardgames/
    addBoardgame(gameInfo: OtherGameInfo, image: File) {
        const { $api } = useNuxtApp()
        const formData = new FormData()
        formData.append('gameInfo', new Blob([JSON.stringify(gameInfo)], {
            type: 'application/json'
        }))
        formData.append('gameImage', image)
        return $api<AddBoardGameResponse>('boardgames/', {
            method: 'POST',
            body: formData
        })
    },

    // GET /api/sb/boardgames/genres/top?top=n
    getTopNGenresFromUsersPreferences(top?:number){
        const { $api } = useNuxtApp();
        return $api<string[]>('boardgames/genres/top',{
            method: 'GET',
            query: top ? { top } : {}
        })
    },

    getPopularBoardgamesFromUserPreferences(){
        const { $api } = useNuxtApp();
        return $api<OnboardingDTO[]>('boardgames/popular/user/games');
    },

    getPopularGamesBasedOnGenres(genres: string[] ){
        const { $api } = useNuxtApp();
        return $api<OnboardingDTO[]>('boardgames/popular/games/genre',{
            method: 'POST',
            body: genres? {genres} : {}
        });
    }
    
}
