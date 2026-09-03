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


const {
    inviteCount,
    fetchInvites
} = useEvents()

const {
    chats,
    getChats
} = usePrivateChat()

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
})

const conversations = computed(() => {
    const lastMessage = inviteCount.value > 0
        ? `${inviteCount.value} pending invites`
        : 'No pending invites'

    return [
        {
            id: "boardwise-invites",
            username: 'Invites',
            profilePicture: '/images/default-listing.png',
            lastMessage,
            unread: inviteCount.value,
            isOnline: inviteCount.value > 0,
            isInvite: true
        },

        ...chats.value
    ]
})

const selectedConversation = ref(
    conversations.value[0] ?? null
)

const selectConversation = (id: string) => {
    const convoIndex =
        conversations.value.findIndex(
            (item) => item.id === id
        )

    const conversation = conversations.value[convoIndex];

    if (!conversation) return

    conversation.isOnline = !( conversation.username === 'Invites' && inviteCount.value > 0 )

    selectedConversation.value = conversation
    mobileConversationOpen.value = true
}
</script>