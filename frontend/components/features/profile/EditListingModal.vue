<template>
  <v-dialog v-model="open" max-width="500">
    <v-card class="pa-6 d-flex flex-column ga-5">

      <h2 class="ma-0">Edit Listing</h2>

      <v-text-field
        v-model="gameTitle"
        label="Listing Title"
        placeholder="Game title"
        variant="outlined"
        density="compact"
        hide-details
      />

      <v-btn-toggle v-model="listingType" color="primary" variant="outlined" mandatory divided>
        <v-btn value="sale">Sell</v-btn>
        <v-btn value="rental">Rent</v-btn>
      </v-btn-toggle>

      <v-text-field
        v-model="price"
        label="Amount"
        prefix="R"
        placeholder="e.g. 650"
        type="number"
        variant="outlined"
        density="compact"
        hide-details
      />

      <template v-if="listingType === 'rental'">
        <v-text-field
          v-model="startDate"
          label="Start Date"
          type="date"
          variant="outlined"
          density="compact"
          hide-details
        />
        <v-text-field
          v-model="endDate"
          label="End Date"
          type="date"
          variant="outlined"
          density="compact"
          hide-details
        />
      </template>

      <v-textarea
        v-model="description"
        label="Description"
        placeholder="Describe the listing"
        variant="outlined"
        density="compact"
        rows="3"
        hide-details
      />

      <div class="d-flex align-center ga-3">
        <v-btn variant="outlined" color="primary" @click="triggerUpload">
          Upload Image
        </v-btn>
        <span class="text-body-2" style="color: var(--color-text-muted);">
          {{ fileName || '···' }}
        </span>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden-input"
          @change="handleFileChange"
        />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="open = false">Cancel</v-btn>
        <v-btn color="primary" :loading="loading" @click="handleSave">Save Changes</v-btn>
      </div>

    </v-card>
  </v-dialog>
</template>

<script setup>
import { useMarketplace } from '~/composables/useMarketplace'

const { editListing, loading } = useMarketplace()

const open  = defineModel()
const props = defineProps({ listing: Object })
const emit  = defineEmits(['saved'])

const gameTitle   = ref(props.listing?.gameTitle ?? '')
const listingType = ref(props.listing?.listingType ?? 'sale')
const price       = ref(props.listing?.price ?? '')
const description = ref(props.listing?.description ?? '')
const itemType    = ref(props.listing?.itemType ?? '')
const startDate   = ref(props.listing?.rentalPeriod?.startDate ?? '')
const endDate     = ref(props.listing?.rentalPeriod?.endDate ?? '')
const imageFile   = ref(null)
const fileName    = ref(null)
const fileInput   = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0] ?? null
  imageFile.value = file
  fileName.value  = file?.name ?? null
}

const handleSave = async () => {
  const listingData = {
    gameTitle:    gameTitle.value,
    itemType:     itemType.value,
    listingType:  listingType.value,
    price:        Number(price.value),
    description:  description.value,
    genres:       props.listing?.genres ?? [],
    rentalPeriod: listingType.value === 'rental'
      ? { startDate: startDate.value, endDate: endDate.value }
      : null,
  }

  await editListing(props.listing.listingId, listingData, imageFile.value ?? undefined)
  emit('saved')
  open.value = false
}
</script>

<style scoped>
.hidden-input { display: none; }
</style>