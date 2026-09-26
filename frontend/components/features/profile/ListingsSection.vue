<template>
  <section>

    <ListingGrid
      :listings="listings"
      :editable="editable"
      @add-listing="showAddListing = true"
      @delete-listing="openDelete"
      @deleted="$emit('deleted')"
      @updated="$emit('updated')"
    />

    <AddListingModal v-model="showAddListing" @confirm="handleAddListing" />

    <DeleteModal v-model="showDelete" @confirm="handleDelete" />

  </section>
</template>

<script setup>
import ListingGrid from './ListingsGrid.vue'
import AddListingModal from './AddListingModal.vue'
import DeleteModal from './DeleteListingModal.vue'

import { useMarketplace } from '~/composables/useMarketplace'
import { useSnackBar } from '~/composables/useSnackbar'

const { show } = useSnackBar()

defineProps({
  listings: {
    type: Array,
    default: () => []
  },
  editable: {
    type: Boolean,
    default: false
  }
})

const { removeListing, addListing } = useMarketplace()

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

const handleAddListing = async (payload, file, callback) => {
  try {
    await addListing(payload, file)
    emit('updated')
    callback?.()
    show('Listing created successfully!')
  } catch (err) {
    callback?.(err?.data?.message || err?.message || 'Failed to create listing. Please try again.')
  }
}
</script>