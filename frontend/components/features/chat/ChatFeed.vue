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
            <BaseLoadingState v-if="isLoading" />

            <template v-else-if="messages.length">
                <ChatMessage
                    v-for="message in messages"
                    :key="message.id"
                    :message="message"
                    :conversation="conversation"
                    :user="user"
                />
            </template>

            <BaseEmptyState
                v-else
                class="chat-feed__empty"
                title="No messages yet"
                message="Start the conversation by sending the first message."
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
import { useProfile } from '~/composables/useProfile'
import { useStomp } from '~/composables/useStomp'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import ChatMessage from './ChatMessage.vue'


const { fetchCurrentUser, isLoading } = useProfile()
const { connect } = useStomp()
const props = defineProps({
    messages: {
        type: Array,
        default: () => []
    },
    conversation: {
        type: Object,
        required: true
    }
})

const feedRef = ref(null)
const user = ref(null)

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

onMounted(async () => {
    user.value = await fetchCurrentUser();
    connect()
    scrollToBottom()
})
</script>