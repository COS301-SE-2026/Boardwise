import { jwtDecode } from 'jwt-decode';
import { useStomp } from '~/composables/useStomp';
import { useRoute } from 'vue-router';
import { type CommunityMessageDTO, type Member, CommunityService } from '~/services/communityService';

export interface CommunityMessage{
    id: string,
    communityId: string,
    message: string
}

export interface NewMemberNotification{
    type: 'COMMUNITY_CHAT',
    senderId: string,
    message: string
}

const error = ref<string>('');
const isLoading = ref<boolean>(false);
const messages = ref<Array<CommunityMessageDTO>>([]);

export const useCommunityChat = () => {
    const { isConnected, subscribe, unsubscribe, sendCommunityMessage } = useStomp();
    const route = useRoute();
    let dest: string | null = null;
    let notifDest: string | null = null;
    const token = localStorage.getItem("access_token");

    const lastMessageTime = computed(() =>{
        if(messages.value.length === 0) return null;

        return messages.value[messages.value.length - 1]?.sentAt
    })

    const getMissedCommunityMessages = async (targetId: string) => {
        error.value = '';
        isLoading.value = true;

        if(messages.value.length > 0 && messages.value[0]?.communityId !== targetId){
            messages.value = [];
        }

        try{
            if(!token) throw new Error("User is not authenticated");

            const res = await CommunityService.getMissedCommunityMessage(
                targetId, 
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

            error.value = err.data?.message || "Could not retrieve missed community messages."
            throw err;
        }
        finally{
            isLoading.value = false;
        }
    }

    const listenForMessages = (id: string) => {
        if(!token) return;

        subscribe(`/topic/community/${id}/chat`, (message: CommunityMessageDTO) => {
            console.log("Community message received!!\n", message);

            const myUserId = jwtDecode<{sub: string}>(token).sub;
            const serverEcho = message.senderId === myUserId;

            if(serverEcho){
                const eIdx = messages.value.findIndex((el) => {
                    return el.id === message.id && el.senderId === myUserId;
                })
                if(eIdx !== -1 && messages.value[eIdx]){
                    const existing = messages.value[eIdx];  
                    existing.sentAt = message.sentAt;
                    messages.value[eIdx] = existing;
            
                    // you'd also update indexedDB (for demo 4)
                }
            }
            else{
                messages.value.push(message);
            }
            
            messages.value.sort((a, b) => {
                return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
            })
        })
    }

    const listenForNewMemberJoin = (id: string, notificationHandler: (member: Member) => void) => {
        if(!token) return;

        subscribe(`/topic/community/${id}/notification`, (notification: NewMemberNotification) => {
            console.log("System community message received!!\n", notification);
            const newMember = JSON.parse(notification.message) as Member;
            notificationHandler(newMember);
            
        })
    }

    const sendGroupMessage = (msg: CommunityMessageDTO) => {
        if(!token) return;
        
        messages.value.push(msg);
        messages.value.sort((a, b) => {
            return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
        })

        const toWire: CommunityMessage = {
            id: msg.id,
            communityId: msg.communityId,
            message: msg.message
        };

        sendCommunityMessage(toWire);
    }

    const subToComm = (id: string) => {
        if(dest)
            unsubscribe(dest);
        
        dest = `/topic/community/${id}/chat`;
        listenForMessages(id);
        
    }

    const subToCommNotif = (id: string, notificationHandler: (member: Member) => void) => {
        if(notifDest)
            unsubscribe(notifDest);

        notifDest = `/topic/community/${id}/notification`;
        listenForNewMemberJoin(id, notificationHandler);
    }

    const unSubToCommNotif = () => {
        if(notifDest)
            unsubscribe(notifDest);
    }

    watch(
        () => route.params.id,
        (id: any) => {
            if (typeof id === "string") {
                subToComm(id);
            }
        },
        { immediate: true }
    )

    onUnmounted(() => {
        unsubscribe(dest!)
        unsubscribe(notifDest!)
    });

    return {
        isConnected,
        isLoading,
        error,
        sendGroupMessage,
        getMissedCommunityMessages,
        messages,
        listenForNewMemberJoin,
        subToCommNotif,
        unSubToCommNotif
    }
}