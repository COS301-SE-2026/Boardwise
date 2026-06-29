<template>
  <div class="chat-feed" ref="feedEl">

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
    if (feedEl.value) {
      feedEl.value.scrollTo({ top: feedEl.value.scrollHeight, behavior: 'smooth' })
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.chat-feed {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  height: 400px;
  overflow-y: auto;
  padding: var(--space-4);
  background: var(--color-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
</style>