<template>
  <BaseGrid cols="220px" gap="var(--space-6)">

    <ProfileListingCard
      v-for="listing in listings"
      :key="listing.listingId"
      :listing="listing"
      @deleted="listings = listings.filter(l => l.listingId !== $event)" 

    />

    <AddListingCard @click="$emit('add-listing')" />

  </BaseGrid>
</template>

<script setup>
import BaseGrid           from '~/components/ui/BaseGrid.vue'
import ProfileListingCard from './ProfileListingCard.vue'
import AddListingCard     from './AddListingCard.vue'


defineProps({
  listings: { type: Array, default: () => [] }
})

defineEmits(['add-listing'])
</script>