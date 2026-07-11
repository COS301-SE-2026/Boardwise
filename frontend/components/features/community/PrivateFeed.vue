<template>
  <div class="community-chats">
    <ChatFeed :messages="messages" />
    <ChatInput @send="handleSend" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatFeed from './chat/ChatFeed.vue'
import ChatInput from './chat/ChatInput.vue'

defineProps({
  community: { type: Object, required: true }
})

const messages = ref([
  {
    id: 1,
    name: 'Thabo M.',
    avatar: '/images/avatar.jpg',
    text: 'Hey everyone! Anyone up for a game this weekend?',
    time: '10:12',
    isOwn: false
  },
  {
    id: 2,
    name: 'You',
    avatar: '/images/avatar.jpg',
    text: "I am! Let's do Catan.",
    time: '10:15',
    isOwn: true
  }
])

const handleSend = (text) => {
  messages.value.push({
    id: Date.now(),
    name: 'You',
    avatar: '/images/avatar.jpg',
    text,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    isOwn: true
  })
}
</script>

<style scoped>
.community-chats {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
</style>