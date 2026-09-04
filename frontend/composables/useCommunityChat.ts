import { jwtDecode } from 'jwt-decode';
import { useStomp } from '~/composables/useStomp';
import { useRoute } from 'vue-router';
import { type CommunityMessageDTO, CommunityService } from '~/services/communityService';

export interface CommunityMessage{
    id: string,
    communityId: string,
    message: string
}

const error = ref<string>('');
const isLoading = ref<boolean>(false);
const messages = ref<Array<CommunityMessageDTO>>([]);

export const useCommunityChat = () => {
    const { isConnected, subscribe, unsubscribe, sendCommunityMessage } = useStomp();
    const route = useRoute();
    let dest: string | null = null;
    const token = localStorage.getItem("access_token");

    const lastMessageTime = computed(() =>{
        if(messages.value.length === 0) return null;

        return messages.value[messages.value.length - 1]?.sentAt
    })

    const getMissedCommunityMessages = async (targetId: string) => {
        error.value = '';
        isLoading.value = true;

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

    watch(
        () => route.params.id,
        (id: any) => {
            if (typeof id === "string") {
                subToComm(id);
            }
        },
        { immediate: true }
    )

    onUnmounted(() => unsubscribe(dest!));

    return {
        isConnected,
        isLoading,
        error,
        sendGroupMessage,
        getMissedCommunityMessages,
        messages
    }
}