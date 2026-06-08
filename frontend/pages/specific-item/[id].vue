<template>
  <PageContainer>

    <Navbar />

    <BaseEmptyState
        v-if="!listing"
        title="Listing is coming soon"
        description="This listing may have been tempoarly removed or is no longer available."
    />

    <SpecificItemListingDetail
        v-else
        :listing="listing"
    />

  </PageContainer>
</template>

<script setup>
import { listings } from '~/services/mockData/listings'
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import SpecificItemListingDetail from '~/components/features/specific-item/SpecificItemListingDetail.vue'

const route = useRoute()

const listing = listings.find(item => item.id === route.params.id)

</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
  margin-top: var(--space-6);
}
</style>