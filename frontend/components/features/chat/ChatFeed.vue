<template>
    <BaseCard class="flex-grow-1 overflow-y-auto pa-4">
        
        <ChatMessage
            v-for="message in messages"
            :key="message.id"
            :message="message"
        />
    </BaseCard>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue';
import ChatMessage from './ChatMessage.vue';

defineProps({
    messages: {
        type: Array,
        default: () => []
    }
})
</script>
