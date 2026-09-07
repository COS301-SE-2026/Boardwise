<template>
    <div class="d-flex flex-column ga-3"
    >
        <ChatConversationCard
            v-for="conversation in conversations"
            :key="conversation.id"
            :conversation="conversation"
            :active="conversation.id === selected"
            @select="$emit('select', $event)"
        />

    </div>
</template>

<script setup>
import ChatConversationCard from './ChatConversationCard.vue';

const props = defineProps({
    conversations: {
        type: Array,
        default: () => []
    },
    selected: {
        type: [String, Number],
        default:null
    }
})

defineEmits(['select'])
</script>