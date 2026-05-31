<template>
  <PageContainer>
    <Navbar />

    <div class="d-flex flex-column ga-5 mb-6">
      <SectionTitle title="Library" subtitle="Browse community rulebooks" />
      <RulebookSearch
        @upload="showUpload = true"
        @search="handleSearch"
      />
      <BaseTabs
        :tabs="tabs"
        :active-tab="selectedTab"
        @change="selectedTab = $event"
      />
    </div>

    <RecommendedBooks :rulebooks="recommended" />

    <SectionTitle title="All Rulebooks" class="mt-8" />

    <RulebookGrid :rulebooks="filteredRulebooks" @select="openRulebook" />

    <BaseModal v-model="showModal">
      <RulebookDetails v-if="selectedRulebook" :rulebook="selectedRulebook" />
    </BaseModal>

    <UploadRulebookModal
      v-model="showUpload"
      @add="handleUploadRulebook"
    />

  </PageContainer>
</template>

<script setup>


import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseTabs from '~/components/ui/BaseTabs.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseModal from '~/components/ui/BaseModal.vue'

import RulebookGrid from '~/components/features/library/RulebookGrid.vue'
import RecommendedBooks from '~/components/features/library/RecommendedBooks.vue'
import RulebookDetails from '~/components/features/library/RulebookDetail.vue'
import RulebookSearch from '~/components/features/library/RulebookSearch.vue'
import UploadRulebookModal from '~/components/features/library/UploadRulebookModal.vue'
import { useRouter } from 'vue-router'
import {ref, computed, onMounted} from 'vue'

import {useLibrary} from '~/composables/useLibrary'

const router = useRouter()

const {rulebooks, isLoading, fetchAllRulebooks} = useLibrary()

const tabs = ['All', 'Strategy', 'Family', 'Party']
const selectedTab = ref('All')
const searchQuery = ref('')
const showModal = ref(false)
const showUpload = ref(false)
const selectedRulebook = ref(null)

onMounted(() => { // Does stuff when component loads
  fetchAllRulebooks()
})

// const recommended = rulebooks.slice(0, 5)
const recommended = computed(() => {
  return rulebooks.value.slice(0, 5);
})

const filteredRulebooks = computed(() =>{
  let result = rulebooks.value

  if(selectedTab.value !== 'All'){
    result = rulebooks.filter(r => r.category === selectedTab.value)
  }

  if(searchQuery.value){
    const lCaseQuery = searchQuery.value.toLowerCase()
    result = result.filter(r =>
      r.title.toLowerCase().includes(lCaseQuery) ||
      (r.description && r.description.toLowerCase().includes(lCaseQuery))
    )
  }

  return result
})

const openRulebook = (rulebook) => {
  selectedRulebook.value = rulebook
  showModal.value = true
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleUploadRulebook = (newRulebook) => {
  console.log("New rulebook: ", newRulebook)
}
</script>