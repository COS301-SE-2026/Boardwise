<template>
    <div class="pa-6">
        <BaseButton variant="secondary"
            prepend-icon="mdi-arrow-left" 
            class="md-4"
            @click="router.push(`/events`)"
        >
            Back to events
        </BaseButton>
        
        <BaseImage
            :src="event.imageUrl ?? '/default-event.png'"
            :alt="event.name"
            height="200px"
            fit="cover"
            style="border-radius: 12px;"
        />

        <div class="mt-4 d-flex flex-column ga-3">

            <div class="d-flex justify-space-between align-center">
                <h2 class="text-h5 font-weight-bold">{{ event.name }}</h2>
                <v-chip :color="statusColor(event.eventStatus)"  variant="tonal" size="small">
                    {{ event.eventStatus }}
                </v-chip>
            </div>

            <p class="text-body-2 text-medium-emphasis">
                {{ event.description }}
            </p>

            <v-divider />

            <div class="d-flex flex-column ga-2">
                <p class="text-body-2">
                    <v-icon size="16" class="mr-1">mdi-calendar</v-icon>
                    {{ event.date }} : {{ event.startTime }} - {{ event.endTime }}
                </p>

                <p class="text-body-2">
                    <v-icon size="16" class="mr-1">mdi-map-marker</v-icon>
                    {{ event.location }}
                </p>

                <p class="text-body-2">
                    <v-icon size="16" class="mr-1">mdi-account</v-icon>
                    Hosted by @{{ event.host.username }} 
                </p>

                <p class="text-body-2">
                    <v-icon size="16" class="mr-1">mdi-account-group</v-icon>
                    {{ event.attendeeCount }} attending
                </p>

                <p class="text-body-2">
                    <v-icon size="16" class="mr-1">mdi-eye</v-icon>
                    {{ event.visibility }}
                </p>
            </div>

            <v-divider />

            <div v-if="event.games.length">
                <p class="text-body-2 font-weight-bold mb-2">Games</p>

                <div class="games-grid">
                    <BaseCard
                        v-for="game in event.games"
                        :key="game.id"
                        rounded="lg"
                    >
                        <BaseImage
                            :src="game.imageUrl"
                            :alt="game.title"
                            height="120px"
                            width="100%"
                            fit="cover"
                        />

                        <div class="pa-3">
                            <p class="text-body-2 font-weight-bold mb-1">
                                {{ game.title }}
                            </p>
                            <p class="text-caption text-medium-emphasis mb-0">
                                {{ game.genres.join(', ') }}
                            </p>
                        </div>
                    </BaseCard>
                </div>
            </div>

            <v-divider />
                
            <div class="d-flex flex-column ga-2">
                <template v-if="!event.isHost">
                    <BaseButton
                        v-if="event.rsvpStatus !== 'ATTENDING'"
                        :disabled="event.eventStatus !== 'OPEN'"
                        @click="$emit('rsvp', event.id)"
                    >
                        <v-icon start>mdi-calendar-check</v-icon>
                        RSVP to event
                    </BaseButton>

                    <BaseButton
                        v-else
                        variant="secondary"
                        @click="$emit('de-rsvp', event.id)"
                    >
                        <v-icon start>mdi-calendar-remove</v-icon>
                        Cancel RSVP
                    </BaseButton>
                </template>

                <template v-if="event.isHost">
                    <BaseButton
                        variant="secondary"
                        @click="$emit('edit', event)"
                    >
                        <v-icon start>mdi-pencil</v-icon>
                        Edit event
                    </BaseButton>

                    <BaseButton
                        variant="secondary"
                        @click="$emit('cancel-event', event.id)"
                    >
                        <v-icon start>mdi-cancel</v-icon>
                        Cancel event
                    </BaseButton>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from '#vue-router'

import BaseCard from '~/components/ui/BaseCard.vue';
import BaseImage from '~/components/ui/BaseImage.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

const router = useRouter()

const props = defineProps({
    event: {
        type: Object, 
        required: true
    }
})

defineEmits(['close', 'rsvp', 'de-rsvp', 'edit', 'cancel-event'])

// const isHost = computed(() => props.event.host.username === props.currentUser)

const statusColor = (status) => {
  if (status === 'OPEN')         return 'success'
  if (status === 'FULLY_BOOKED') return 'warning'
  if (status === 'CANCELLED')    return 'error'
  return 'grey'
}

</script>

<style scoped>
.games-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
}
</style>