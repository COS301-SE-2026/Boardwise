<template>
  <PageContainer>
    <Navbar />
    <div v-if="loading">Loading...</div>
    <ListingDetail v-else-if="listing" :listing="listing" />
    <div v-else>Listing not found.</div>
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