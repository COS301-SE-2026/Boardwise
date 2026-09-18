<template>
  <PageContainer>
    <Navbar />

    <EventHeader 
      @search="searchQuery = $event"
      @create-event="showCreateEvent = true"  
    />
    
    <!-- Mobile -->
    <div class="d-flex d-md-none mt-6 mb-4">
      <v-chip 
        color="secondary"
        prepend-icon="mdi-filter-variant"
        :aria-expanded="showFilters"
        aria-controls="event-mobile-filters"
        size="large"
        @click="showFilters = true"
      >
        Filters
      </v-chip>

      <v-navigation-drawer
        v-model="showFilters"
        temporary
        location="left"
        width="300"
      >
        <EventFilter 
          :events="events" 
          @filter="handleFilter" 
        />
      </v-navigation-drawer>
    </div>

    <div class="d-md-none">
      <BaseLoadingState v-if="isLoading" />

      <template v-else>
        <EventGrid 
          :events="pagedEvents"
          @select="openEvent"
        />

        <template v-if="filteredEvents.length > 0">
          <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
            <span class="card-meta">Page {{ eventsPage }} of {{ eventsTotalPages }}</span>
          </div>
          
          <BasePagination
            v-if="eventsTotalPages > 1"
            class="mt-4"
            :model-value="eventsPage"
            :total-pages="eventsTotalPages"
            @update:modelValue="goToPage"
          />
        </template>
      </template>
      
    </div>

    <!-- Desktop -->
    <div class="d-none d-md-flex ga-6 mt-6 align-start">
      <EventFilter 
        :events="events" 
        @filter="handleFilter" 
      />
    
      <div class="flex-grow-1" style="min-width: 0;">
        <BaseLoadingState v-if="isLoading" />

        <template v-else>
          <BaseEmptyState
            v-if="filteredEvents.length === 0"
            title="No events found"
            message="Try adjusting your filters, or be the first to create one."
          />

          <template v-else>
            <EventGrid 
              :events="pagedEvents" 
              @select="openEvent" 
            />

            <template v-if="filteredEvents.length > 0">
              <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
                <span class="card-meta">Page {{ eventsPage }} of {{ eventsTotalPages }}</span>
              </div>

              <BasePagination
                v-if="eventsTotalPages > 1"
                class="mt-4"
                :model-value="eventsPage"
                :total-pages="eventsTotalPages"
                @update:modelValue="goToPage"
              />
            </template>
          </template>
        </template>
      </div>
    </div>

    <CreateEvent v-model="showCreateEvent"   :on-submit="handleCreateEvent"  @created="handleCreateEvent" />

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
import BasePagination from '~/components/ui/BasePagination.vue'

import EventFilter from '~/components/features/events/EventFilter.vue'
import EventGrid from '~/components/features/events/EventGrid.vue'
import CreateEvent from '~/components/features/events/CreateEvent.vue'
import { useEvents } from '~/composables/useEvents'
import { useSnackBar } from '~/composables/useSnackbar'
import { ref, computed, onMounted, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useRouter } from 'vue-router'
import EditEventModal from '~/components/features/events/EditEventModal.vue'
import InviteModal from '~/components/features/community/InviteModal.vue'
import EventHeader from '~/components/features/events/EventHeader.vue'
import BaseLoadingState from '~/components/ui/BaseLoadingState.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

const showFilters = ref(false)
const { show } = useSnackBar(3)
const {
  events, 
  isLoading, 
  fetchEvents,
  createEvent,
  rsvpToEvent, 
  deRsvpToEvent,
  cancelEvent
} = useEvents()

const router = useRouter()

onMounted(async () => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
    return
  }

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

// ============================== Pagination ==========================================

const EVENTS_PAGE_SIZE = 6
const eventsPage = ref(1)

const eventsTotalPages = computed(() => 
  Math.max(1, Math.ceil(filteredEvents.value.length /EVENTS_PAGE_SIZE))
)

const pagedEvents = computed(() => {
  const start = (eventsPage.value - 1) * EVENTS_PAGE_SIZE
  return filteredEvents.value.slice(start, start + EVENTS_PAGE_SIZE)
})

const goToPage = async (pageNum) => {
  eventsPage.value = pageNum
}

watch(filteredEvents, () => {
  if (eventsPage.value > eventsTotalPages.value) {
    eventsPage.value = 1
  }
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

const handleFilter = (filters) => {
  activeFilters.value = filters
  eventsPage.value = 1
}

const handleRsvp = async (eventId) => {
  try {
    const updated = await rsvpToEvent(eventId)
    selectedEvent.value = updated
    show('Your seat is saved for game night.', 'success')
  } catch {
    show('Failed to RSVP. Please try again.', 'error')
  }
}

const handleDeRsvp = async (eventId) => {
  try {
    const updated = await deRsvpToEvent(eventId)
    selectedEvent.value = updated
    show('Your seat has been opened up.', 'info')
  } catch {
    show('Failed to cancel RSVP.', 'error')
  }
}

const handleCancelEvent = async (eventId) => {
  try {
    await cancelEvent(eventId)
    showDetail.value = false
    show('The event has been packed away.', 'success')
  } catch {
    show('Failed to cancel event.', 'error')
  }
}

const handleCreateEvent = async ({ eventInfo, image }) => {
  const event = await createEvent(eventInfo, image)
  show('Your event is ready. Game on!', 'success')
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
  show('Your event changes are locked in.', 'success')

  showEditEvent.value = false
  showDetail.value = true
  editingEvent.value = null
}

const delaySearch = useDebounceFn(async (query) => {
  await fetchEvents(query)
}, 400)

watch(searchQuery, (query) => {
  delaySearch(query)
})

</script>