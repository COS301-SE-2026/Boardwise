<template>
  <PageContainer>

    <Navbar />

    <MarketplaceHeader v-model="searchQ" @create-listing="showCreateListing = true" />

    <MarketplaceTabs v-model="activeTab" />

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
        <FilterSidebar @filter="handleFilter" />
      </v-navigation-drawer>
    </div>

    <!-- Desktop -->
    <div class="d-none d-md-flex ga-6 mt-6 align-start">
      <FilterSidebar @filter="handleFilter"/>
      
      <div class="flex-grow-1">
        <div v-if = "loading">
          Loading listings...
        </div> 

        <ListingGrid  
          v-else 
          :listings="listings" 
        />

      </div>
    </div>

    <div class="d-md-none">
      <div v-if="loading">
        Loading listings...
      </div>

      <ListingGrid 
        v-else 
        :listings="listings"
      />
    </div>
    
    <div ref="sentinel" style="height:1px" />

    <AddListingModal
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

const router = useRouter();
const activeTab = ref('Community')
const showFilters = ref(false)
const showCreateListing = ref(false)

const {listings, loading, fetchListings, addListing, loadMore, hasMore} = useMarketplace();

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
  if(entry.isIntersecting&& hasMore.value && !loading.value) loadMore()
})

const searchQ = ref('');
const activeFilterState = ref({})


const delaySearch = useDebounceFn((query) => {
  fetchListings({ ...activeFilterState.value, search: query || null }, true)
}, 400)

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