<template>
  <PageContainer>

    <Navbar />

    <MarketplaceHeader @create-listing="showCreateListing = true" />

    <MarketplaceTabs v-model="activeTab" />

    <div class="marketplace-layout">

      <FilterSidebar />
      <!--TODO: Uncomment for loading -->
      <!-- <div v-if = "loading">Loading listings...</div> -->
      <!-- <ListingGrid  v-else :listings="listings" /> -->
       <ListingGrid :listings="listings" />
    </div>

    <AddListingModal
      v-model="showCreateListing" 
      @confirm="handle"
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

const router = useRouter();
const activeTab = ref('Community')
const showCreateListing = ref(false)

const {listings, loading, fetchListings, addListing} = useMarketplace();

onMounted(() => {
  if(!localStorage.getItem('access_token')){
    router.push('/auth/signin');
  }
  fetchListings()
})

const handle = async (data, image) => {
  await addListing(data, image);
  showCreateListing.value = false;
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