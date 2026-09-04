export interface DirectMessageDTO{
    id: string,
    senderId: string,
    receiverId: string,
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
    isOnline: boolean
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

    async getMissedPrivateMessage(targetId: string, since: string | null | undefined){
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

    async getConversations(){
        const { $api } = useNuxtApp();
        const response = await $api<ConversationsResponseDTO>('messages/conversations');
        return response.conversations;
    }
}