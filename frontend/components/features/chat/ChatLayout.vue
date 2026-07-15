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
import { ref } from 'vue'

import ChatSidebar from './ChatSidebar.vue';
import ChatWindow from './ChatWindow.vue';

const conversations = ref ([
    {
        id: 1,
        name: 'Thabo M.',
        avatar: '/images/avatar.jpg',
        lastMessage: 'See you tonight!',
        time: '09:32',
        online: true,
        unread: 2
    },
    {
        id: 2,
        name: 'Breezy',
        avatar: '/images/avatar.jpg',
        lastMessage: 'Thanks',
        time: 'Yesterday',
        online: false,
        unread: 0
    }
])

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