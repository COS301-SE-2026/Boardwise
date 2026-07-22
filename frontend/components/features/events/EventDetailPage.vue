<template>
    <div class="pa-6">
        <BaseButton variant="secondary"
            prepend-icon="mdi-arrow-left" 
            class="md-4"
            @click="router.push('/events')"
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

                <div class="d-flex flex-column ga-2">
                    <div
                        v-for="game in event.games"
                        :key="game.id"
                        class="d-flex align-center ga-3"
                    >
                        <BaseImage
                            :src="game.imageUrl"
                            :alt="game.title"
                            width="40px"
                            height="40px"
                            fit="cover"
                            style="border-radius: 8px;"
                        />

                        <div>
                            <p class="text-body-2 font-weight-bold mb-0">{{ game.title }}</p>
                            <p class="text-caption text-medium-emphasis mb-0">
                                {{ game.genres.join(', ') }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <v-divider />
                
            <div class="d-flex flex-column ga-2">
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

                <template v-if="isHost">
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

import BaseImage from '~/components/ui/BaseImage.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

const router = userRouter()

const props = defineProps({
    event: {
        type: Object, 
        required: true
    }, 

    currentUser: {
        type: String, 
        default: ''
    }
})

defineEmits(['close', 'rsvp', 'de-rsvp', 'edit', 'cancel-event'])

const isHost = computed(() => props.event.host.username === props.currentUser)

const statusColor = (status) => {
  if (status === 'OPEN')         return 'success'
  if (status === 'FULLY_BOOKED') return 'warning'
  if (status === 'CANCELLED')    return 'error'
  return 'grey'
}

</script>