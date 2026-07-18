interface Boardgame{
    id: string;
    title: string;
    description: string;
    imageURL: string;
    genres: Array<string>;
}
interface Community{
    id: string;
    name: string;
    image: string;
}

export interface OtherGameDTO {
    title: string;
    description: string;
    minPlayers: number;
    maxPlayers: number;
    minAge: number;
    duration: number;
    genres: Array<string>;
}

// interface GameInventory {
//     id: string;
//     title: string;
//     description: string;
//     imageURL: string;
//     genres: Array<string>;
// }

interface InventoryUpdateResponse {
    message: string;
    ownedGamesCount: number;
    games: Array<Boardgame>;
}

interface GameListItem {
    id: string;
    title: string;
}

interface BoardgameSearchResponse {
    message: string;
    boardGames: GameListItem[];
}

interface Preferences{
    visibility: string;
    genres : Array<string>;
}

interface ProfileResponse{
    fullName: string;
    username: string;
    location: string;
    profilePicture: string;
    friendCount: number;
    groupCount: number;
    ownedGameCount: number;
    games: Array<Boardgame>;
    communities: Array<Community>;
    preferences: Preferences;
    createdAt: string;
}

interface ProfileUpdateResponse{
    username: string;
    email: string;
    password: string; // Remove this from being returned by the updateProfile endpoint
}

interface ProfilePictureResponse{
    message: string;
    profilePictureUrl: string;
}

interface GenresResponse {
    message: string;
    genres: string[];
}

export const userService = {
    getCurrentUser(){
        const { $api } = useNuxtApp();
        return $api<ProfileResponse>("/users/");
    },

    getUser(username: string){
        const { $api } = useNuxtApp();
        return $api<ProfileResponse>("/users/" + username);
    },
    
    updateProfile(user: {
        username?: string,
        location?: string,
        name?: string
    }){
        let firstName: string | null | undefined = user.name != null ? 
                                user.name.split(" ")[0] :
                                null;
        let lastName: string | null | undefined = user.name != null ? 
                                user.name.split(" ")[1] :
                                null;

        const { $api } = useNuxtApp();
        return $api<ProfileUpdateResponse>('/users/', {
            method: 'PATCH',
            body: {
                firstName,
                lastName,
                username : user.username,
                location : user.location
            }
        });
    },

    updateProfilePicture(newPfp: File){
        
    },

    searchForBoardGame(game: string){
        const { $api } = useNuxtApp();

        return $api<BoardgameSearchResponse>('/boardgames/',{
            params: {
                query: game
            }
        })
    },

    addExistingGameToInventory(gameId: string){
        const { $api } = useNuxtApp();
        
        return $api<InventoryUpdateResponse>(`/users/gameInventory/${gameId}`, {
            method: 'POST'
        });
    },

    addGameToInventory(gameInfo: OtherGameDTO, gameImage: File){
        const { $api } = useNuxtApp();
        const formData = new FormData();

        formData.append('gameInfo', new Blob([JSON.stringify(gameInfo)], { type: 'application/json' }));
        formData.append('gameImage', gameImage);

        return $api<InventoryUpdateResponse>('/users/gameInventory', {
            method: 'POST',
            body: formData
        });
    },

    removeGameFromInventory(gameId: string) {
        const { $api } = useNuxtApp();

        return $api<InventoryUpdateResponse>(`/users/gameInventory/${gameId}`, {
            method: 'DELETE'
        });
    },

    getGenres(query?: string) {
        const { $api } = useNuxtApp();
        return $api<GenresResponse>('/boardgames/genres', {
            params: query ? { query } : undefined
        });
    },

}