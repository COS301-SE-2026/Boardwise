<template>
 <v-dialog v-model="open" max-width="500">
  <v-card class="pa-6 d-flex flex-column ga-5">

      <h2>Edit Listing</h2>

      <v-text-field v-model="title" 
      label="Listing Title" 
      placeholder="Game title" 
      variant="outlined" 
      density="compact" 
      hide-details />


      <v-btn-toggle v-model="type" color="primary" variant="outlined" mandatory>
        <v-btn value="sell">Sell</v-btn>
        <v-btn value="rent">Rent</v-btn>
      </v-btn-toggle>

      <v-text-field v-if="type" 
      v-model="price" 
      label="Amount" 
      prefix="R" 
      placeholder="e.g. 650" 
      type="number" 
      variant="outlined"
      density="compact" 
      hide-details />


      <v-select v-if="type === 'rent'" 
      v-model="rentalPeriod" 
      label="Rental Period" :items="['1 day','3 days','1 week','2 weeks','1 month']" 
      variant="outlined" 
      density="compact"
       hide-details />


      <v-checkbox v-if="type === 'sell'"
       v-model="negotiable" 
       label="Open to negotiation" 
       color="primary" 
       density="compact" 
       hide-details />


      <v-text-field v-model="location" 
      label="Location" 
      placeholder="e.g. Pretoria" 
      variant="outlined" 
      density="compact" 
      hide-details />


      <div class="d-flex align-center ga-3">
        <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Image</v-btn>
        <span class="text-grey text-body-2">{{ fileName || '···' }}</span>
        <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleConfirm">Create Listing</v-btn>
      </div>

    </v-card>
  </v-dialog>
</template>

<script setup>
import { useMarketplace } from '~/composables/useMarketplace'
const { editListing, loading } = useMarketplace()

const open = defineModel()
const props = defineProps({ listing: Object })
const emit = defineEmits(['save'])


const gameTitle   = ref(props.listing?.gameTitle ?? '')
const listingType = ref(props.listing?.listingType ?? 'sale')
const imageFile   = ref(null)
const itemType    = ref(props.listing?.itemType ?? '')
const description = ref(props.listing?.description ?? '')
const startDate   = ref(props.listing?.rentalPeriod?.startDate ?? '')
const endDate     = ref(props.listing?.rentalPeriod?.endDate ?? '')

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  imageFile.value = e.target.files[0] ?? null  // stores  File object
}

const handleSave = async () => {
  const listingData = {
    gameTitle: gameTitle.value,
    itemType: itemType.value,
    listingType: listingType.value,
    price: Number(price.value),
    description: description.value,
    genres: props.listing?.genres ?? [],
    rentalPeriod: listingType.value === 'rental'
      ? [startDate.value, endDate.value]
      : null
  }
  const currentImageName = computed(() =>
  props.listing?.imageUrl ? props.listing.imageUrl.split('/').pop() : null
)
  await editListing(props.listing.listingId, listingData, imageFile.value ?? undefined)
  emit('saved')
  open.value = false
}
</script>

<style scoped>
.hidden-input { display: none; }
</style>