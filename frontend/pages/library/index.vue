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

    <BaseLoadingState v-if="isLoading" message="Fetching your library..." />

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
        
        <RulebookFilterSidebar/>
        
      </v-navigation-drawer>
    </div>

    <!-- Desktop -->
    <div class="d-none d-md-flex ga-6 align-start">

      <!-- Filters -->
      <RulebookFilterSidebar/>
      
      <div class="flex-grow-1" style="min-width: 0;">
        <BaseLoadingState v-if="isLoading" message="Loading rulebooks... " />
        <template v-else>
          <RulebookGrid
            :rulebooks="pagedRulebooks"
            @select="openRulebook"
          />

          <template v-if="rulebooks.length > 0">
            <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
              <span class="card-meta">
                Showing {{ rulebooksRangeStart }}-{{ rulebooksRangeEnd }}
                of {{ hasMore ? `${rulebooks.length}+` : rulebooks.length }} rulebooks
              </span>
            </div>

            <BasePagination 
              v-if="rulebooksTotalPages > 1"
              class="mt-4"
              :model-value="rulebooksPage"
              :total-pages="rulebooksTotalPages"
              @update:modelValue="goToRulebooksPage"
            />
          </template>
        </template>
      </div>
    </div>

  <div class="d-md-none">
    <BaseLoadingState v-if="isLoading" message="Fetching your library... " />

    <template v-else>
      <RulebookGrid
        :rulebooks="pagedRulebooks"
        @select="openRulebook"
      />

      <template v-if="rulebooks.length > 0">
        <div class="d-flex justify-space-between align-center mt-6 flex-wrap ga-4">
          <span class="card-meta">
            Showing {{ rulebooksRangeStart }}-{{ rulebooksRangeEnd }}
            of {{ hasMore ? `${rulebooks.length}+` : rulebooks.length }} rulebooks
          </span>
        </div>

        <BasePagination 
          v-if="rulebooksTotalPages > 1"
          class="mt-4"
          :model-value="rulebooksPage"
          :total-pages="rulebooksTotalPages"
          @update:modelValue="goToRulebooksPage"
        />
      </template>
    </template>
  </div>

  <v-navigation-drawer v-model="showDetail" location="right" temporary width="480">
    
    <BaseLoadingState v-if="isLoading" message="Loading rulebooks... " />

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
import { useDebounceFn } from '@vueuse/core'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import BasePagination from '~/components/ui/BasePagination.vue'

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

const CARD_PAGE_SIZE = 12

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

onMounted(() => { // Does stuff when component loads
  fetchFeaturedRulebooks();
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
  rulebooksPage.value = 1
  getAllRulebooks({...activeFilterState.value, search:query || null, limit: CARD_PAGE_SIZE}, true);
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

// ================= Pagination ======================
const rulebooksPage = ref(1)

const rulebooksTotalPages = computed(() => {
  const loadedPages = Math.ceil((rulebooks.value?.length || 0) / CARD_PAGE_SIZE)
  return hasMore.value ? loadedPages + 1 : Math.max(loadedPages, 1)
})

const pagedRulebooks = computed(() => {
  const start = (rulebooksPage.value - 1) * CARD_PAGE_SIZE
  return rulebooks.value.slice(start, start + CARD_PAGE_SIZE)
})

const rulebooksRangeStart = computed(() => (rulebooks.value.length === 0 ? 0 : (rulebooksPage.value - 1) * CARD_PAGE_SIZE + 1))
const rulebooksRangeEnd = computed(() => (rulebooksPage.value - 1) * CARD_PAGE_SIZE + pagedRulebooks.value.length)

const goToRulebooksPage = async (page) => {
  rulebooksPage.value = page
  while (rulebooks.value.length < page * CARD_PAGE_SIZE && hasMore.value && !isLoading.value) {
    await loadMore()
  }
}

const { filters } = useRulebookFilters();

watch(
  filters,
  (newFilters) => {
    const currentGenre = newFilters.genre[0]
    activeFilterState.value = {
      genre: currentGenre === 'all' ? null : currentGenre,
      playerCount: newFilters.playerCount,
      duration: newFilters.duration,
      minAge: newFilters.minAge
    }
    rulebooksPage.value = 1;
    getAllRulebooks({...activeFilterState.value, search: searchQuery.value || null, limit: CARD_PAGE_SIZE}, true);
  },{deep: true, immediate: true}
)
</script>