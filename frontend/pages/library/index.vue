<template>
  <PageContainer>
    <Navbar />

    <div class="d-flex flex-column ga-5 mb-6">
      <SectionTitle title="Library" subtitle="Browse community rulebooks" />
      <RulebookSearch
        @upload="showUpload = true"
        @search="handleSearch"
      />
    </div>

  <RulebookCarousel :rulebooks="rulebooks" @select="openRulebook" />

  <RecommendedBooks :rulebooks="recommended" @select ="openRulebook"/>

  <SectionTitle title="All Rulebooks" class="mt-8" />

    <div class="d-flex ga-6 align-start">
    <RulebookFilterSidebar :rulebooks="rulebooks" @filter="handleFilter" />
    <RulebookGrid :rulebooks="filteredRulebooks" @select="openRulebook" class="flex-1-1" />
  </div>

  <v-navigation-drawer v-model="showDetail" location="right" temporary width="480">
    <RulebookDetail
      v-if="selectedRulebook"
      :rulebook="selectedRulebook"
      :rulebooks="rulebooks"
      @select="openRulebook"
      @close="showDetail = false"
    />

  </v-navigation-drawer>

    <UploadRulebookModal
      v-model="showUpload"
      @add="handleUploadRulebook"
    />

  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

import RulebookFilterSidebar from '~/components/features/library/RulebookFilterSidebar.vue'
import RulebookGrid from '~/components/features/library/RulebookGrid.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'
import RulebookSearch from '~/components/features/library/RulebookSearch.vue'
import UploadRulebookModal from '~/components/features/library/UploadRulebookModal.vue'
import RulebookDetail from '~/components/features/library/RulebookDetail.vue'
import RulebookCarousel from '~/components/features/library/RulebookCarousel.vue'

import { useLibrary } from '~/composables/useLibrary'

const {rulebooks, isLoading, fetchAllRulebooks} = useLibrary()

const searchQuery = ref('')
const activeFilters = ref({})
const showDetail = ref(false)
const showUpload = ref(false)
const selectedRulebook = ref(null)

onMounted(() => { // Does stuff when component loads
  fetchAllRulebooks()
})

const recommended = computed(() => {
  return rulebooks.value.slice(0, 5);
})

const filteredRulebooks = computed(() =>{
  let result = rulebooks.value

  if(searchQuery.value){
    const lCaseQuery = searchQuery.value.toLowerCase()
    result = result.filter(r =>
      r.title.toLowerCase().includes(lCaseQuery) ||
      (r.description && r.description.toLowerCase().includes(lCaseQuery))
    )
  }

  if (activeFilters.value.genre && activeFilters.value.genre !== 'All') {
    result = result.filter(r => r.genre === activeFilters.value.genre)
  }

  if (activeFilters.value.languages?.length) {
    result = result.filter(r => activeFilters.value.languages.includes(r.language))
  }

  if (activeFilters.value.minPlayers) {
    result = result.filter(r => r.minPlayers === Number(activeFilters.value.minPlayers))
  }

  if (activeFilters.value.maxPlayers) {
    result = result.filter(r => r.maxPlayers === Number(activeFilters.value.maxPlayers))
  }

  return result
})


const openRulebook = (rulebook) => {
  selectedRulebook.value = rulebook
  showDetail.value = true
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleFilter = (filters) => {
  activeFilters.value = filters
}

const handleUploadRulebook = (newRulebook) => {
  console.log("New rulebook: ", newRulebook)
}
</script>