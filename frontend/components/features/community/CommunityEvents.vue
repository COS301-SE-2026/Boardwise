<template>
    <div class="d-flex flex-column ga-4">

        <div
            v-if="communityEvents.length"
            class="d-flex flex-column ga-4" 
        >

        <EventReminder
            v-for="event in communityEvents"
            :key="event.id"
            :event="event"
            @toggle-rsvp="toggleRsvp"
        />
        </div>

        <BaseEmptyState
            v-if="communityEvents.length === 0"
            title="No upcoming events"
            description="This community hasn't scheduled any events yet."
    />
        
        <BaseButton
            v-if="community.isMember"
            @click="showCreateModal = true"
        >
            Create Event
        </BaseButton>

        <CreateEventModal
            v-model="showCreateModal"
            @create="createEvent"
        />
</div>
</template>

<script setup>

import {computed , ref} from 'vue'

import BaseEmptyState from '~/components/ui/BaseEmptyState.vue';
import { events } from '~/services/mockData/events';
import EventReminder from './EventReminder.vue';
import CreateEventModal from './CreateEventModal.vue';
import createEvent from '../events/CreateEvent.vue';
import BaseButton from '~/components/ui/BaseButton.vue';

    const props = defineProps({
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

    const showCreateModal = ref(false)

    const toggleRsvp = (eventId) => {
        const event = events.find(e.id === eventId)
        if (event){
            event.rsvped = !event.rsvped
        }
    }

</script>
