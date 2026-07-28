<template> 
    <PageContainer>
        <Navbar />

        <div v-if="isLoading" class="d-flex justify-center pa-16">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <EventDetailPage
            v-else-if="event"
            :event="event"
            @rsvp="handleRsvp"
            @de-rsvp="handleDeRsvp"
            @edit="showEditModal = true"
            @cancel-event="handleCancelEvent"
        />        

        <div v-else class="pa-16 text center">
            <p class="mb-4">Event not found.</p>
            <BaseButton @click="router.push('/events')">Back to events</BaseButton>
        </div>

        <EditEventModal
            v-model="showEditModal"
            :event="event"
            @saved="handleEventUpdated"
        />

    </PageContainer>
</template>

<script setup>
definePageMeta({ middleware: 'auth' })

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import EventDetailPage from '~/components/features/events/EventDetailPage.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import EditEventModal from '~/components/features/events/EditEventModal.vue'

import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'
// import { useProfile } from '~/composables/useProfile'

const { show } = useSnackBar(3)
const route = useRoute()
const router = useRouter()

const { 
    rsvpToEvent,
    fetchEventbyId,
    deRsvpToEvent,
    cancelEvent,
    isLoading
} = useEvents()

const event = ref(null)
const showEditModal = ref(false)

onMounted(async () => {
    if(!localStorage.getItem('access_token')) {
        router.push('/auth/signin')
        return
    }
    console.log(route.params.id)
    const fetched = await fetchEventbyId(route.params.id);
    console.log(fetched)

    event.value = fetched
})

const handleRsvp = async () => {
    try {
        event.value = await rsvpToEvent(event.value.id)
        show('RSVP successful!', 'success')
    } catch {
        show('Failed to RSVP. Please try again.', 'error')
    }
}

const handleDeRsvp = async () => {
    try {
        event.value = await deRsvpToEvent(event.value.id)
        show('RSVP cancelled', 'info')
    } catch {
        show('Failed to cancel RSVP', 'error')
    }
}

const handleCancelEvent = async () => {
    try {
        event.value = await cancelEvent(event.value.id)
        show('Event cancelled', 'success')
        router.push('/events')
    } catch {
        show('Failed to cancel event.', 'error')
    }
}

const handleEventUpdated = async (updatedEvent) => {
    try {
        // await fetchEvents();
        event.value = updatedEvent
        show('Event updated!', 'success');
    } catch {
        show('Failed to refresh event details.', 'error');
    } finally {
        showEditModal.value = false;
    }
}
</script>