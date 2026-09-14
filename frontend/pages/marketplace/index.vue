<template>
  <PageContainer data-test="page-container">

    <Navbar data-test="navbar" />

    <MarketplaceHeader data-test="marketplace-header" @search ="searchQ = $event" @create-listing="showCreateListing = true" />

    <MarketplaceTabs data-test="marketplace-tabs" v-model="activeTab" />

    <!-- Community Listings -->
    <template v-if="activeTab === 'Community Listings'">
      <!-- Mobile -->
      <div class="d-flex d-md-none mt-6 mb-4">
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
          <FilterSidebar data-test="filter-sidebar" @filter="handleFilter" />
        </v-navigation-drawer>
      </div>

      <!-- Desktop -->
      <div class="d-flex d-md-flex ga-6 mt-6 align-start">
        <div class="d-none d-md-block">
          <FilterSidebar data-test="filter-sidebar" @filter="handleFilter"/>
        </div>
          
        <div class="flex-1-1">
          <v-container v-if="loading" class="d-flex justify-center align-center" style="min-height: 60vh">
            <MarketplaceLoadingState tab="Community Listings" />
          </v-container>

          <MarketplaceEmptyState
            v-else-if="listings.length === 0"
            tab="Community Listings"
            :search="searchQ"
            :has-active-filters="hasCommunityFilters"
            @clear-filters="resetCommunityFilters"
            @create-listing="showCreateListing = true"
          />

          <template v-else>
            <ListingGrid data-test="listing-grid" :listings="listings" />
          
            <p class="text-caption text-medium-emphasis mt-4">
              Showing {{ communityRangeStart }}-{{ communityRangeEnd }}
              of {{ hasMore ? `${listings.length}+` : listings.length }} marketplace listings
            </p>

            <BasePagination
              v-if="communityTotalPages > 1"
              class="mt-4"
              :model-value="communityPage"
              :total-pages="communityTotalPages"
              @update:modelValue="goToCommunityPage"
            />
          </template>
        </div>
      </div>
    </template>
    
    <!-- External Retail -->
    <template v-else-if="activeTab === 'Web'">
      <!-- Mobile -->
      <div class="d-flex d-md-none mt-6 mb-4">
          <v-chip
            color="secondary"
            prepend-icon="mdi-filter-variant"
            size="large"
            @click="showRetailFilters = true"
          >
            Filters
          </v-chip>

          <v-navigation-drawer
            v-model="showRetailFilters"
            temporary
            location="left"
            width="300"
          >
            <RetailerFilterSidebar 
              data-test="retailer-filter-sidebar"
              :retailer-options="retailerOptions"
              @filter="handleRetailerFilter" 
            />
          </v-navigation-drawer>
        </div>

        <!-- Desktop -->
        <div class="d-flex d-md-flex ga-6 mt-6 align-start">
          <div class="d-none d-md-block">
            <RetailerFilterSidebar 
              data-test="filter-sidebar" 
              :retailer-options="retailerOptions"
              @filter="handleRetailerFilter"
            />
          </div>
            
          <div class="flex-1-1">
            <v-container 
              v-if="retailLoading && retailResults.length === 0" 
              class="d-flex justify-center align-center flex-1-1"
              style="min-height: 60vh"
            >
              <MarketplaceLoadingState tab="Web" />

            </v-container>

            <MarketplaceEmptyState
              v-else-if="filteredRetailResults.length === 0"
              tab="Web"
              :search="searchQ"
              :has-active-filters="hasRetailFilters"
              @clear-filters="resetRetailFilters"
            />

            <template v-else>
              <RetailerGrid 
                data-test="retailer-grid" 
                :retailers="filteredRetailResults" 
              />

              <p class="text-caption text-medium-emphasis mt-4">
                Showing {{ retailRangeStart }}-{{ retailRangeEnd }}
                of {{ hasMoreRetail ? `${filteredRetailResults.length}+` : filteredRetailResults.length }} results
              </p>

              <BasePagination
                v-if="retailTotalPages > 1"
                class="mt-4"
                :model-value="retailPage"
                :total-pages="retailTotalPages"
                @update:modelValue="goToRetailPage"
              />
            </template>
          </div>
      </div>
    </template>

    <MarketplaceLoadingState
      v-if="showInlineLoading"
      :tab="activeTab"
      inline
    />

    <div ref="sentinel" style="height:1px" />

    <AddListingModal
      data-test="add-listing-modal"
      v-model="showCreateListing" 
      @confirm="handleAdd"
    />
    
  </PageContainer>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import { computed, unref } from 'vue'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BasePagination from '~/components/ui/BasePagination.vue'

import MarketplaceHeader from '~/components/features/marketplace/MarketplaceHeader.vue'
import MarketplaceTabs from '~/components/features/marketplace/MarketplaceTabs.vue'

import FilterSidebar from '~/components/features/marketplace/FilterSidebar.vue'
import RetailerFilterSidebar from '~/components/features/marketplace/RetailerFilterSidebar.vue'
import ListingGrid from '~/components/features/marketplace/ListingGrid.vue'
import RetailerGrid from '~/components/features/marketplace/RetailerGrid.vue'
import AddListingModal from '~/components/features/profile/AddListingModal.vue'

import MarketplaceLoadingState from '~/components/features/marketplace/MarketplaceLoadingState.vue'
import MarketplaceEmptyState from '~/components/features/marketplace/MarketplaceEmptyState.vue'

import { useRouter } from 'vue-router'
import { useMarketplace } from '~/composables/useMarketplace'
import { useIntersectionObserver, useDebounceFn  } from '@vueuse/core'
import { useRetail } from '~/composables/useRetail'

const PAGE_SIZE = 6

