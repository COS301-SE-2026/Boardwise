<template>
  <section>

    <ListingGrid
      :listings="listings"
      @add-listing="showAddListing = false"
      @delete-listing="openDelete"
      @deleted="$emit('deleted')"
      @updated="$emit('updated')"
    />

    <AddListingModal v-model="showAddListing" />

    <DeleteModal v-model="showDelete" @confirm="handleDelete" />

  </section>
</template>

<script setup>
import ListingGrid from './ListingsGrid.vue'
import AddListingModal from './AddListingModal.vue'
import DeleteModal from './DeleteListingModal.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'
import { useMarketplace } from '~/composables/useMarketplace'

defineProps({
  listings: Array
})

const { removeListing } = useMarketplace()

const showAddListing = ref(false)
const showDelete = ref(false)
const selectedId = ref(null)

const emit = defineEmits(['deleted', 'updated', 'add-listing'])

const openDelete = (id) => {
  selectedId.value = id
  showDelete.value = true
}

const handleDelete = async () => {
  if (selectedId.value) {
    await removeListing(selectedId.value)
    selectedId.value = null
  }
}
</script>