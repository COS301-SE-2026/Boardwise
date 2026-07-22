<template>
    <div class="chat-layout mt-6">
        <ChatSidebar
            :conversations="conversations"
            :selected-id="selectedConversation.id"
            @select="selectConversation"
        />

        <ChatWindow
            :conversation="selectedConversation"
        />
    </div>
</template>

<script setup>
import { ref , computed, onMouted } from 'vue'

import ChatSidebar from './ChatSidebar.vue';
import ChatWindow from './ChatWindow.vue';
import { getChats } from '~/services/chatService.js'

import { useEvents } from '~/composables/useEvents'

const { inviteCount, fetchInvites } = useEvents()

onMouted(() => {
    fetchInvites()
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
        }
    ]
})

const selectedConversation = ref(conversations.value[0])

const selectConversation = (id) => {
    const conversation = conversations.value.find(c => c.id === id)

    if (conversation){
        selectedConversation.value = conversation
    }
}
</script>

<style scoped>
.chat-layout {
    display: grid;
    grid-template-columns: 320px 1fr;
    gap: var(--space-6);
    min-height: calc(100vh - 180px);
}

@media (max-width: 960px) {
    .chat-layout {
        grid-template-columns: 1fr;
    }
}
</style>