<template>
  <PageContainer>
    <Navbar />
    
    <div class="d-flex flex-column ga-5 mb-6">
      <SectionTitle 
        title="Events" 
        subtitle="Discover and join gaming events near you" 
      />

      <EventSearch
        @search="handleSearch"
        @create-event="showCreateEvent = true"
      />
    </div>
    
    <div class="d-flex ga-6 align-start">

      <EventFilter 
        :events="events" 
        @filter="handleFilter" 
      />

      <EventGrid 
        :events="filteredEvents" 
        @select="openEvent" 
        class="flex-1-1" 
      />
    </div>

    <CreateEvent v-model="showCreateEvent" @created="handleCreateEvent" />

    <EditEventModal
      v-model="showEditEvent"
      :event="editingEvent"
      @saved="handleEventUpdated"
    />

    <InviteModal
      v-model="showInviteModal"
      :event="createdEvent"
    />

  </PageContainer>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

import EventSearch from '~/components/features/events/EventSearch.vue'
import EventFilter from '~/components/features/events/EventFilter.vue'
import EventGrid from '~/components/features/events/EventGrid.vue'


import CreateEvent from '~/components/features/events/CreateEvent.vue'

import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'
import { useProfile } from '~/composables/useProfile'

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import EditEventModal from '~/components/features/events/EditEventModal.vue'
import InviteModal from '~/components/features/community/InviteModal.vue'
const { show } = useSnackBar(3)

const { fetchCurrentUser } = useProfile()

const {
  events, 
  isLoading, 
  fetchEvents,
  createEvent,
  rsvpToEvent, 
  deRsvpFromEvent,
  cancelEvent
} = useEvents()

const router = useRouter()

onMounted(async () => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
    return
  }

  const userDetails = await fetchCurrentUser()
  currentUsername.value = userDetails.username
  fetchEvents()
})


const searchQuery = ref('')
const activeFilters = ref({})

const showCreateEvent = ref(false)
const showDetail = ref(false)
const showEditEvent = ref(false)
const selectedEvent = ref(null)
const editingEvent = ref(null)

const currentUsername = ref(null)

const filteredEvents = computed(() => {
  let result = events.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()

    result = result.filter(e =>
      e.name.toLowerCase().includes(query) ||
      e.games.some(g => g.title.toLowerCase().includes(query))
    )
  }

  if (activeFilters.value.date && activeFilters.value.date !== 'All') {
    const now = new Date()
    result = result.filter(e => {
      const eventDate = new Date(e.startTime)
      if (activeFilters.value.date === 'Today') {
        return eventDate.toDateString() === now.toDateString()
      }

      if (activeFilters.value.date === 'This Week') {
        const weekFromNow = new Date(now)
        weekFromNow.setDate(now.getDate() + 7)
        return eventDate >= now && eventDate <= weekFromNow
      }
      if (activeFilters.value.date === 'This Month') {
        return eventDate.getMonth() === now.getMonth() && eventDate.getFullYear() === now.getFullYear()
      }
      return true
    })
  }


  if (activeFilters.value.games?.length) {
    result = result.filter(e =>
      e.games.some(g => activeFilters.value.games.includes(g.title))
    )
  }

  if (activeFilters.value.online && !activeFilters.value.inPerson) {
    result = result.filter(e => e.location.toLowerCase() === 'online')
  }

  if (activeFilters.value.inPerson && !activeFilters.value.online) {
    result = result.filter(e => e.location.toLowerCase() !== 'online')
  }

  return result
})

const showInviteModal = ref(false);
const createdEvent = ref(null);

const openEvent = (event) => {
  router.push(`/events/detail/${event.id}`)
}

const openEdit = (event) => {
  editingEvent.value = event
  showEditEvent.value = true
  showDetail.value = false
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleFilter = (filters) => {
  activeFilters.value = filters
}

const handleRsvp = async (eventId) => {
  try {
    const updated = await rsvpToEvent(eventId)
    selectedEvent.value = updated
    show('RSVP successful!', 'success')
  } catch {
    show('Failed to RSVP. Please try again.', 'error')
  }
}

const handleDeRsvp = async (eventId) => {
  try {
    const updated = await deRsvpFromEvent(eventId)
    selectedEvent.value = updated
    show('RSVP cancelled', 'info')
  } catch {
    show('Failed to cancel RSVP.', 'error')
  }
}

const handleCancelEvent = async (eventId) => {
  try {
    await cancelEvent(eventId)
    showDetail.value = false
    show('Event cancelled', 'success')
  } catch {
    show('Failed to cancel event.', 'error')
  }
}

const handleCreateEvent = async ({ eventInfo, image }) => {
  const event = await createEvent(eventInfo, image)
  show('Event created!', 'success')
  createdEvent.value = event      
  showCreateEvent.value = false   
  showInviteModal.value = true   
  return event;
}

const handleEventCreated = (event) => {
  createdEvent.value = event
  showInviteModal.value = true
}

const handleEventUpdated = async () => {

  await fetchEvents();

  if (editingEvent.value) {
    selectedEvent.value = events.value.find(
      e => e.id === editingEvent.value.id
    )
  }



  show('Event updated!', 'success')

  showEditEvent.value = false
  showDetail.value = true
  editingEvent.value = null



}






</script>