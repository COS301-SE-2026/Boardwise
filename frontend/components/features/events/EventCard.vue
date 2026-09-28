<template> 
    <BaseCard data-test="event-card" clickable class="h-100 d-flex flex-column" @click="$emit('click', event)">
      <template #media>
        <BaseImage
            data-test="event-image"
            :src="event.imageUrl ?? '/default-event.png'"
            :alt="event.name"
            height="200px"
        />

        <BaseBadge
          data-test="event-status-badge"
          absolute
          size="x-small"
          tone="tonal"
          :variant="statusColor(event.eventStatus)"
        >
          {{ event.eventStatus }}
        </BaseBadge>
      </template>

      <p class="card-title" data-test="event-title">
        {{ event.name }}
      </p>

      <p class="card-meta" data-test="event-datetime">
        <v-icon size="16">mdi-calendar</v-icon>
        {{ event.date }} : {{ event.startTime  }} - {{ event.endTime }}
      </p>

      <p class="card-meta" data-test="event-location">
        <v-icon size="16">mdi-map-marker</v-icon>
        {{ event.location }}
      </p>
        
      <p class="card-meta" data-test="event-host">
        <v-icon size="16">mdi-account</v-icon>
        Hosted By @{{ event.host.username }}
      </p>

      <div class="d-flex ga-1 flex-wrap">
        <BaseBadge 
          v-for="game in event.games.slice(0,2)"
          :key="game.id"
          size="x-small"
          tone="tonal"
          variant="primary"
        >
          {{  game.title  }}
        </BaseBadge>

        <BaseBadge v-if="event.games.length > 2" size="x-small" tone="tonal" variant="default">
          <v-icon>mdi-plus</v-icon> {{  event.games.length -2 }}
        </BaseBadge>
      </div>

      <div class="d-flex justify-space-between align-center">
        <p class="card-meta" data-test="event-attendees">
          <v-icon size="12">mdi-account-group</v-icon>
          {{ event.attendeeCount }} attending
        </p>

        <BaseBadge
          data-test="event-rsvp"
          size="x-small"
          tone="tonal"
          :variant="rsvpColor(event.rsvpStatus)"
        >
          {{ event.rsvpStatus }}
        </BaseBadge>
      </div>
    </BaseCard>
</template>

<script setup> 
import BaseBadge from '~/components/ui/BaseBadge.vue'
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
  return 'neutral'
}

const rsvpColor = (status) => {
  if (status == 'ATTENDING') return 'success'
  if (status == 'INVITED') return 'warning'
  if (status == 'NOT_ATTENDING') return 'error'
  return 'neutral'
}
</script>