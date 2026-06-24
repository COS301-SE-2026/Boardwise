<template>
  <PageContainer>

    <Navbar />

    <MarketplaceHeader v-model="searchQ" @create-listing="showCreateListing = true" />

    <MarketplaceTabs v-model="activeTab" />

    <div class="marketplace-layout">

      <FilterSidebar @filter="handleFilter"/>
      <!--TODO: Uncomment for loading -->
      <!-- <div v-if = "loading">Loading listings...</div> -->
      <!-- <ListingGrid  v-else :listings="listings" /> -->
       <ListingGrid :listings="listings" />
    </div>
    
    <div ref="sentinel" style="height:1px" />

    <AddListingModal
      v-model="showCreateListing" 
      @confirm="handleAdd"
    />
    
  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import MarketplaceHeader from '~/components/features/marketplace/MarketplaceHeader.vue'
import MarketplaceTabs from '~/components/features/marketplace/MarketplaceTabs.vue'

import FilterSidebar from '~/components/features/marketplace/FilterSidebar.vue'
import ListingGrid from '~/components/features/marketplace/ListingGrid.vue'
import AddListingModal from '~/components/features/profile/AddListingModal.vue'
import { useRouter } from 'vue-router'
import { useMarketplace } from '~/composables/useMarketplace'
import { useIntersectionObserver } from '@vueuse/core'

const router = useRouter();
const activeTab = ref('Community')
const showCreateListing = ref(false)

const {listings, loading, fetchListings, addListing, loadMore} = useMarketplace();

onMounted(() => {
  if(!localStorage.getItem('access_token')){
    router.push('/auth/signin');
  }
  fetchListings({}, true) 
})

const handleAdd = async (data, image) => {
  await addListing(data, image);
  showCreateListing.value = false;
}

const sentinel = ref(null)
useIntersectionObserver(sentinel,([entry])=>{
  if(entry.isIntersecting) loadMore()
})

const searchQ = ref('');
const activeFilterState = ref({})


watch(searchQ,(q)=>{
  fetchListings({
    ...activeFilterState.value,
    search: q || null},true)
  })


  const handleFilter = (filters)=>{

  const conditions = filters.conditions.length ? filters.conditions.map(c => c.toLowerCase()) : null
  console.log('conditions:', conditions)

  const  lt= (filters.rent && filters.sale) ? null : filters.rent ? 'rental' : filters.sale ? 'sale' : null;

   activeFilterState.value = {
    listingType: lt,
    genres: filters.genres,
    conditions: conditions,
    minPrice: filters.minPrice,
    maxPrice: filters.maxPrice,
  }
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

@media (max-width: 900px) {
  .marketplace-layout {
    flex-direction: column;
  }
}
</style>