<template>
    <BaseCard data-test="base-card" flush>

        <div class="d-flex ga-4 pa-4">
            <BaseImage
                data-test="base-image"
                :src="invite.event.image || '/images/default-listing.png'"
                :alt="invite.event.name"
                height="140px"
                width="180px"
                class="invite-card__image"

            />

            <div class="flex-grow-1">
                <p class="card-title mb-2">
                    {{ invite.event.name }}
                </p>

                <p class="card-meta mb-1">
                    <strong>
                        Host:
                    </strong>

                    {{ invite.host.username }}
                </p>

                <p class="card-meta-2 mb-4">
                    <strong>
                        Date:
                    </strong>
                    
                    {{ invite.event.date }}
                </p>

                <div class="d-flex ga-2">

                    <BaseButton
                        data-test="accept-button"
                        :loading="responding"
                        :disabled="responding"
                        @click="$emit('accept', invite.event.id)"
                    >
                        Accept
                    </BaseButton>

                    <BaseButton
                        data-test="decline-button"
                        variant="secondary"
                        :loading="responding"
                        :disabled="responding"
                        @click="$emit('decline', invite.event.id)"
                    >
                        Decline
                    </BaseButton>
                </div>
            </div>
        </div>
    </BaseCard>
</template>

<script setup lang="ts">
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseButton from '~/components/ui/BaseButton.vue';

import type { InviteItem } from '~/services/eventService'

defineProps<{
    invite: InviteItem, 
    responding?: boolean

}>()

defineEmits<{
    (e: 'accept', eventId: string): void
    (e: 'decline', eventId: string): void
}>()
</script>

<style scoped>
.invite-card__image {
    width: 180px;
    height: 140px;
    border-radius: 12px;
    object-fit: cover;
    flex-shrink: 0;
}
</style>