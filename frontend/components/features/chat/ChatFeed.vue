<template>
    <BaseCard class="chat-feed-card flex-grow-1">
        <div
            ref="feedRef"
            class="chat-feed"
            role="log"
            aria-label="Conversation messages"
            aria-live="polite"
            aria-relevant="additions text"
        >
            <template v-if="messages.length">
                <ChatMessage
                    v-for="message in messages"
                    :key="message.id"
                    :message="message"
                />
            </template>

            <BaseEmptyState
                v-else
                class="chat-feed__empty"
                title="No messages yet"
                description="Start the conversation by sending the first message."
            />
        </div>
    </BaseCard>

</template>

<script setup>
import {
    nextTick,
    onMounted,
    ref,
    watch
} from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import ChatMessage from './ChatMessage.vue'

const props = defineProps({
    messages: {
        type: Array,
        default: () => []
    }
})

const feedRef = ref(null)

const scrollToBottom = async () => {
    await nextTick()

    if (!feedRef.value) return

    feedRef.value.scrollTop =
        feedRef.value.scrollHeight
}

watch(
    () => props.messages.length,
    scrollToBottom
)

onMounted(scrollToBottom)
</script>