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

            <BaseFilterGroup title="Filter chats" class="mt-4">
                <v-chip-group
                    v-model="activeFilter"
                    mandatory
                    column
                >

                <v-chip value="all">
                    All
                </v-chip>

                 <v-chip value="online">
                    Online
                </v-chip>

                 <v-chip value="unread">
                    Unread
                </v-chip>
                </v-chip-group>
            </BaseFilterGroup>

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
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue';

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

const activeFilter = ref('all')

const filteredConversations = computed(() => {

    let list = [...props.conversations]

    if(search.value) {
        list = list.filter(conversation => 
            conversation.name
            .toLowerCase()
            .includes(search.value.toLowerCase())
        )
    }

    switch (activeFilter.value) {
        case 'unread':
            list= list.filter(conversation => conversation.unread > 0)
            break
        case 'online':
            list = list.filter(conversation => conversation.online)
            break
    }
            return list

})

</script>
