import api from "./api";

export const userService = {
    getCurrentUser(){
        return api.get("/users/");
    },

    getUser(username: string){
        return api.get("/users/" + username);
    },
    
    updateProfile(username: string){
        return api.patch('/users/', {
            username
        });
    }
}