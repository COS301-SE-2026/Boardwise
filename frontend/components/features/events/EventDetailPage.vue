<template>
    <div class="event-detail">

        <!-- Host staus banner -->
        <div v-if="event.isHost" class="event-detail__host-banner">
            <div class="event-detail__host-banner-info">
                <v-icon size="20">mdi-shield-account</v-icon>
                <div>
                    <p class="event-detail__host-banner-title">Host view active</p>
                    <p class="event-detail__host-banner-sub">
                        You're manging this event as host. Changes update instantly for attendees.
                    </p>
                </div>
            </div>

            <BaseButton variant="secondary" border="var(--alabastar)"  color="var(--alabastar)" size="md" @click="$emit('edit', event)">
                <v-icon start size="16">mdi-pencil</v-icon>
                Edit event
            </BaseButton>
        </div>

        <!-- Breadcrumb -->
        <div class="event-detail__breadcrumb">
            <BaseBackButton :to="'/events'">Events</BaseBackButton>
            <span class="event-detail__crump-sep">/</span>
            <span class="event-detail__crump-current">{{ event.name }}</span>
        </div>

        <div class="event-detail__grid">
            <!-- ================ MAIN COLUMN ===================== -->
             <div class="event-detail__main">

                <!-- Hero -->
                <BaseCard class="event-detail__hero" flush>
                    <template #media>
                        <BaseImage
                            :src="event.imageUrl ?? '/default-event.png'"
                            :alt="event.name"
                            height="320px"
                            fit="cover"
                        />

                        <div class="event-detail__hero-gradient" />

                        <div class="event-detail__hero-badges">
                            <BaseBadge :variant="statusColor(event.eventStatus)" tone="tonal" size="small">
                                {{ event.eventStatus }}
                            </BaseBadge>

                            <BaseBadge variant="default" tone="tonal" size="small">
                                {{ isOnline ? 'Online' : 'In-Person' }}
                            </BaseBadge>
                        </div>

                        <div class="event-detail__hero-title">
                            <span class="event-detail__hero-eyebrow">{{ event.visibility }} event</span>
                            <h1>{{ event.name }}</h1>
                        </div>
                    </template>
                </BaseCard>

                <!-- Description -->
                <BaseCard class="event-detail__section">
                    <p class="event-detail__description">
                        {{ event.description || 'No description provided.' }}
                    </p>
                </BaseCard>

                <!-- Particulars -->
                <BaseCard class="event-detail__section">
                    <h2 class="event-detail__section-title">Event particulars</h2>

                    <div class="event-detail__particulars">
                        <div class="event-detail__particular">
                            <div class="event-detail__particular-icon">
                                <v-icon size="20">mdi-calendar</v-icon>
                            </div>

                            <div class="event-detail__particular-body">
                                <p class="event-detail__particular-label">Date &amp; time</p>
                                <p class="event-detail__particular-value">{{ event.date }}</p>
                                <p class="event-detail__particular-meta">{{ event.startTime }} - {{ event.endTime }}</p>
                            </div>
                        </div>

                        <div class="event-detail__particular">
                            <div class="event-detail__particular-icon">
                                <v-icon size="20">mdi-map-marker</v-icon>
                            </div>

                            <div class="event-detail__particular-body">
                                <p class="event-detail__particular-label">Location</p>
                                <p class="event-detail__particular-value">{{ event.location }}</p>
                            </div>
                        </div>

                        <div class="event-detail__particular">
                            <BaseAvatar size="smd" :name="event.host?.username" />
                            
                            <div class="event-detail__particular-body">
                                <p class="event-detail__particular-label">Host &amp; organiser</p>
                                <p class="event-detail__particular-value">{{ event.host?.username ?? 'unknown' }}</p>
                            </div>
                        </div>

                        <div class="event-detail__particular">
                            <div class="event-detail__particular-icon">
                                <v-icon size="20">mdi-account-group</v-icon>
                            </div>

                            <div class="event-detail__particular-body">
                                <p class="event-detail__particular-label">Attendance</p>
                                <p class="event-detail__particular-value">{{ event.attendeeCount }} attending</p>

                                <div v-if="capacity" class="event-detail__capacity-track">
                                    <div class="event-detail__capacity-fill" :style="{ width: capacityPercent + '%'}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </BaseCard>

                <!-- Games -->
                <BaseCard v-if="event.games?.length" class="event-detail__section">
                    <h2 class="event-detail__section-title">Games on the table</h2>

                    <div class="event-detail__games">
                        <BaseCard v-for="game in event.games" :key="game.id" class="event-detail__game-card" flush>
                            <template #media>
                                <BaseImage :src="game.imageUrl" :alt="game.title" height="120px" />
                            </template>

                            <p class="card-title">{{ game.title }}</p>
                            <p class="card-meta">{{ game.genres?.join(', ') }}</p>
                        </BaseCard>
                    </div>
                </BaseCard>
             </div>

            <!-- ===================== SIDEBAR ========================= -->
            <aside class="event-detail__sidebar">
                <BaseCard class="event-detail__rsvp-card">
                    <template v-if="!event.isHost">
                        <div class="event-detail__rsvp-header">
                            <span class="event-detail__particular-label">Your RSVP</span>
                            <BaseBadge :variant="rsvpColor(event.rsvpStatus)" tone="tonal" size="small">
                                {{ event.rsvpStatus }}
                            </BaseBadge>
                        </div>

                        <BaseButton 
                            v-if="event.rsvpStatus !== 'ATTENDING'"
                            block
                            :disbaled="event.eventStatus !== 'OPEN'"
                            @click="$emit('rsvp', event.id)"
                        >
                            <v-icon start>mdi-calendar-check</v-icon>
                            RSVP to event
                        </BaseButton>

                        <BaseButton v-else block variant="secondary" @click="$emit('de-rsvp', event.id)">
                            <v-icon start>mdi-calendar-remove</v-icon>
                            Cancel RSVP
                        </BaseButton>
                    </template>

                    <template v-else>
                        <p class="event-detail__section-title">Host controls</p>

                        <BaseButton block variant="secondary" @click="$emit('edit', event)">
                            <v-icon start>mdi-pencil</v-icon>
                            Edit event
                        </BaseButton>

                        <BaseButton block variant="error" @click="$emit('cancel-event', event.id)">
                            <v-icon start>mdi-cancel</v-icon>
                            Cancel event
                        </BaseButton>
                    </template>
                </BaseCard>

                <BaseCard class="event-detail__invite-card">
                    <p class="event-detail__section-title">Invite friends</p>
                    <p class="card-meta">Share this event with your tabletop friends.</p>
                    <BaseButton block variant="secondary">
                        <v-icon start>mdi-share-variant</v-icon>
                        Share event
                    </BaseButton>
                </BaseCard>

                <BaseCard class="event-detail__venue-card">
                    <p class="event-detail__section-title">Venue &amp; arrival</p>
                    <div class="event-detail__venue-map">
                        <v-icon size="28" color="var(--color-primary)">mdi-map-marker</v-icon>
                        <p class="card-meta">{{ event.location }}</p>
                    </div>
                </BaseCard>

            </aside>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseBackButton from '~/components/ui/BaseBackButton.vue'

const props = defineProps({
    event: {
        type: Object, 
        required: true
    }
})

defineEmits(['close', 'rsvp', 'de-rsvp', 'edit', 'cancel-event'])

const isOnline = computed(() => props.event.location?.toLowerCase() === 'online')
const capacity = computed(() => props.event.capacity ?? null)
const capacityPercent = computed(() => {
    if(!capacity.value) return 0
    return Math.min(100, Math.round((props.event.attendeeCount / capacity.value) * 100))
})
// const isHost = computed(() => props.event.host.username === props.currentUser)

const statusColor = (status) => {
  if (status === 'OPEN')         return 'success'
  if (status === 'FULLY_BOOKED') return 'warning'
  if (status === 'CANCELLED')    return 'error'
  return 'neutral'
}

const rsvpColor = (status) => {
  if (status === 'ATTENDING') return 'success'
  if (status === 'INVITED') return 'warning'
  if (status === 'NOT_ATTENDING') return 'error'
  return 'neutral'
}
</script>