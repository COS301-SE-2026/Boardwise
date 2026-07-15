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

const props = defineProps({
    conversation: {
        type: Object,
        required: true
    }
})

const messages = ref([])

watch(
    () => props.conversation,
    () => {
        messages.value = [
            {
                id:1,
                name: props.conversation.name,
                avatar: props.conversation.avatar,
                text: 'Hey! Are you still coming to board game night',
                time: '09:30',
                isOwn: false
            },
            {
                id:2,
                name: 'You',
                avatar: '/images/avatar.jpg',
                text: 'Yep! I will be there',
                time: '09:32',
                isOwn: true
            }
        ]
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
