<template>
  <section class="section">

    <div class="top">

      <SectionTitle title="My Listings" />

      <BaseButton @click="showAddListing = true">
        + Add Listing
      </BaseButton>

    </div>

    <ListingGrid :listings="listings" />

    <AddListingModal v-model="showAddListing" />

  </section>
</template>

<script setup>
import ListingGrid from './ListingGrid.vue'
import AddListingModal from './AddListingModal.vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

defineProps({
  listings: Array
})

const showAddListing = ref(false)
</script>

<style scoped>
.section {
  margin-top: 32px;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .top {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>