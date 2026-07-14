<template>
  <BaseCard 
    ref="feedEl"
    class="d-flex flex-column ga-4 pa-4"
    style="height:420px; overflow-y:auto;"
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

  </BaseCard>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import ChatMessage from './ChatMessage.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

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

