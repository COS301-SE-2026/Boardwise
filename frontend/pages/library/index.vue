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

    <RulebookCarousel :rulebooks="featuredRulebooks" @select="openRulebook" />

    <v-container v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 60vh">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-container>

    <RecommendedBooks v-else :rulebooks="recommended" @select ="openRulebook"/>

    <SectionTitle
        title="All Rulebooks"
        class="mt-8"
    />

    <!-- Mobile filter -->
    <div class="d-flex d-md-none mt-4 mb-4">
      <v-chip
        color="secondary"
        prepend-icon="mdi-filter-variant"
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
        
        <RulebookFilterSidebar
          @filter="handleFilter"
        />
        
      </v-navigation-drawer>
    </div>

    <!-- Desktop -->
    <div class="d-none d-md-flex ga-6 align-start">

      <!-- Filters -->
      <RulebookFilterSidebar
        @filter="handleFilter"
      />
      
      <div class="flex-grow-1" style="min-width: 0;">
        <v-container 
          v-if="isLoading"
          class="d-flex justify-center align-center"
          style="min-height: 60vh"
        >
          <v-progress-circular
            indeterminate
            color="primary"
            size="48"
          />
        </v-container>

        <RulebookGrid
          v-else
          :rulebooks="rulebooks"
          @select="openRulebook"
        />
      </div>
    </div>

  <div class="d-md-none">
    <v-container 
      v-if="isLoading"
      class="d-flex justify-center align-center"
      style="min-height: 60vh"
    >
      <div v-if="isLoading" class="d-flex justify-center align-center h-100">
        <v-progress-circular indeterminate color="primary"/>
      </div>
    </v-container>

    <RulebookGrid
      v-else
      :rulebooks="rulebooks"
      @select="openRulebook"
    />
  </div>

  <div 
    ref="sentinel"
    style="height: 1px"
  />

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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useIntersectionObserver, useDebounceFn } from '@vueuse/core'

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
const showFilters = ref(false)

const route = useRoute();
const router = useRouter();

const {rulebooks, isLoading, getAllRulebooks, getRulebookById, currentRulebook, featuredRulebooks, loadMore, hasMore, fetchFeaturedRulebooks } = useLibrary()
const {triggerUpload, isUploading, error} = useVaultUpload();
const { isAuthenticated } = useAuth();

const searchQuery = ref('')
const activeFilterState = ref({})
const showDetail = ref(false)
const showUpload = ref(false)
const selectedRulebook = ref(null)
const sentinel = ref(null)

onMounted(() => { // Does stuff when component loads
  fetchFeaturedRulebooks();
  getAllRulebooks({}, true);
})

useIntersectionObserver(sentinel,([entry])=>{
  if(entry.isIntersecting&& hasMore.value && !isLoading.value){
    loadMore();
  }
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

const delaySearch = useDebounceFn((query) => {
  getAllRulebooks({...activeFilterState.value, search:query || null}, true);
}, 400);

watch(searchQuery, (query) => {
  delaySearch(query);
});


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
  activeFilterState.value = {
    genre: filters.genre,
    languages: filters.languages,
    playerCount: filters.playerCount,
    duration: filters.duration,
    minAge: filters.minAge,
  }
    getAllRulebooks({...activeFilterState.value, search: searchQuery.value || null}, true);
    
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

const recommended = computed(() => {
  return featuredRulebooks.value.slice(0, 5);
})
</script>