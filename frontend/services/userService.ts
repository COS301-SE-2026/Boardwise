interface Boardgame{
    id: string;
    title: string;
    description: string;
    imageURL: string;
    genres: Array<string>;
}
interface Preferences{
    visibility: string;
    genres : Array<string>;
}
interface ProfileResponse{
    fullName: string;
    username: string;
    profilePicture: string;
    friendCount: number;
    groupCount: number;
    ownedGameCount: number;
    games: Array<Boardgame>;
    preferences: Preferences;
    createdAt: string;
}

interface ProfileUpdateResponse{
    username: string;
    email: string;
    password: string; // Remove this from being returned by the updateProfile endpoint
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
    
    updateProfile(username: string){
        const { $api } = useNuxtApp();
        return $api<ProfileUpdateResponse>('/users/', {
            method: 'PATCH',
            body: {
            username
        }
        });
    }
}