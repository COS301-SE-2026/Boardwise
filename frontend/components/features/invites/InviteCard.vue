<template>
    <BaseCard class="pa-4">

        <div class="d-flex ga-4">

            <BaseImage
                :src="invite.event.image || '/images/default-listing.png'"
                :alt="invite.event.name"
                class="event-image"

            />

            <div class="flex-grow-1">

                <h3 class="text-h6 mb-2">
                    {{ invite.event.name }}
                </h3>

                <p class="text-body-2 mb-1">
                    <strong>
                        Host:
                    </strong>

                    {{ invite.host.username }}
                </p>

                <p class="text-body-2 mb-4">
                    <strong>
                        Date:
                    </strong>
                    
                    {{ invite.event.date }}
                </p>

                <div class="d-flex ga-2">

                    <v-btn
                        color="primary"
                        @click="$emit('accept', invite.event.id)"
                    >
                        Accept
                    </v-btn>

                    <v-btn
                        variant="outlined"
                        color="error"
                        @click="$emit('decline', invite.event.id)"
                    >
                        Decline
                    </v-btn>

                </div>
            </div>
        </div>
    </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

import type { InviteItem } from '~/services/eventService'

defineProps<{
    invite: InviteItem
}>()

defineEmits<{
    (e: 'accept', eventId: string): void
    (e: 'decline', eventId: string): void
}>()
</script>

<style scoped>
.event-image {
    width: 180px;
    height: 140px;
    border-radius: 12px;
    object-fit: cover;
    flex-shrink: 0;
}
</style>