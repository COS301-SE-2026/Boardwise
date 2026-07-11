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

    <v-navigation-drawer v-model="showDetail" location="right" temporary width="480">
      <EventDetail
        v-if="selectedEvent"
        :event="selectedEvent"
        :current-user="currentUsername"
        @close="showDetail = false"
        @rsvp="handleRsvp"
        @de-rsvp="handleDeRsvp"
        @edit="openEdit"
        @cancel-event="handleCancelEvent"
      />
    </v-navigation-drawer>

    <CreateEvent v-model="showCreateEvent" @created="handleCreateEvent" />

    <CreateEvent v-model="showEditEvent" :initial-data="editingEvent" @created="handleEditEvent" />

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

import EventDetail from '~/components/features/events/EventDetail.vue'
import CreateEvent from '~/components/features/events/CreateEvent.vue'

import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'
import { useProfile } from '~/composables/useProfile'

const { show } = useSnackBar

const { fetchCurrentUser } = useProfile()

const {
  events, 
  isLoading, 
  fetchEvents,
  createEvent,
  updateEvent, 
  rsvpToEvent, 
  deRsvpFromEvent,
  cancelEvent
} = useEvents()

const router = useRouter()

onMounted(async () => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
  }

  let userDetails = await fetchCurrentUser();
  currentUsername.value = userDetails.username;
  fetchEvents()
})

import { onMounted } from 'vue';
import { useRouter } from 'vue-router'
import { userService } from '~/services/userService'

const searchQuery = ref('')
const activeFilters = ref({})

const showCreateEvent = ref(false)
const showDetail = ref(false)
const showEditEvent = ref(false)
const selectedEvent = ref(null)
const editingEvent = ref(null)

const currentUsername = ref(null);

const filteredEvents = computed(() => {
  let result = events.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()

    result = result.filter(e =>
      e.name.toLowerCase().includes(query) ||
      e.game.some(g => g.title.toLowerCase().includes(q))
    )
  }

  if (activeFilters.value.date && activeFilters.value.date !== 'All') {
    const today = new Date().toISOString().split('T')[0]
    if (activeFilters.value.date === 'Today') {
      result = result.filter(e => e.date === today)
    }
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

const openEvent = (event) => {
  selectedEvent.value = event
  showDetail.value = true
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

const handleCreateEvent = async ({ eventInfo , image }) => {
  try {
    await createEvent(eventInfo, image)
    show('Event created!', 'success')
  } catch {
    show('Failed to create event.', 'error')
  }
}

const handleEditEvent = async ({ eventInfo , image }) => {
  if(!editingEvent.value) return
  
  try {
    await updateEvent(editingEvent.value.id, eventInfo, image)
    show('Event updated!', 'success')
  } catch {
    show('Failed to update event.', 'error')
  }
}

</script>