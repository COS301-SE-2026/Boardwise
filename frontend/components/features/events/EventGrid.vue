<template>
    <BaseGrid data-test="event-grid">
        <EventCard
            v-for="event in events"
            :key="event.id"
            :event="event"
            @click="$emit('select', event)"
        />
    </BaseGrid>
</template>

<script setup>
import EventCard from './EventCard.vue'
import BaseGrid from '~/components/ui/BaseGrid.vue'

defineProps({
    events: {
        type: Array,
        default: () => []
    }
})

defineEmits(['select'])
</script>