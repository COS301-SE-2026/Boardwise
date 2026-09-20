import { useStomp } from '~/composables/useStomp';
import { useProfile } from '~/composables/useProfile';
import { useSnackBar } from '~/composables/useSnackbar';
import { onUnmounted, computed } from 'vue';
import { type DirectMessageDTO, ChatService } from '~/services/chatService';
import { jwtDecode } from 'jwt-decode';
import type { ProfileResponse } from '~/services/userService';
import { ms } from 'vuetify/iconsets/ms';

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
    lastMessageSender: string,
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
const watchedPresenceUsers = new Set<string>();

export const usePrivateChat = () => {
    const { show } = useSnackBar();
    const { isConnected, subscribe, unsubscribe, sendPrivateMessage } = useStomp();
    const { fetchUserById, fetchUserPresence } = useProfile();
    const dest = "/user/queue/chat";
    const token = localStorage.getItem("access_token");
    const pendingChat = ref<Conversation | null>(null);

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
            chats.value = response.map((el) => {                
                const newChat: Conversation = {
                    ...el,
                    lastMessage: getMessagePreview(el.lastMessage),
                    unread: false
                }
                return newChat;
            }).sort((a, b) =>  new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime());
            
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
            const fromPartner = message.senderId === currentChat.value?.userId;
            const serverEcho = message.senderId === myUserId;
            
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
                convo.lastMessage = getMessagePreview(message.message);
                convo.lastMessageSender = message.senderId;
                convo.lastMessageAt = message.sentAt;
                convo.unread = !fromPartner && !serverEcho;
                chats.value.unshift(convo);
            }
            else{
        
                try{
                    const convo: Conversation = {
                        id: convoId,
                        userId: message.senderId,
                        username: "",
                        profilePicture: "",
                        isOnline: true,
                        lastMessage: getMessagePreview(message.message),
                        lastMessageSender: message.senderId,
                        lastMessageAt: message.sentAt,
                        unread: true
                    }

                    const sender: ProfileResponse | undefined = await fetchUserById(convo.userId);
                
                    if(sender){
                        convo.username = sender.username;
                        convo.profilePicture = sender.profilePicture;
                    }

                    chats.value.unshift(convo);
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

        if(pendingChat.value && pendingChat.value.id === currentChat.value?.id){
            pendingChat.value.lastMessage = getMessagePreview(msg.message);
            pendingChat.value.lastMessageAt = msg.sentAt;
            chats.value.unshift(pendingChat.value);
            pendingChat.value = null;
        }
        else{
            const existing = chats.value.find((el) => el.id === currentChat.value?.id);
            if(existing){
                existing.lastMessage = getMessagePreview(msg.message);
                existing.lastMessageAt = msg.sentAt;
            }
        }

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
        let chat: Conversation | undefined = chats.value.find((el) => {
            return el.id === convoId;
        });  

        if(chat){
            chats.value = chats.value.filter((el) => el.id !== convoId);
            chats.value.unshift(chat);
            currentChat.value = chat;
            pendingChat.value = null;
            messages.value = [];
            return;
        }

        try{
            const receiver: ProfileResponse | undefined = await fetchUserById(receiverId);
        
            if(!receiver) {
                show("That user is no longer on boardwise", "info");
                return;
            }

            const receiverOnline: boolean = await fetchUserPresence(receiverId);
            listenForPresence(receiverId);

            chat = {
                id: convoId,
                userId: receiverId,
                username: receiver.username,
                profilePicture: receiver.profilePicture,
                isOnline: receiverOnline,
                lastMessage: "",
                lastMessageSender: "",
                lastMessageAt: new Date().toISOString(),
                unread: false
            };

            pendingChat.value = chat;
            currentChat.value = chat;
            messages.value = [];
        }
        catch(err){
            show("Something went wrong when starting a conversation with this user", "error");
        }

    }

    const getMessagePreview = (rawMessage: string) => {
        if(!rawMessage) return '';

        try{
            const listingMessage = JSON.parse(rawMessage);
            if(listingMessage && typeof listingMessage === 'object' && listingMessage.type === 'LISTING_QUERY'){
                return `Enquired about listing: ${listingMessage.listingTitle ?? 'a listing'}`;
            }
            return rawMessage;
        }
        catch{
            return rawMessage;
        }
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
        currentChat,
        pendingChat 
    };
}