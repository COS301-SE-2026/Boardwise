export interface BoardGame{
    id: string
    title: string
    description: string
    imageUrl: string 
    genres: string[]
}

export interface GetBoardGamesResponse{
    message:string
    result: BoardGame[]
}

export interface GetGenresResponse{
    message: string
    result: string[]
}

export interface OtherGameInfo{
    title: string
    description: string
    genres: string[]
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
    }
}
