<template>
  <BaseGrid cols="220px" gap="var(--space-6)">

    <ProfileListingCard
      v-for="listing in listings"
      :key="listing.listingId"
      :listing="listing"
      :editable="editable"
      @deleted="$emit('deleted')"
      @updated="$emit('updated')"
    />

    <AddListingCard v-if="editable" @click="$emit('add-listing')" />

  </BaseGrid>
</template>

<script setup>
import BaseGrid           from '~/components/ui/BaseGrid.vue'
import ProfileListingCard from './ProfileListingCard.vue'
import AddListingCard     from './AddListingCard.vue'

const emit = defineEmits(['add-listing', 'deleted', 'updated'])

defineProps({
  listings: { type: Array, default: () => [] },
  editable: { type: Boolean, default: false }
})


</script>