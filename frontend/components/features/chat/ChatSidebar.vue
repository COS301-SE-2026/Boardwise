<template>
    <BaseCard class="chat-sidebar pa-4 h-100">
        <header class="chat-sidebar__header">
            <div>
                <h1 class="chat-sidebar__title">
                    Chats
                </h1>

                <p class="text-body-2 text-medium-emphasis mb-0">
                    Stay connected with your Boardwise community.
                </p>
            </div>
        </header>

        <BaseSearch
            v-model="search"
            class="mt-5"
            placeholder="Search conversations"
            aria-label="Search conversations"
        />

        <div class="chat-sidebar__filters mt-4">
            <span
                id="chat-filter-label"
                class="text-body-2 font-weight-medium"
            >
                Show
            </span>

            <v-chip-group
                v-model="activeFilter"
                mandatory
                aria-labelledby="chat-filter-label"
            >
                <v-chip
                    value="all"
                    filter
                    variant="tonal"
                >
                    All
                </v-chip>

                <v-chip
                    value="online"
                    filter
                    variant="tonal"
                >
                    Online
                </v-chip>

                <v-chip
                    value="unread"
                    filter
                    variant="tonal"
                >
                    Unread
                </v-chip>
            </v-chip-group>
        </div>

        <v-divider class="my-4" />

        <div class="chat-sidebar__results">
            <ChatConversationList
                v-if="filteredConversations.length"
                :conversations="filteredConversations"
                :selected="selectedId"
                @select="emit('select', $event)"
            />

            <BaseEmptyState
                v-else
                class="mt-8"
                title="No conversations found"
                :description="emptyDescription"
            />
        </div>
    </BaseCard>
</template>

<script setup>
import { computed, ref } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'

import ChatConversationList from './ChatConversationList.vue'

const props = defineProps({
    conversations: {
        type: Array,
        default: () => []
    },

    selectedId: {
        type: [String, Number],
        default: null
    }
})

const emit = defineEmits(['select'])

const search = ref('')
const activeFilter = ref('all')

const filteredConversations = computed(() => {
    const query = search.value
        .trim()
        .toLowerCase()

    let list = [...props.conversations]

    if (query) {
        list = list.filter((conversation) =>
            conversation.name
                ?.toLowerCase()
                .includes(query)
        )
    }

    if (activeFilter.value === 'unread') {
        list = list.filter(
            (conversation) => Number(conversation.unread) > 0
        )
    }

    if (activeFilter.value === 'online') {
        list = list.filter(
            (conversation) => conversation.online
        )
    }

    return list
})

const emptyDescription = computed(() => {
    if (search.value.trim()) {
        return `No conversations match "${search.value.trim()}".`
    }

    if (activeFilter.value === 'online') {
        return 'None of your conversations are currently online.'
    }

    if (activeFilter.value === 'unread') {
        return 'You have no unread conversations.'
    }

    return 'Your conversations will appear here.'
})
</script>