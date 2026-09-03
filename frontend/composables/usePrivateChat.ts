import { useStomp } from '~/composables/useStomp';
import { useProfile } from '~/composables/useProfile';
import { onUnmounted, computed } from 'vue';
import { type DirectMessageDTO, type ConversationDTO, ChatService } from '~/services/chatService';
import { jwtDecode } from 'jwt-decode';
import type { ProfileResponse } from '~/services/userService';

export interface DirectMessage{ // send
    id: string,
    receiverId: string,
    message: string
}

export interface Conversation{
    id: string,
    userId: string,
    username: string,
    profilePicture: string,
    lastMessage: string,
    lastMessageAt: string,
    isOnline: boolean,
    unread: boolean
}

export const usePrivateChat = () => {
    const { isConnected, subscribe, unsubscribe, sendPrivateMessage } = useStomp();
    const dest = "/user/queue/chat";
    const error = ref<string>('');
    const isLoading = ref<boolean>(false);
    const chats = ref<Array<Conversation>>([]);
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
            const response = await ChatService.getConversations();
            chats.value = response.map((el, i, arr) => {
                const unread = true;
                
                const newChat: Conversation = {
                    ...el,
                    unread
                }

                return newChat;
            })
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
        if(!token) return;

        subscribe(dest, async (message: DirectMessageDTO) => {
            // check for server echo
            const myUserId = jwtDecode(token).sub;
            const eIdx = messages.value.findIndex((el) => {
                el.id === message.id && el.senderId === myUserId;
            })
            if(eIdx !== -1){
                const existing = messages.value[eIdx];
                if(!existing) return;
                
                existing.sentAt = message.sentAt;
                messages.value[eIdx] = existing;

                // you'd also update indexedDB (for demo 4)


                return;
            }
            // update state 

            messages.value.push(message);
            // when new message is received push the conversation to the top
            // check if they have spoken before
            const convoId = generateConversationId(message.senderId, message.receiverId);
            

            const eId: number = chats.value.findIndex((el) => {
                el.id === generateConversationId(message.senderId, message.receiverId)
            })  
            if(eId !== -1 && chats.value[eId]){
                const convo: Conversation = chats.value[eId];
                chats.value.splice(eId, 1);
                convo.lastMessage = message.message;
                convo.lastMessageAt = message.sentAt;
                chats.value.push(convo);
            }
            else{
                const { fetchUserById } = useProfile();
                const convo: Conversation = {
                    id: convoId,
                    userId: message.senderId,
                    username: "",
                    profilePicture: "",
                    isOnline: true,
                    lastMessage: message.message,
                    lastMessageAt: message.sentAt,
                    unread: true
                }

                const sender: ProfileResponse | undefined = await fetchUserById(convo.userId);
                
                if(sender){
                    convo.username = sender.username;
                    convo.profilePicture = sender.profilePicture;
                    chats.value.push(convo);
                }
                
            }
        });
    }

    const sendMessage = (msg: DirectMessageDTO) => {
        if(!token) return;

        messages.value.push(msg);

        const toWire: DirectMessage = {
            id: msg.id,
            receiverId: msg.receiverId,
            message: msg.message
        };

        sendPrivateMessage(toWire);
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