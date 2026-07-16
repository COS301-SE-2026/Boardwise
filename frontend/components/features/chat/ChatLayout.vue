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
import { getChats } from '~/services/chatService.js'

const conversations = ref (getChats())

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