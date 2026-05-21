<template>
  <v-card style="overflow: hidden;">

    <v-img
      :src="listing.imageUrl"
      :alt="listing.gameTitle"
      height="180"
      cover
    />

    <v-card-text class="d-flex flex-column ga-2">

      <h3 class="ma-0">{{ listing.gameTitle }}</h3>

      <p class="text-primary font-weight-bold ma-0">R{{ listing.price }}</p>

      <div class="d-flex ga-3 mt-2">
        <v-btn size="small" color="primary" @click="showEdit = true">Edit</v-btn>
        <v-btn size="small" variant="outlined" color="error" @click="showDelete = true">Delete</v-btn>
      </div>

    </v-card-text>

    <EditListingModal v-model="showEdit" :listing="listing" />
    <DeleteListingModal v-model="showDelete" :listing="listing" />

  </v-card>
</template>

<script setup>
import EditListingModal from './EditListingModal.vue'
import DeleteListingModal from './DeleteListingModal.vue'

defineProps({
  listing: {
    type: Object,
    required: true
  }
})

const showEdit = ref(false)
const showDelete = ref(false)
</script>