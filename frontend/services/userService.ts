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
    }
}