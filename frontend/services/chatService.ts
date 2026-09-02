export interface DirectMessageDTO{
    id: string,
    senderId: string,
    receiverId: string,
    message: string,
    sentAt: string
}

export interface CommunityMessageDTO{
    id: string,
    senderId: string,
    communityId: string,
    message: string,
    sentAt: string
}

export interface ConversationDTO{
    id: string,
    userId: string,
    username: string,
    profilePicture: string,
    lastMessage: string,
    lastMessageAt: string,
    isOnline: string
}

interface CommunityMessagesDTO{
    message: string,
    data: Array<CommunityMessageDTO>
}

interface DirectMessagesDTO{
    message: string,
    data: Array<DirectMessageDTO>
}

interface ConversationsResponseDTO{
    message: string;
    conversations: Array<ConversationDTO>;
}

export const ChatService = {

    async getMissedPrivateMessage(targetId: string, since: string){
        const { $api } = useNuxtApp();
        const response = await $api<DirectMessagesDTO>('messages/',{
            query: {
                type: "DIRECT",
                targetId,
                since
            }
        });
        return response.data;
    },

    async getMissedCommunityMessage(targetId: string, since: string | null | undefined){
        const { $api } = useNuxtApp();
        const response = await $api<CommunityMessagesDTO>('messages/',{
            query: {
                type: "COMMUNITY",
                targetId,
                since
            }
        });
        return response.data;
    },

    async getConversations(){
        const { $api } = useNuxtApp();
        const response = await $api<ConversationsResponseDTO>('messages/conversations');
        return response.conversations;
    }
}