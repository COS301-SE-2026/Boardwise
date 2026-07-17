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

import { onMounted } from 'vue';
import { useRouter } from 'vue-router'

const searchQuery = ref('')
const activeFilters = ref({})
const showCreateEvent = ref(false)

const router = useRouter()
onMounted(() => {
  if (!localStorage.getItem('access_token')) {
    router.push('/auth/signin')
  }
})

// Mock data until backend exists
const events = ref([
  {
    id: 1,
    title: 'Catan Night',
    date: '12 July 2026',
    time: '18:00',
    location: 'Pretoria',
    game: 'Catan',
    host: 'Boardwise',
    attendanceType: 'In Person',
    image: '/default-event.png'
  },
  {
    id: 2,
    title: 'Chess Tournament',
    date: '18 July 2026',
    time: '10:00',
    location: 'Cape Town',
    game: 'Chess',
    host: 'Chess Club',
    attendanceType: 'In Person',
    image: '/default-event.png'
  },
  {
    id: 3,
    title: 'Online D&D Session',
    date: '20 July 2026',
    time: '19:00',
    location: 'Discord',
    game: 'D&D',
    host: 'Dungeon Masters',
    attendanceType: 'Online',
    image: '/default-event.png'
  }
])

const filteredEvents = computed(() => {
  let result = events.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()

    result = result.filter(event =>
      event.title.toLowerCase().includes(query) ||
      event.game.toLowerCase().includes(query)
    )
  }

  return result
})

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleFilter = (filters) => {
  activeFilters.value = filters
}

const openEvent = (event) => {
  console.log('Selected event:', event)
}
</script>