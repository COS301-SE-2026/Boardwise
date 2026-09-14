<template>
    <v-row class="mt-8">
        <v-col 
            v-for="event in events"
            :key="event.id"
            cols="12"
            sm="6"
            md="4"
        >
            <EventCard
                :event="event"
                class="h-100"
                @click="$emit('select', event)"
            />
        </v-col>
    </v-row>
</template>

<script setup>
import EventCard from './EventCard.vue'

defineProps({
    events: {
        type: Array,
        default: () => []
    }
})

defineEmits(['select'])
</script>