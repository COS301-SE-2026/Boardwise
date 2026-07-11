<template> 
    <BaseCard 
      class="cursor-pointer overflow-hidden pa-0"
      @click="$emit('click', event)"
    >
        <BaseImage
            :src="event.imageUrl ?? '/default-event.png'"
            :alt="event.name"
            height="180px"
            fit="cover"
        />

        <div class="pa-3 d-flex flex-column ga-1">
          <div class="d-flex justify-space-between align-center">
            <p class="text-body-2 font-weight-bold mb-0">
              {{ event.name }}
            </p>

            <v-chip
              size="x-small"
              :color="statusColor(event.eventStatus)"
              variant="tonal"
            >
              {{  event.eventStatus  }}
            </v-chip>
          </div>

          <p class="text-caption text-medium-emphasis mb-0">
            <v-icon size="12">mdi-calendar</v-icon>
            {{ event.date }} : {{ event.startTime  }} - {{ event.endTime }}
          </p>

          <p class="text-caption text-medium-emphasis mb-0">
            <v-icon size="12">mdi-map-marker</v-icon>
            {{ event.location }}
          </p>
          
          <p class="text-caption text-medium-emphasis mb-0">
            <v-icon size="12">mdi-account</v-icon>
            Hosted By @{{ event.host.username }}
          </p>

          <div class="d-flex ga-1 flex-wrap mt-1">
            <v-chip 
              v-for="game in event.games.slice(0,2)"
              :key="game.id"
              size="x-small"
              variant="tonal"
              color="primary"
            >
              {{  game.title  }}
            </v-chip>

            <v-chip v-if="event.games.length > 2" size="x-small" variant="tonal">
              + {{  event.games.length -2 }}
            </v-chip>
          </div>

          <div class="d-flex justify-space-between align-center mt-2">
            <p class="text-caption text-medium-emphasis mb-0">
              <v-icon size="12">mdi account-group</v-icon>
                {{ event.attendeeCount }} attending
            </p>

            <v-chip 
              size="x-small"
              :color="rsvpColor(event.rsvpStatus)"
              variant="tonal"
            >
              {{ event.rsvpStatus }}
            </v-chip>
          </div>

        </div>
    </BaseCard>
</template>

<script setup> 
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    event: {
        type: Object,
        required: true
    }
})

defineEmits(['click'])

const statusColor = (status) => {
  if (status == 'OPEN') return 'success'
  if (status == 'FULLY_BOOKED') return 'warning'
  if (status == 'CANCELLED') return 'error'
  return 'grey'
}

const rsvpColor = (status) => {
  if (status == 'ATTENDING') return 'success'
  if (status == 'INVITED') return 'warning'
  if (status == 'NOT_ATTENDING') return 'error'
  return 'grey'
}
</script>