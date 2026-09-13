import { useStomp } from '~/composables/useStomp';
import { useProfile } from '~/composables/useProfile';
import { onUnmounted, computed } from 'vue';
import { type DirectMessageDTO, ChatService } from '~/services/chatService';
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
    unread: boolean;
    isInvite?: boolean
}

interface PresenceNotification{
    type: "PRESENCE",
    userId: string,
    isOnline: boolean
}

const error = ref<string>('');
const isLoading = ref<boolean>(false);
const chats = ref<Array<Conversation>>([]);
const currentChat = ref<Conversation | null | undefined>(null);
const messages = ref<Array<DirectMessageDTO>>([]);
const watchedPresenceUsers = new Set<String>();

export const usePrivateChat = () => {
    const { isConnected, subscribe, unsubscribe, sendPrivateMessage } = useStomp();
    const { fetchUserById } = useProfile();
    const dest = "/user/queue/chat";
    const token = localStorage.getItem("access_token");

    const lastMessageTime = computed(() =>{
        if(messages.value.length === 0) return null;

        return messages.value[messages.value.length - 1]?.sentAt
    })

    const generateConversationId = (userIdA: string, userIdB: string) => {
        if(!userIdA || !userIdB)
            throw new Error("Both user IDs are required.")

        return [userIdA, userIdB].sort().join('_');
    }

    const getMissedMessages = async (targetId: string) => {
        error.value = '';
        isLoading.value = true;

        try{
            if(!token) throw new Error("User is not authenticated");

            const userId = jwtDecode(token).sub

            const isSameChat = messages.value.length > 0 && 
                (messages.value[0]?.senderId === targetId || messages.value[0]?.receiverId === targetId);

            if(!isSameChat)
                messages.value = [];
            
            const res = await ChatService.getMissedPrivateMessage(
                generateConversationId(targetId, userId as string), 
                lastMessageTime.value
            );

            currentChat.value = chats.value.find((el) => {
                return el.id == targetId;
            })

            messages.value = res.sort((a, b) => {
                return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
            });
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

    const getChats = async (messageReceipt: boolean = false) => {
        error.value = '';
        isLoading.value = !messageReceipt;

        try{
            const response = await ChatService.getConversations();
            const fetchedChats = response.map((el) => {                
                const newChat: Conversation = {
                    ...el,
                    unread: false
                }
                return newChat;
            });

            const unsavedChats = chats.value.filter( el => 
                !fetchedChats.some((fetched) => fetched.id === el.id) &&
                (!el.lastMessage || el.lastMessage === "")
            );

            chats.value = [...unsavedChats, ...fetchedChats].sort((a, b) => 
                new Date(a.lastMessageAt).getTime() - new Date(b.lastMessageAt).getTime()
            );
            
            for(const chat of chats.value){
                listenForPresence(chat.userId);
            }
        }
        catch(err: any){
            error.value = err.data?.message || "Could not retrieve chats."
            throw err;
        }
        finally{
            isLoading.value = false;
        }
    }

    // websocket stuff
    const listenForMessages = () => {
        if(!token) return;

        subscribe(dest, async (message: DirectMessageDTO) => {            
            // check for that we have this chat open and that is a server echo or not
            const myUserId = jwtDecode<{sub: string}>(token).sub;
            const fromPartner = message.senderId == currentChat.value?.userId;
            const serverEcho = message.senderId == myUserId;
            
            if(fromPartner || serverEcho ){
                
                const eIdx = messages.value.findIndex((el) => {
                    return el.id === message.id && el.senderId === myUserId;
                })
                if(eIdx !== -1 && messages.value[eIdx]){
                    const existing = messages.value[eIdx];  
                    existing.sentAt = message.sentAt;
                    messages.value[eIdx] = existing;
            
                    // you'd also update indexedDB (for demo 4)
                }
                else if(fromPartner){
                    messages.value.push(message);
                }
                // update state 
                messages.value.sort((a, b) => {
                    return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
                })
            }
        
            // when new message is received push the conversation to the top
            // check if they have spoken before
            const convoId = generateConversationId(message.senderId, message.receiverId);
            const eId: number = chats.value.findIndex((el) => {
                return el.id === convoId
            })  
            if(eId !== -1 && chats.value[eId]){
                const convo: Conversation = chats.value[eId];
                chats.value.splice(eId, 1);
                convo.lastMessage = message.message;
                convo.lastMessageAt = message.sentAt;
                chats.value.unshift(convo);
            }
            else{
                
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

                chats.value.unshift(convo);

                try{
                    const sender: ProfileResponse | undefined = await fetchUserById(convo.userId);
                
                    if(sender){
                        convo.username = sender.username;
                        convo.profilePicture = sender.profilePicture;
                    }
                }
                catch(err){
                    console.error("[Private chat composable]: Making request for user data failed:", err);
                }
               
            }
        });
    }

    const listenForPresence = (userId: string) => {
        if(watchedPresenceUsers.has(userId)){
            console.warn(`[usePrivateChat]: "/topic/presence/${userId}" already subscribed to.`)
            return;
        } 

        watchedPresenceUsers.add(userId);
        subscribe(`/topic/presence/${userId}`, (presence: PresenceNotification) => {
            console.log("[usePrivateChat] Presence Notification received!!!")
            
            const cIdx = chats.value.findIndex((el) => el.userId === presence.userId);
            if(cIdx !== -1 && chats.value[cIdx]){
                chats.value[cIdx].isOnline = presence.isOnline;
            }
            if(currentChat.value?.userId === presence.userId){
                currentChat.value.isOnline = presence.isOnline;
            }
        })
    }

    const sendDirectMessage = (msg: DirectMessageDTO) => {
        if(!token) return;

        messages.value.push(msg);
        messages.value.sort((a, b) => {
            return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
        })

        const toWire: DirectMessage = {
            id: msg.id,
            receiverId: msg.receiverId,
            message: msg.message
        };

        sendPrivateMessage(toWire);
    }

    const startNewConversation = async (receiverId: string) => {
        const senderId = jwtDecode(token!).sub;
        if(!senderId) return;

        const convoId = generateConversationId(senderId, receiverId);
        let chat = chats.value.find((el) => {
            return el.id === convoId;
        })  

        if(chat){
            chats.value = chats.value.filter((el) => el.id !== convoId)
            chats.value.unshift(chat)
        }
        else{
            const { fetchUserById } = useProfile();
            const sender: ProfileResponse | undefined = await fetchUserById(receiverId);

            if(!sender) return;

            chat = {
                id: convoId,
                userId: receiverId,
                username: sender.username,
                profilePicture: sender.profilePicture,
                isOnline: false,
                lastMessage: "",
                lastMessageAt: new Date().toISOString(),
                unread: false
            }
            chats.value.unshift(chat);
        }
        currentChat.value = chat;
        messages.value = [];
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
    
    onUnmounted(() => { 
        unsubscribe(dest);
        for(const userId of watchedPresenceUsers){
            unsubscribe(`/topic/presence/${userId}`);
        }
        watchedPresenceUsers.clear();
    });

    return { 
        isConnected, 
        sendDirectMessage, 
        getMissedMessages, 
        getChats, 
        isLoading, 
        error,
        chats,
        messages,
        generateConversationId,
        startNewConversation,
        currentChat 
    };
}