const router = useRouter();
const activeTab = ref('Community Listings')
const showFilters = ref(false)
const showRetailFilters = ref(false)
const showCreateListing = ref(false)

const {listings, loading, fetchListings, addListing, loadMore, hasMore} = useMarketplace();
const {retailResults, retailLoading, hasMoreRetail, fetchPersonalisedListings} = useRetail()

onMounted(async () => {
  if(!localStorage.getItem('access_token')){
    router.push('/auth/signin');
  }
  fetchListings({}, true)   
})

const handleAdd = async (data, image) => {
  await addListing(data, image);
  showCreateListing.value = false;
  fetchListings(activeFilterState.value, true);
}

const sentinel = ref(null)
useIntersectionObserver(sentinel, async ([entry])=>  {
  if(!entry.isIntersecting) return;

  if(activeTab.value === 'Web'){
    if(hasMoreRetail.value && !retailLoading.value) fetchPersonalisedListings();
    console.log("CUrrent retail results: ", retailResults.value );
    return
  }

  if(hasMore.value && !loading.value) loadMore()
})

const showInlineLoading = computed(() => {
  const currentListings = unref(listings) ?? []
  const currentRetail = unref(retailResults) ?? []

  if (activeTab.value === 'Web') {
    return retailLoading.value && currentRetail.length > 0
  }
  return loading.value && currentListings.length > 0
})

const searchQ = ref('');
const activeFilterState = ref({})
const activeRetailFilterState = ref({ retailers: null, minPrice: null, maxPrice: null }, true)

const delaySearch = useDebounceFn((query) => {
  if(activeTab.value === 'Web'){
    fetchPersonalisedListings(true);
    return
  }

  fetchListings({ ...activeFilterState.value, search: query || null }, true)
}, 400)

watch(activeTab, (tab) => {
  if(tab === 'Web' && retailResults.value.length === 0) {
    fetchPersonalisedListings(true);
  }
})

watch(searchQ,(query)=>{
  delaySearch(query);
})

  const getListingType = (rent, sale)=> { 
    if (rent && sale) return null;
    if (rent) return 'rental';
    if (sale) return 'sale';
    return null;
  }

  const handleFilter = (filters)=>{

  const conditions = filters.conditions.length > 0 ? filters.conditions.map(c => c.toLowerCase()) : null
  const genres = filters.genres?.length > 0 ? filters.genres : null

  const  lt= getListingType(filters.rent,filters.sale);

   activeFilterState.value = {
    listingType: lt,
    genres,
    conditions,
    minPrice: filters.minPrice || null,
    maxPrice: filters.maxPrice || null,
  }

  fetchListings({ ...activeFilterState.value, search: searchQ.value || null }, true);
}

const KNOWN_RETAILERS = ['Bobshop', 'Takealot', 'ToysRUs']

const retailerOptions = computed(() => {
  const loadedNames = retailResults.value.map(r => r.retailer).filter(Boolean)
  return [...new Set([...KNOWN_RETAILERS, ...loadedNames ])].sort()
})

const handleRetailerFilter = (filters) => {
  activeRetailFilterState.value = filters
  retailPage.value = 1
}

const filteredRetailResults = computed(() => {
  const { retailers, minPrice, maxPrice } = activeRetailFilterState.value

  return retailResults.value.filter((r) => {
    if (retailers && !retailers.includes(r.retailer)) return false
    if (minPrice != null && (r.price ?? 0) < minPrice) return false
    if (maxPrice != null && (r.price ?? Infinity) > maxPrice) return false
    return true
  })
})

// Pagination: Community Listings 
const communityPage = ref(1)

const communityTotalPages = computed(() => {
  const loadedPages = Math.ceil((listings.value?.length || 0) / PAGE_SIZE)
  return hasMore.value ? loadedPages + 1 : Math.max(loadedPages, 1)
})

const pagedListings = computed(() => {
  const start = (communityPage.value - 1) * PAGE_SIZE
  return listings.value.slice(start, start + PAGE_SIZE)
})

const communityRangeStart = computed(() => (communityPage.value - 1) * PAGE_SIZE + 1)
const communityRangeEnd = computed(() => (communityPage.value - 1) * PAGE_SIZE + pagedListings.value.length)

const ensureCommunityPageLoaded = async (page) => {
  while (listings.value.length < page * PAGE_SIZE && hasMore.value && !loading.value) {
    await loadMore()
  }
}

const goToCommunityPage = async (page) => {
  communityPage.value = page
  await ensureCommunityPageLoaded(page)
}

// Pagination: Retailer
const retailPage = ref(1)

const retailTotalPages = computed(() => {
  const loadedPages = Math.ceil((filteredRetailResults.value?.length || 0) / PAGE_SIZE)
  return hasMoreRetail.value ? loadedPages + 1 : Math.max(loadedPages, 1)
})

const pagedRetailResults = computed(() => {
  const start = (retailPage.value - 1) * PAGE_SIZE
  return filteredRetailResults.value.slice(start, start + PAGE_SIZE)
})

const retailRangeStart = computed(() => (retailPage.value - 1) * PAGE_SIZE + 1)
const retailRangeEnd = computed(() => (retailPage.value - 1) * PAGE_SIZE + pagedRetailResults.value.length)

const ensureRetailPageLoaded = async (page) => {
  while (filteredRetailResults.value.length < page * PAGE_SIZE && hasMoreRetail.value && !retailLoading.value) {
    await fetchPersonalisedListings()
  }
}

const goToRetailPage = async (page) => {
  retailPage.value = page
  await ensureRetailPageLoaded(page)
}

</script>

<style scoped>
.marketplace-layout {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  align-items: flex-start;
}

@media (max-width: 960px) {
  .marketplace-layout {
    flex-direction: column;
  }
}
</style>