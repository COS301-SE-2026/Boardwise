<template>
  <div class="community-chat-content">
    <ChatFeed
      :messages="messages"
      class="community-chat-content__feed"
    />

    <div class="community-chat-content__composer">
      <div
        v-if="!community.isMember"
        class="community-chat-join"
      >
        <div>
          <p class="font-weight-bold mb-1">
            Join the conversation
          </p>

          <p class="text-body-2 text-medium-emphasis mb-0">
            Join this community to send messages.
          </p>
          <
            <BaseButton
              :disabled="loading"
              @click="$emit('join')"
            >
              {{ loading ? 'Joining...' : 'Join community' }}
            </BaseButton>
        </div>

        <div
        v-if="!community.isMember"
        class="community-chat-join"
      >
        <div>
          <p class="text-body-2 text-medium-emphasis mb-0">
            You're a member of this community.</p>
          <BaseButton
            variant="error"
            @click="$emit('leave')"
          >
            <v-icon
              icon="mdi-exit-to-app"
              class="me-2"
              aria-hidden="true"
            />
            Leave community
          </BaseButton>
        </div>

        <BaseButton
          :disabled="loading"
          @click="$emit('join')"
        >
          {{ loading ? 'Joining...' : 'Join community' }}
        </BaseButton>
      </div>

      <ChatInput
        v-else
        @send="handleSend"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatFeed from './ChatFeed.vue'
import ChatInput from './ChatInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import { useCommunity } from '~/composables/useCommunity'
const {
  loading
} = useCommunity()

defineProps({
  community: { type: Object, required: true }
})

defineEmits(['join','leave'])

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
