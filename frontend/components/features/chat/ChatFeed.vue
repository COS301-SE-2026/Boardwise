<template>
    <BaseCard class="flex-grow-1 overflow-y-auto pa-4">
        <div v-if="messages.length">
            <ChatMessage
                v-for="message in messages"
                :key="message.id"
                :message="message"
            />
        </div>
        
        <BaseEmptyState
            v-else
            title="No messages"
            description="Start the conversation by sending a message."
        />

    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';
import ChatMessage from './ChatMessage.vue';
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';

defineProps({
    messages: {
        type: Array,
        default: () => []
    }
})
</script>
