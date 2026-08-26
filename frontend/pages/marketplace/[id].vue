<template>
  <PageContainer data-test="page-container" >
    <Navbar data-test="navbar" />
    <div v-if="loading" data-test="listing-loading">Loading...</div>
    <ListingDetail data-test="listing-detail" v-else-if="listing" :listing="listing" />
    <div v-else data-test="listing-not-found">Listing not found.</div>
  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import ListingDetail from '~/components/features/marketplace/ListingDetail.vue'
import { useMarketplace } from '~/composables/useMarketplace'

const route = useRoute()
const { fetchListingById, loading } = useMarketplace()
const listing = ref(null)

onMounted(async () => {
  listing.value = await fetchListingById(route.params.id)
})
</script>