import { useStomp } from '~/composables/useStomp';
import { onUnmounted, computed } from 'vue';
import { type DirectMessageDTO, type ConversationDTO, ChatService } from '~/services/chatService';
import { jwtDecode } from 'jwt-decode';

export interface DirectMessage{ // send
    id: string,
    receiverId: string,
    message: string
}

export const usePrivateChat = () => {
    const { isConnected, subscribe, unsubscribe, sendPrivateMessage } = useStomp();
    const dest = "/user/queue/chat";
    const error = ref<string>('');
    const isLoading = ref<boolean>(false);
    const chats = ref<Array<ConversationDTO>>([]);
    const messages = ref<Array<DirectMessageDTO>>([]);
    const token = localStorage.getItem("access_token");
    const lastMessageTime = computed(() =>{
        if(messages.value.length === 0) return null;

        return messages.value[messages.value.length - 1]?.sentAt
    })

    const getMissedMessages = async (targetId: string) => {
        error.value = '';
        isLoading.value = true;

        try{
            if(!token) throw new Error("User is not authenticated");

            messages.value = await ChatService.getMissedPrivateMessage(
                targetId, 
                lastMessageTime.value ?? new Date().toISOString()
            );
        }
        catch(err: any){
            if(err.message.includes("authenticated")){
                const router = useRouter()
                localStorage.removeItem("access_token");
                router.push("/auth/signin")
                return;
            }

            error.value = err.data?.message || "Could not retrieve missed messages."
            throw err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const getChats = async () => {
        error.value = '';
        isLoading.value = true;

        try{
            chats.value = await ChatService.getConversations();
        }
        catch(err: any){
            error.value = err.data?.message || "Could not retrieve chats."
            throw err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const generateConversationId = (userIdA: string, userIdB: string) => {
        if(!userIdA || !userIdB)
            throw new Error("Both user IDs are required.")

        return [userIdA, userIdB].sort().join('_');
    }

    // websocket stuff
    const listenForMessages = () => {
        subscribe(dest, (message: DirectMessageDTO) => {
            messages.value.push(message);
        });
    }

    const sendMessage = (msg: {
        receiverId: string,
        message: string
    }) => {
        if(!token) return

        const decoded = jwtDecode(token);
        const senderId = decoded.sub ?? "";
        const id = crypto.randomUUID();
        const sentAt = new Date().toISOString();

        sendPrivateMessage({ id, ...msg });
        messages.value.push({
            id, 
            ...msg,
            sentAt,
            senderId
        });
    }

    if(isConnected.value){
        listenForMessages();
    }
    else{
        const stop = watch(isConnected, (connected) => {
            if(connected){
                listenForMessages();
                stop();
            }
        })
    }
    
    onUnmounted(() => unsubscribe(dest));

    return { 
        isConnected, 
        sendMessage, 
        getMissedMessages, 
        getChats, 
        isLoading, 
        error,
        chats,
        messages,
        generateConversationId 
    };
}