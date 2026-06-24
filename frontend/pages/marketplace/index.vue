<template>
  <PageContainer>

    <Navbar />

    <MarketplaceHeader @create-listing="showCreateListing = true" />

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

const handleFilter = (filters)=>{

  const  lt= (filters.rent && filters.sale) ? null : filters.rent ? 'rental' : filters.sale ? 'sale' : null;

  fetchListings({
    listingType: lt,
    genres: filters.genres,
    conditions: filters.conditions.length ? filters.conditions.map(c => c.toLowerCase()) : null,
    minPrice: filters.minPrice ? Number(filters.minPrice) : null,
    maxPrice: filters.maxPrice ? Number(filters.maxPrice) : null,
  },true);
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