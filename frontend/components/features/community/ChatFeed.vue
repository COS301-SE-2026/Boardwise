<template>
  <div 
    ref="feedEl"
    class="community-chat-feed"

  >
    <BaseLoadingState v-if="isLoading" />

    <template v-else-if="messages.length">
      <ChatMessage
        v-for="message in messages"
        :key="message.id"
        :message="message"
        :token="token"
        :community="community"
      />
    </template>

    <BaseEmptyState
      v-else
      title="No messages yet"
      message="Be the first to say something!"
    />

</div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue'

import { useCommunityChat } from '~/composables/useCommunityChat'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import ChatMessage from './ChatMessage.vue'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'

const { isLoading } = useCommunityChat()

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  community: {
    type: Object,
    required: true
  },
  token: { 
    type: String, 
    required: true 
  }
})

const feedEl = ref(null)

const scrollToBottom = async () => {
  await nextTick()

  if(!feedEl.value) return

  feedEl.value.scrollTop = feedEl.value.scrollHeight
}

watch(
  () => props.messages.length,
  () => scrollToBottom(),
  {flush: 'post'}
)

onMounted(async () => {
  scrollToBottom()
})
</script>
