<template>
  <div class="community-events">
    <div class="events-header">
      <h2>
        <v-icon left>mdi-calendar</v-icon>
        Upcoming Events
      </h2>
      <BaseButton @click="$emit('create-event')">+ Create Event</BaseButton>
    </div>

    <div class="events-list">
      <div v-for="event in events" :key="event.id" class="event-card">
        <div class="event-date">
          <span class="event-day">{{ formatDay(event.date) }}</span>
          <span class="event-month">{{ formatMonth(event.date) }}</span>
        </div>
        <div class="event-info">
          <h3>{{ event.name }}</h3>
          <p class="event-details">
            {{ event.game }} • {{ formatTime(event.time) }} • {{ event.attendees?.length || 0 }} players
          </p>
          <div class="event-meta">
            <span class="event-location">
              <v-icon size="16">mdi-map-marker</v-icon>
              {{ event.location }}
            </span>
            <span class="event-host">
              <v-icon size="16">mdi-account</v-icon>
              Hosted by {{ event.organiser }}
            </span>
            <BaseBadge :variant="event.visibility.toLowerCase()">
              {{ event.visibility }}
            </BaseBadge>
          </div>
        </div>
        <BaseButton 
          :variant="event.rsvped ? 'secondary' : 'primary'"
          @click="$emit('join-event', event.id)"
        >
          {{ event.rsvped ? 'Cancel' : 'Join' }}
        </BaseButton>
      </div>

      <div v-if="events.length === 0" class="event-empty">
        <v-icon size="48" color="var(--color-text-muted)">mdi-calendar-blank</v-icon>
        <p>No events scheduled for this community</p>
        <BaseButton variant="secondary" @click="$emit('create-event')">Plan an Event</BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'

defineProps({
  communityId: {
    type: Number,
    required: true
  },
  events: {
    type: Array,
    default: () => []
  }
})

defineEmits(['create-event', 'join-event'])

const formatDay = (dateStr) => {
  if (!dateStr) return '--'
  return new Date(dateStr).getDate().toString().padStart(2, '0')
}

const formatMonth = (dateStr) => {
  if (!dateStr) return '---'
  return new Date(dateStr).toLocaleString('en', { month: 'short' }).toUpperCase()
}

const formatTime = (timeStr) => {
  if (!timeStr) return '--:--'
  return timeStr.slice(0, 5)
}
</script>

<style scoped>
.community-events {
  padding: var(--space-2) 0;
}

.events-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.events-header h2 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--fs-h3);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  margin: 0;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.event-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  background: var(--color-surface);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-base);
}

.event-card:hover {
  box-shadow: var(--shadow-md);
}

.event-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  min-width: 52px;
}

.event-day {
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  line-height: 1;
}

.event-month {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.event-info {
  flex: 1;
  min-width: 0;
}

.event-info h3 {
  margin: 0 0 var(--space-1) 0;
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.event-details {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  margin: 0 0 var(--space-1) 0;
}

.event-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  align-items: center;
}

.event-location, .event-host {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.event-empty {
  text-align: center;
  padding: var(--space-8) var(--space-4);
  color: var(--color-text-muted);
}

.event-empty p {
  margin: var(--space-2) 0 var(--space-4) 0;
}

@media (max-width: 768px) {
  .event-card {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .event-date {
    flex-direction: row;
    gap: var(--space-3);
    justify-content: center;
  }

  .event-meta {
    justify-content: center;
  }

  .events-header {
    flex-direction: column;
    gap: var(--space-3);
  }
}
</style>