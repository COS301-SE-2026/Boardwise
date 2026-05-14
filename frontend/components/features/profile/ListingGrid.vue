<template>
  <div class="grid">

    <ProfileListingCard
      v-for="listing in listings"
      :key="listing.id"
      :listing="listing"
    />

    <AddListingCard />

  </div>
</template>

<script setup>
import ProfileListingCard from './ProfileListingCard.vue'
import AddListingCard from './AddListingCard.vue'

defineProps({
  listings: Array
})
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