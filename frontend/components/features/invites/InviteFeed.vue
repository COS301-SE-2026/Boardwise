<template> 
    <div class="d-flex flex-column ga-4 pa-4 h-100 overflow-y-auto">
        <InviteCard
            v-for="invite in invites"
            :key="invite.event.eventId"
            :invite="invite"
            @accept="respond($event, 'accept')"
            @decline="respond($event, 'decline')"
        />

        <div v-if="!invites.length" class="text-center text-medium-emphasis mt-8">
            <v-icon size="64">
                mdi-email-outline
            </v-icon>

            <p class="mt-4">
                No pending invites
            </p>
        </div>
    </div>
</template>

<script setup lang="ts">
import InviteCard from './InviteCard.vue';

import type { InviteItem } from '~/services/eventService';

const props = defineProps ({
    invites: {
        type: Array as () => InviteItem[],
        default: () => []
    }
})

const emit = defineEmits<{
    (e: 'accept', eventId: string): void
    (e: 'decline', eventId: string): void
}>()

const respond = (
    eventId: string, 
    status: 'accept' | 'decline'
) => {
    if (status === 'accept') {
        emit('accept', eventId)
    } else {
        emit('decline', eventId)
    }
}
</script>