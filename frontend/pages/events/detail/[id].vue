<template> 
    <PageContainer>
        <Navbar />

        <div v-if="loading" class="d-flex justify-center pa-16">
            <v-progress-circular indeterminate color="primary" />
        </div>

        <EventDetail 
            v-else-if="event"
            :event="event"
            :current-user="currentUsername"
            @rsvp="handleRsvp"
            @de-rsvp="handleDeRsvp"
            @edit="showEditEvent = true"
            @cancel-event="handleCancelEvent"
        />        

        <div v-else class="pa-16 text center">
            <p class="mb-4">Event not found.</p>
            <BaseButton @click="router.push('/events')">Back to events</BaseButton>
        </div>

        <CreateEvent v-model="showEditEvent" :initial-data="event" @created="handleEditEvent" />
    </PageContainer>
</template>

<script>
definePageMeta({ middleware: 'auth' })

import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import EventDetailPage from '~/components/features/events/EventDetailPage.vue'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import CreateEvent from '~/components/features/events/CreateEvent.vue'

import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'
import { useProfile } from '~/composables/useProfile'

const { show } = useSnackBar
const route = useRoute()
const router = useRouter()
const { fetchCurrentUser } = useProfile()

const { 
    events, 
    fetchEventbyId,
    rsvpToEvent,
    deRsvpFromEvent,
    updateEvent,
    cancelEvent
} = useEvents()

const loading = ref(true)
const event = ref(null)
const showEditEvent = ref(false)
const currentUsername = ref(null)

onMounted(async () => {
    if(!localStorage.getItem('access_token')) {
        router.push('/auth/signin')
        return
    }

    const userDetails = await fetchCurrentUser()
    currentUsername.value = userDetails.username

    const existing = events.value.find(e => e.id === route.params.id)
    event.value = existing ?? await fetchEventbyId(route.params.id)
    loading.value = false
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
        event.value = await deRsvpFromEvent(event.value.id)
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

</script>