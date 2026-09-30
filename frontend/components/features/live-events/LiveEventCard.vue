<template>
    <BaseCard clickable class="live-event-card" @click="$emit('click', event)">
        <div class="live-event-card__top">
            <span class="live-event-card__pulse-badge">
                <span class="live-event-card__pulse-dot" />
                LIVE NOW
            </span>
            <BaseBadge variant="default" tone="tonal" size="x-small">{{ event.table }}</BaseBadge>
        </div>

        <p class="card-title">{{  event.name }}</p>
        <p class="card-meta"><v-icon size="16">mdi-map-marker</v-icon> {{ event.venue }}</p>
        <p class="card-meta"><v-icon size="16">mdi-account-group</v-icon> {{ seatedCount }}/{{ event.capacity }} seated</p>

        <div class="live-event-card__capacity-track">
            <div class="live-event-card__capacity-fill" :style="{ width: capacityPercent + '%'}" />
        </div>

        <BaseButton variant="accent" block size="sm" @click.stop="$emit('click', event)">
            <v-icon start size="16">mdi-meeting-room</v-icon>
             {{  hasOpenSeat ? 'Join Live Table' : 'View Table' }}
        </BaseButton>
    </BaseCard>
</template>

<script setup>
import { computed } from 'vue'

import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({ event: { type: Object, required: true }})
defineEmits(['click'])

const seatedCount = computed(() => props.event.seats.filter(s => s.status !== 'OPEN').length)
const capacityPercent = computed(() => Math.round((seatedCount.value / props.event.capacity) * 100))
const hasOpenSeat = computed(() => props.event.seats.some(s => s.status === 'OPEN'))
</script>