<template>
    <div class="d-flex flex-column ga-4 h-100">

        <ChatHeader
            :conversation="props.conversation"
        />

        <ChatFeed
            :messages="messages"
            class="flex-grow-1"
        />

        <ChatComposer
            @send="handleSend"
        />
    </div>
</template>

<script setup>
import { ref, watch } from 'vue'

import ChatHeader from './ChatHeader.vue';
import ChatFeed from './ChatFeed.vue';
import ChatComposer from './ChatComposer.vue';
import { getMessages } from '~/services/chatService.js';

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    }
})

const messages = ref([])

watch(
    () => props.conversation.id,
    (id) => {
        messages.value = [...getMessages(id)]
    },
    {
        immediate: true
    }
)

const handleSend = (text) => {
    messages.value.push({
        id: Date.now(),
        name: 'You',
        avatar: '/images/avatar.jpg',
        text,
        time: new Date().toLocaleTimeString([],{
            hour: '2-digit',
            minute: '2-digit'
        }),
        isOwn: true
    })
}
</script>
