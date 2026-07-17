import { chats } from './mockData/chats'
import { messages } from './mockData/messages'

export const getChats = () => chats

export const getMessages = (chatId: number) => {
    return (
        messages.find(chat => chat.chatId === chatId)?.messages ?? []
    )
}