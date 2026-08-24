import { userService, type OtherGameDTO  } from "@/services/userService";
import { useRouter } from "vue-router";

export const useProfile = () => {
    const isLoading = ref(false)
    const router = useRouter()
    const error = ref('')

    const fetchCurrentUser = async () => {
        isLoading.value = true;
        error.value = ''
        try{
            const res = await userService.getCurrentUser()
            console.log(res)
            return res
        }
        catch(err: any){
            error.value = err.data?.message || "This user does not exist"
            if(err.response?.status === 401){
                localStorage.removeItem("access_token")
                router.push('/auth/signin')
                return;
            }
            throw err;
                
        }
        finally{
            isLoading.value = false
        }
    }

    const updateProfile = async (user: {
        username?: string,
        location?: string,
        name?: string
    }) => {
        isLoading.value = true;
        error.value = ''
        if(user.name){
            const tName = user.name.trim();
            if(tName.split(" ").length !== 2){
                error.value = "Name must be separated by exactly one space (example: 'First Name')"
                throw new Error("Error during update profile.");
            }
        }

        try{
            const res = await userService.updateProfile(user)
            return res
        }
        catch(err: any){
            error.value = err.data?.message || "Profile update failed"
            if(err.response?.status === 401){
                localStorage.removeItem("access_token")
                router.push('/auth/signin')
                return;
            }
            throw err;
                
        }
        finally{
            isLoading.value = false
        }
    };

    const updateProfilePicture = async (newPfp: File) => {
        isLoading.value = true;
        error.value = ''
        try{
            const res = await userService.updateProfilePicture(newPfp);
            return res.profilePictureUrl;
        }
        catch(err: any){
            error.value = err.data?.message || "Profile picture update failed"
            if(err.response?.status === 401){
                localStorage.removeItem("access_token")
                router.push('/auth/signin')
                return;
            }
            throw err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const searchGames = async (game: string) => {
        isLoading.value = true;
        error.value = ''
        try {
            const res = await userService.searchForBoardGame(game);
            return res.boardGames
        } catch(err) {
            error.value = "Search failed"
            throw err
        } finally {
            isLoading.value = false
        }
    };

    const addExistingGame = async (gameId: string) => {
        isLoading.value = true;
        error.value = ''
        try {
            const res = await userService.addExistingGameToInventory(gameId);
            return res;
        } catch (err: any) {
            error.value = "Failed to add game";
            if (err.response?.status === 401) {
                router.push('/auth/signin');
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    const addGame = async (gameInfo: OtherGameDTO, gameImage: File) => {
        isLoading.value = true;
        error.value = ''
        try {
            const res = await userService.addGameToInventory(gameInfo, gameImage);
            return res;
        } catch (err: any) {
            error.value = "Failed to add game";
            if (err.response?.status === 401) {
                router.push('/auth/signin');
            }
            throw err; // modal won't close on fail  
        } finally {
            isLoading.value = false;
        }
    };

    const removeGame = async (gameId: string) => {
        isLoading.value = true;
        error.value = ''
        try {
            const res = await userService.removeGameFromInventory(gameId);
            return res;
        } catch (err: any) {
            error.value = "Failed to remove game";
            if (err.response?.status === 401) {
                router.push('/auth/signin');
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    return { isLoading, fetchCurrentUser, updateProfile, updateProfilePicture, addGame, removeGame, searchGames ,addExistingGame, error }
}