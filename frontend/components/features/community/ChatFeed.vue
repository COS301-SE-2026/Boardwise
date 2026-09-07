<template>
  <div 
    ref="feedEl"
    class="community-chat-feed"

  >

    <BaseEmptyState
      v-if="messages.length === 0"
      title="No messages yet"
      message="Be the first to say something!"
    />

    <ChatMessage
      v-for="message in messages"
      :key="message.id"
      :message="message"
    />

</div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import ChatMessage from './ChatMessage.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const feedEl = ref(null)

watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    feedEl.value?.scrollTo({ 
      top: feedEl.value.scrollHeight,
      behavior: 'smooth' })
  }
)
</script>

