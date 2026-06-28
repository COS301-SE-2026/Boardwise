<template>
  <div class="community-chats">
    <ChatFeed :messages="messages" />
    <ChatInput @send="handleSend" />
  </div>
</template>

<script setup>
import ChatFeed from './chat/ChatFeed.vue'
import ChatInput from './chat/ChatInput.vue'

defineProps({
  community: {
    type: Object,
    required: true
  },
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['send'])

const handleSend = (text) => {
  emit('send', text)
}
</script>

<style scoped>
.community-chats {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
</style>