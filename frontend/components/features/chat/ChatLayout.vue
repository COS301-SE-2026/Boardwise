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
                @back="mobileConversationOpen = false"
            />
        </div>
    </div>
</template>

<script setup>
import {
    computed,
    onMounted,
    ref
} from 'vue'

import ChatSidebar from './ChatSidebar.vue'
import ChatWindow from './ChatWindow.vue'

import { getChats } from '~/services/chatService.js'
import { useEvents } from '~/composables/useEvents'

const {
    inviteCount,
    fetchInvites
} = useEvents()

const mobileConversationOpen = ref(false)

onMounted(async () => {
    await fetchInvites()
})

const conversations = computed(() => {
    const lastMessage = inviteCount.value > 0
        ? `${inviteCount.value} pending invites`
        : 'No pending invites'

    return [
        {
            id: 'invites',
            name: 'Invites',
            avatar: '/images/default-listing.png',
            lastMessage,
            unread: inviteCount.value,
            online: false,
            isInvite: true
        },

        ...getChats()
    ]
})

const selectedConversation = ref(
    conversations.value[0] ?? null
)

const selectConversation = (id) => {
    const conversation =
        conversations.value.find(
            (item) => item.id === id
        )

    if (!conversation) return

    selectedConversation.value = conversation
    mobileConversationOpen.value = true
}
</script>