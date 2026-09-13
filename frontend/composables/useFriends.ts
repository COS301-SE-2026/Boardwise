import { createSharedComposable } from "@vueuse/core";
import { onUnmounted } from "vue";
import { FriendService, type FriendListDTO, type FriendRequestsDTO, type ProfileResponseDTO, type FriendRequestNotification, type FriendConfirmationNotification, NotificationType } from "~/services/friendService";


const { subscribe, unsubscribe } = useStomp();
const { show } = useSnackBar();


const _useFriends=()=>{
    const isLoading = ref(false);
    const userFriendList = ref<FriendListDTO>()
    const userFriendRequests = ref<FriendRequestsDTO>();
    const otherFriendList = ref<FriendListDTO>();
    const profile = ref<ProfileResponseDTO>();
    const dest = '/user/queue/notification';
    const token = localStorage.getItem("access_token");

    const getOwnFriendsList = async ()=>{
        isLoading.value = true;
        try{
            userFriendList.value = await FriendService.getOwnFriendsList();
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
            userFriendRequests.value = await FriendService.getFriendRequests();
        }
        catch(err){
            console.log(err);
        }
        finally{
            isLoading.value = false; 

        }
    };

    const sendFriendRequest = async (userId: string) =>{
        isLoading.value = true;
        try{
            const sendResp  = (await FriendService.sendFriendRequest(userId)).message; 
            show(sendResp, "success");            
        }
        catch(err){
            console.log(err);
            show("Error while sending friend request", "error");
            throw err;
        }
        finally{
            isLoading.value = false; 
        }
    };

    const getOtherUserProfile = async(userId: string)=>{
        isLoading.value = true
        try{
            profile.value = await FriendService.getOtherUserProfile(userId);
            // show("Successfully Fetched account", "success");
        }
        catch(err){
            console.log(err);
            // show("Could not fetch Profile", "error");
        }
        finally{
            isLoading.value = false;
        }
    }

    const getUserFriendsList = async (userId: string) =>{
        isLoading.value = true
        try{
            otherFriendList.value = await  FriendService.getUserFriendsList(userId);
            show("Successfully Fetched account", "success");
        }
        catch(err){
            console.log(err);
            show("Could not fetch Profile", "error");
        }
        finally{
            isLoading.value = false;
        }
    }

    const unfriendUser = async(id:string) =>{
        isLoading.value = true
        try{
            const v = await FriendService.unfriendUser(id);
            show("You have successfully unfriended them", "success");
            return v.message;

        }
        catch(err){
            console.log(err);
            show("Failed to Unfriend user", "error");
        }
        finally{
            isLoading.value = false;
        }
    }

    const respondToFriendRequest = async (id: string, action: "accept" | "decline") => {
        isLoading.value = true;
        try{
            const sendResp  = (await FriendService.respondToFriendRequest(id, action)).message; 
            show(sendResp, "success");            
        }
        catch(err){
            console.log(err);
            show("Error while sending friend request", "error");
            throw err;
        }
        finally{
            isLoading.value = false; 
        }
    }

    // websocket stuff 
    const listenForFriendNotifications = (handler: (notification : FriendRequestNotification | FriendConfirmationNotification) => void) => {
        if(!token) return;

        subscribe(dest, handler);
    }

    onUnmounted(() => unsubscribe(dest));

    return {
        unfriendUser, 
        getOwnFriendsList, 
        getFriendRequests, 
        sendFriendRequest, 
        getOtherUserProfile,
        getUserFriendsList , 
        isLoading, 
        otherFriendList, 
        profile,
        userFriendList,
        userFriendRequests,
        respondToFriendRequest,
        listenForFriendNotifications
    };
}

export const useFriends = createSharedComposable(_useFriends);