<template>
  <base-card
    class="add-card d-flex flex-column justify-center align-center"
    @click="showAdd = true"
  >
    <span class="text-h3 font-weight-bold">+</span>
    <p class="mt-3">Add Listing</p>

    <AddListingModal v-model="showAdd" @confirm="handleConfirm" />
  </base-card>
</template>

<script setup>
import BaseCard        from '~/components/ui/BaseCard.vue'
import AddListingModal from '~/components/features/profile/AddListingModal.vue'
import { useMarketplace } from '~/composables/useMarketplace'

const { addListing } = useMarketplace()
const showAdd = ref(false)

const handleConfirm = async (payload, file) => {
  await addListing(payload, file)
}
</script>

<style scoped>
.add-card {
  cursor:     pointer;
  overflow:   hidden;
  height:     300px;
  border:     2px dashed #ccc;
  margin-top: 25px;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.add-card:hover {
  transform:        translateY(-2px);
  background-color: var(--color-surface-alt);
  box-shadow:       var(--shadow-md) !important;
}
</style>