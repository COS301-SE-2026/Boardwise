<template>
  <div class="d-flex flex-wrap ga-6 mt-8">

    <ProfileListingCard
      v-for="listing in listings"
      :key="listing.listingId"
      :listing="listing"
    />

    <AddListingCard @click="$emit('add-listing')" />

  </div>
</template>

<script setup>
import ProfileListingCard from './ProfileListingCard.vue'
import AddListingCard from './AddListingCard.vue'


defineProps({
  listings: {
    type: Array,
    default: () => []
  }
})

defineEmits(['add-listing'])
</script>