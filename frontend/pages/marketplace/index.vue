<template>
  <PageContainer data-test="page-container">

    <Navbar data-test="navbar" />

    <MarketplaceHeader data-test="marketplace-header" v-model="searchQ" @create-listing="showCreateListing = true" />

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
      <div class="d-none d-md-flex ga-6 mt-6 align-start">
        <FilterSidebar data-test="filter-sidebar" @filter="handleFilter"/>
        <v-container v-if="loading" class="d-flex justify-center align-center" style="min-height: 60vh">
          <v-progress-circular data-test="loading-spinner" indeterminate color="primary" size="48" />
        </v-container>
        <ListingGrid data-test="listing-grid" v-else :listings="listings" />
      </div>
      
    </template>
    
    <!-- External Retail -->
    <template v-else-if="activeTab === 'Web'">
      <v-container
        v-if="retailLoading && retailResults.length === 0"
        class="d-flex justify-center align-center"
        style="min-height: 60vh"
      >
        <v-progress-circular 
          data-test="loading-spinner"
          indeterminate
          color="primary"
          size="48"
        />
      </v-container>
      
      <RetailerGrid 
        v-else
        data-test="retailer-grid"
        :retailers="retailResults"
      />
    </template>

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

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import MarketplaceHeader from '~/components/features/marketplace/MarketplaceHeader.vue'
import MarketplaceTabs from '~/components/features/marketplace/MarketplaceTabs.vue'

import FilterSidebar from '~/components/features/marketplace/FilterSidebar.vue'
import ListingGrid from '~/components/features/marketplace/ListingGrid.vue'
import AddListingModal from '~/components/features/profile/AddListingModal.vue'

import { useRouter } from 'vue-router'
import { useMarketplace } from '~/composables/useMarketplace'
import { useIntersectionObserver, useDebounceFn  } from '@vueuse/core'
import { useRetail } from '~/composables/useRetail'

const router = useRouter();
const activeTab = ref('Community Listings')
const showFilters = ref(false)
const showCreateListing = ref(false)

const {listings, loading, fetchListings, addListing, loadMore, hasMore} = useMarketplace();

const {retailResults, retailLoading, hasMoreRetail, fetchPersonalisedListings} = useRetail()

onMounted(() => {
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
useIntersectionObserver(sentinel,([entry])=>{
  if(!entry.isIntersecting) return

  if(activeTab.value === 'Web'){
    if(hasMoreRetail.value && !retailLoading.value) fetchPersonalisedListings()
    return
  }

  if(hasMore.value && !loading.value) loadMore()
})

const searchQ = ref('');
const activeFilterState = ref({})


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

  const  lt= getListingType(filters.rent,filters.sale);

   activeFilterState.value = {
    listingType: lt,
    genres: filters.genres,
    conditions: conditions,
    minPrice: filters.minPrice,
    maxPrice: filters.maxPrice,
  }

  console.log('active filters',activeFilterState.value);

  fetchListings({ ...activeFilterState.value, search: searchQ.value || null }, true);
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