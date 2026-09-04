<template>
  <div 
    ref="feedEl"
    class="community-chat-feed"

  >
    <v-container v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 60vh">
        <v-progress-circular indeterminate color="primary" size="48" />
    </v-container>


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
import { useStomp } from '~/composables/useStomp'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import ChatMessage from './ChatMessage.vue'

const { connect } = useStomp()
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
// const user = ref(null)

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
  connect()
  scrollToBottom()
})
</script>

.community-chat-feed {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  flex: 1 1 auto;
  min-height: 0;
  padding: 16px;
}