<template>
    <BaseCard class="pa-4 h-100">
            <SectionTitle
                title="Chats"
                subtitle="Stay connected"
            />

            <BaseSearch
                v-model="search"
                class="mt-4"
                placeholder="Search chats..."
            />

            <ChatConversationList
                v-if="filteredConversations.length"
                :conversations="filteredConversations"
                :selected="selectedId"
                @select="emit('select', $event)"
            />

            <BaseEmptyState
                v-else
                class="mt-10"
                title="No conversations"
                description="Start chatting with your community."
            /> 
    </BaseCard>
</template>

<script setup>
import { ref, computed } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue';
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';
import BaseSearch from '~/components/ui/BaseSearch.vue';
import SectionTitle from '~/components/ui/SectionTitle.vue';
import ChatConversationList from './ChatConversationList.vue';

const search = ref('')

const props = defineProps({
    conversations: {
        type: Array,
        default: () => []
    },
    selectedId: {
        type: Number,
        default: null
    }
})

const emit = defineEmits(['select'])

const filteredConversations = computed(() => {
    if(!search.value) return props.conversations

    return props.conversations.filter(conversation =>
        conversation.name.toLowerCase().includes(search.value.toLowerCase())
    )
})

</script>
