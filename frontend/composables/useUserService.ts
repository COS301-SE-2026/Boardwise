import { createSharedComposable } from "@vueuse/core";
import { userService, type ProfileSearchResponse } from "~/services/userService";

//Use userservice 
const _useUserService = () =>  {
    const loading = ref(false);
    const userSearchResult = ref<ProfileSearchResponse[]>([]);
    const searchForUser = async (query: string) => {
        loading.value = true;

        try{
            userSearchResult.value = await userService.searchForUser(query);
        }
        catch(err){
            console.log(err);

        }
        finally{
            loading.value = false;
        }
    }  

    return {searchForUser};
}

export const useUserService = createSharedComposable(_useUserService);