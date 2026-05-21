<template>
  <section class="mt-8">

    <div class="d-flex justify-space-between align-center mb-6 flex-wrap ga-4">
      <SectionTitle title="My Listings" />
      <v-btn color="primary" @click="showAddListing = true">+ Add Listing</v-btn>
    </div>

    <ListingGrid
      :listings="listings"
      @add-listing="showAddListing = true"
    />

    <AddListingModal v-model="showAddListing" />

  </section>
</template>

<script setup>
import ListingGrid from './ListingsGrid.vue'
import AddListingModal from './AddListingModal.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

defineProps({
  listings: Array
})

const showAddListing = ref(false)
</script>