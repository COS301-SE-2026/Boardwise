<template>
  <div class="grid">

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

<style scoped>
.grid {
  display: grid;

  grid-template-columns:
    repeat(auto-fill, minmax(220px, 1fr));

  gap: 24px;

  margin-top: 32px;
}
</style>