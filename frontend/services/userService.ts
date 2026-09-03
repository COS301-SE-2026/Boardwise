import { type FriendStatus } from "./friendService";

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

interface GameInventory {
    id: string;
    title: string;
    description: string;
    imageURL: string;
    genres: Array<string>;
}

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

export interface ProfileResponse{
    fullName: string;
    username: string;
    location: string;
    bio?: string;
    profilePicture: string;
    friendCount: number;
    groupCount: number;
    ownedGameCount: number;
    games: Array<Boardgame>;
    communities: Array<Community>;
    preferences: Preferences;
    createdAt: string;
    FriendStatus: FriendStatus;
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

export interface ProfileSearchResponse {
    id: string;
    username: string;
    fullName: string;
    profilePicture: string;
}

interface GenresResponse {
    message: string;
    genres: string[];
}
interface CreateBoardgameResponse {
    message: string;
}
interface BulkAddResponse {
    message: string;
    ownedGamesCount: number;
    games: GameInventory[];
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
        const { $api } = useNuxtApp();
        const formData = new FormData();
        formData.append("profilePicture", newPfp);

        return $api<ProfilePictureResponse>("/users/profilePicture", {
            method: 'POST',
            body: formData
        });
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

    //Search for Users
    searchForUser(query: string){
        const { $api } = useNuxtApp();
        return $api<ProfileSearchResponse[]>('/users/',{
            params:{
                search: query
            }
        });
    },

    createBoardgame(gameInfo: OtherGameDTO, gameImage: File){
        const { $api } = useNuxtApp();
        const formData = new FormData();

        formData.append('gameInfo', new Blob([JSON.stringify(gameInfo)], { type: 'application/json' }));
        formData.append('gameImage', gameImage);

        return $api<CreateBoardgameResponse>('/boardgames/', {
            method: 'POST',
            body: formData
        });
    },

    addGamesToInventory(payload: { knownGameIds: string[] }){
        const { $api } = useNuxtApp();

        return $api<BulkAddResponse>('users/gameInventory/bulk', {
            method: 'POST',
            body: payload
        });
    }
}