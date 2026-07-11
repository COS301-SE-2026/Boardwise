<template>
        <BaseEmptyState
            v-if="communityEvents.length === 0"
            title="No upcoming events"
            description="This community hasn't scheduled any events yet."
    />
    
        <BaseGrid
            v-else
            class="320px "
        >

        <EventCard
            v-for="event in communityEvents"
            :key="event.id"
            :event="event"
        />
    </BaseGrid> 
</template>

<script setup>

import {computed } from 'vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';
import { events } from '~/services/mockData/events';

import EventCard from '../events/EventCard.vue';
import BaseGrid from '~/components/ui/BaseGrid.vue';

    defineProps({
        community: {
            type: Object,
            required: true
        }
    })

    const communityEvents = computed(() =>
        events.filter(
            event => event.communityId === props.community.id
        )
    )
</script>
