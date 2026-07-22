<template> 
    <BaseCard class="d-flex flex-column ga-4 pa-4 h-100 overflow-y-auto">
        <div v-if="props.invites.length">
            <InviteCard
                v-for="invite in props.invites"
                :key="invite.event.eventId"
                :invite="invite"
                @accept="$emit('accept', $event)"
                @decline="$emit('decline', $event)"
            />
        </div>

        <BaseEmptyState
            v-else
            title="No Invites"
            description="You don't have any pending event invites."
        />
    </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '~/components/ui/BaseCard.vue';
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';
import InviteCard from './InviteCard.vue';

import type { InviteItem } from '~/services/eventService';

const props = withDefaults(
    defineProps<{
        invites?: InviteItem[]
    }>(),
    {
        invites: () => []
    }
)

defineEmits(['accept', 'decline'])
</script>