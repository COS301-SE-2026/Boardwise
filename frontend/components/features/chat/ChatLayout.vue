<template>
    <div
        class="chat-layout mt-6"
        :class="{
            'chat-layout--conversation-open': mobileConversationOpen
        }"
    >
        <div class="chat-layout__sidebar">
            <ChatSidebar
                :conversations="conversations"
                :selected-id="selectedConversation?.id"
                @select="selectConversation"
            />
        </div>

        <div class="chat-layout__window">
            <ChatWindow
                v-if="selectedConversation"
                :conversation="selectedConversation"
                :show-back="mobileConversationOpen"
                :token="token"
                @back="mobileConversationOpen = false"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import {
    computed,
    onMounted,
    ref
} from 'vue'

import ChatSidebar from './ChatSidebar.vue'
import ChatWindow from './ChatWindow.vue'

import { useEvents } from '~/composables/useEvents'
import { usePrivateChat } from '~/composables/usePrivateChat'
import { useRoute, useRouter } from 'vue-router'


const {
    inviteCount,
    fetchInvites
} = useEvents()

const {
    chats,
    currentChat,
    getChats,
    startNewConversation
} = usePrivateChat()

const router = useRouter()
const route = useRoute()

const mobileConversationOpen = ref(false)

const props = defineProps({
    token: {
        type: String,
        required: true
    }
})

onMounted(async () => {
    await fetchInvites()
    await getChats()

    if(route.query.newChat){
        await startNewConversation(route.query.newChat as string)
        router.replace({ query: {} })
    }

    if(currentChat.value){
        selectConversation(currentChat.value.id)
    }
    else if(conversations.value.length > 0 && conversations.value[0])
        selectConversation(conversations.value[0].id)
})

const conversations = computed(() => {
    const lastMessage = inviteCount.value > 0
        ? `${inviteCount.value} pending invites`
        : 'No pending invites'

    return [
        {
            id: "boardwise-invites",
            userId: 'invites',
            username: 'Invites',
            profilePicture: '/images/default-listing.png',
            lastMessage,
            unread: Boolean(inviteCount.value),
            isOnline: inviteCount.value > 0,
            isInvite: true,
            lastMessageAt: new Date().toISOString()
        },

        ...chats.value
    ]
})

const selectedId = ref<string | null>(null)
const selectedConversation = computed(() =>
    conversations.value.find((c) => c.id === selectedId.value) ?? null
)

const selectConversation = (id: string) => {
    const convo = conversations.value.find((el) => el.id === id)
    if(!convo) return

    convo.isOnline = !(convo.username === 'Invites' && inviteCount.value > 0)
    selectedId.value = id
    mobileConversationOpen.value = true
    currentChat.value = convo.isInvite ? null : convo
}
</script>