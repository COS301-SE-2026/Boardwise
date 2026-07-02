<template>
  <BaseGrid cols="220px" gap="var(--space-6)">

    <ProfileListingCard
      v-for="listing in listings"
      :key="listing.listingId"
      :listing="listing"
      @deleted="$emit('deleted')"
    />

    <AddListingCard @click="$emit('add-listing')" />

  </BaseGrid>
</template>

<script setup>
import BaseGrid           from '~/components/ui/BaseGrid.vue'
import ProfileListingCard from './ProfileListingCard.vue'
import AddListingCard     from './AddListingCard.vue'

const emit = defineEmits(['add-listing', 'deleted'])

defineProps({
  listings: { type: Array, default: () => [] }
})


</script>