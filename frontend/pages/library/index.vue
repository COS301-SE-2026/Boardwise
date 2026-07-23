<template>
  <PageContainer>
    <Navbar />

    <div class="d-flex flex-column ga-5 mb-6">
      <SectionTitle title="Library" subtitle="Browse community rulebooks" />
      <RulebookSearch
        @upload="handleUploadRequest"
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
    <div v-if="isLoading" class="d-flex justify-center align-center h-100">
      <v-progress-circular indeterminate color="primary"/>
    </div>

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
      :loading="isUploading"
      @add="handleUploadRulebook"
    />

  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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
import { useVaultUpload } from '~/composables/useVaultUpload';
import { useAuth } from '~/composables/useAuth';

import { useSnackBar } from '~/composables/useSnackbar';

const { show } = useSnackBar();

const route = useRoute();
const router = useRouter();

const {rulebooks, isLoading, getAllRulebooks, getRulebookById, currentRulebook } = useLibrary()
const {triggerUpload, isUploading, error} = useVaultUpload();
const { isAuthenticated } = useAuth();

const searchQuery = ref('')
const activeFilters = ref({})
const showDetail = ref(false)
const showUpload = ref(false)
const selectedRulebook = ref(null)

onMounted(() => { // Does stuff when component loads
  getAllRulebooks()
})

const handleUploadRequest = () => {
  if(!isAuthenticated.value){
    router.push({
      path: '/auth/signin',
      query: { redirect: route.fullPath }
    });
    return;
  }
  showUpload.value = true;
}

const recommended = computed(() => {
  return rulebooks.value.slice(0, 5);
})

const filteredRulebooks = computed(() =>{
  let result = rulebooks.value

  if(searchQuery.value){
    const lCaseQuery = searchQuery.value.toLowerCase()
    result = result.filter(r =>
      r.title?.toLowerCase().includes(lCaseQuery) ||
      (r.description && r.description.toLowerCase().includes(lCaseQuery))
    )
  }

  if (activeFilters.value.genre && activeFilters.value.genre !== 'All') {
    result = result.filter(r => r.genres && r.genres.includes(activeFilters.value.genre))
  }

  if (activeFilters.value.languages?.length) {
    result = result.filter(r => activeFilters.value.languages.includes(r.language))
  }

  if(activeFilters.value.playerCount){
    const target = Number(activeFilters.value.playerCount);
    result = result.filter(r => r.minPlayers <= target && r.maxPlayers >= target);
  }

  if(activeFilters.value.duration){
    result = result.filter(r => r.duration <= Number(activeFilters.value.duration));
  }
  
  if(activeFilters.value.minAge){
    result = result.filter(r => r.minAge <= Number(activeFilters.value.minAge));
  }

  return result
})


const openRulebook = async (rulebook) => {
  selectedRulebook.value = null;
  showDetail.value = true;
  await getRulebookById(rulebook.id);
  selectedRulebook.value = currentRulebook.value;
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleFilter = (filters) => {
  activeFilters.value = filters
}

const handleUploadRulebook = async (newRulebook) => {
  try{
    await triggerUpload(newRulebook);
    show("Rulebook uploaded successfully!", "success");
    showUpload.value = false;
  }catch(err){
    show(err.message || 'Failed to upload rulebook', 'error');
  }
}
</script>