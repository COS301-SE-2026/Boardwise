<template>
  <div class="d-flex flex-column ga-4">

    <ChatFeed :messages="messages" />

    <BaseCard 
      v-if="!community.isMember"
      class="pa-4">

      <p class="text-body-2 text-medium-emphasis mb-4">
        Join this community to participate in the discussion
      </p>

    <BaseButton 
      :disabled="loading"
      @click="$emit('join')"
    >
      {{ loading ? 'Joining...' : 'Join community' }}
    </BaseButton>

   </BaseCard>
      
    <ChatInput 
      v-else @send="handleSend"/>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatFeed from './ChatFeed.vue'
import ChatInput from './ChatInput.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useCommunity } from '~/composables/useCommunity'
const {
  loading
} = useCommunity()

defineProps({
  community: { type: Object, required: true }
})

defineEmits(['join'])

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
    time: new Date().toLocaleTimeString([], {
      hour: '2-digit', 
      minute: '2-digit' }),
    isOwn: true
  })
}
</script>
