import { createSharedComposable } from "@vueuse/core";
import { FriendService, type friendList, type FriendRequestResponse, type FriendRequestsDTO } from "~/services/friendService";
const { show } = useSnackBar()

//
const _useFriends=()=>{
    const isLoading = ref(false);
    const userFriendList = ref<friendList>()
    const userFriendRequests = ref<FriendRequestsDTO>();
    const getOwnFriendsList = async ()=>{
        isLoading.value = true;
        try{
            userFriendList.value = await  FriendService.getOwnFriendsList();
        }
        catch(err){
            console.log(err);
        }
        finally{
            isLoading.value = false; 

        }
    }



    const getFriendRequests = async () =>{

        isLoading.value = true;
        try{
            userFriendRequests.value = await  FriendService.getFriendRequests();
        }
        catch(err){
            console.log(err);
        }
        finally{
            isLoading.value = false; 

        }
    };

    const wasFriendRequestSent = ref<boolean>(false);
    
    const sendFriendRequest = async (userId: string) =>{
        isLoading.value = true;
        try{
            const sendResp  = (await FriendService.sendFriendRequest(userId)).message; 
            show(sendResp, "success");            
        }
        catch(err){
            console.log(err);
            show("Error while sending friend request", "error");
        }
        finally{
            isLoading.value = false; 
        }
    };

    return {getOwnFriendsList, getFriendRequests, sendFriendRequest, wasFriendRequestSent, isLoading};
}

export const useFriends = createSharedComposable(_useFriends);