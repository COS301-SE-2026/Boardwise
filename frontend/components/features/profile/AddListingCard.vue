<template>
  <BaseAddCard 
    label="Add Listing"
    @click="showAdd = true"
  />

  <AddListingModal 
    v-model="showAdd"
    @confirm="handleConfirm"
  />
</template>

<script setup>
import BaseAddCard from '~/components/ui/BaseAddCard.vue'
import AddListingModal from '~/components/features/profile/AddListingModal.vue'

import { ref } from 'vue'
import { useMarketplace } from '~/composables/useMarketplace'

const { addListing } = useMarketplace()
const showAdd = ref(false)

const handleConfirm = async (payload, file) => {
  await addListing(payload, file)
}
</script>