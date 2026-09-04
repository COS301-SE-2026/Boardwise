<template>
  <BaseGrid>
    <ListingCard
      data-test="listing-grid"
      v-for="listing in listings"
      :key="listing.listingId"
      :listing="listing"
    />
  </BaseGrid>
</template>

<script setup>
import BaseGrid    from '~/components/ui/BaseGrid.vue'
import ListingCard from './ListingCard.vue'

defineProps({
  listings: { type: Array, default: () => [] }
})
</script>
