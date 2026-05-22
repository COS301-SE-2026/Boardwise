<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6 d-flex flex-column ga-5" style="background: var(--color-surface) !important;">
      <h2>Create Listing</h2>

      <v-text-field v-model="title" label="Listing Title" placeholder="Game title" variant="outlined" density="compact" hide-details />

      <v-btn-toggle v-model="type" color="primary" variant="outlined" mandatory>
        <v-btn value="sell">Sell</v-btn>
        <v-btn value="rent">Rent</v-btn>
      </v-btn-toggle>

      <v-text-field v-if="type" v-model="price" label="Amount" prefix="R" placeholder="e.g. 650" type="number" variant="outlined" density="compact" hide-details />

      <v-select
        v-if="type === 'rent'"
        v-model="rentalPeriod"
        label="Rental Period"
        :items="['1 day','3 days','1 week','2 weeks','1 month']"
        variant="outlined"
        density="compact"
        hide-details
      />

      <v-checkbox v-if="type === 'sell'" v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />

      <v-text-field v-model="location" label="Location" placeholder="e.g. Pretoria" variant="outlined" density="compact" hide-details />

      <div class="d-flex align-center ga-3">
        <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Image</v-btn>
        <span class="text-grey text-body-2">{{ fileName || '···' }}</span>
        <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <div class="d-flex justify-end ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleConfirm">Create Listing</v-btn>
      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>
const open = defineModel()
const emit = defineEmits(['confirm'])

const title        = ref('')
const type         = ref('')
const rentalPeriod = ref('')
const negotiable   = ref(false)
const price        = ref('')
const location     = ref('')
const fileName     = ref('')
const fileInput    = ref(null)
const file         = ref(null)

const triggerUpload = () => fileInput.value.click()

const handleFileChange = (e) => {
  const toUpload = e.target.files[0]
  if (toUpload) {
    fileName.value = toUpload.name
    file.value     = toUpload
  }
}

const closeModal = () => {
  open.value = false
  title.value = ''
  type.value = ''
  price.value = ''
  rentalPeriod.value = ''
  negotiable.value = false
  location.value = ''
  fileName.value = ''
  file.value = null
}

const rentalPeriodMap = {
  '1 day':  '1d',
  '3 days': '3d',
  '1 week': '1w',
  '2 weeks':'2w',
  '1 month':'1m',
}

const getRentalPeriod = () => {
  const start = new Date()
  const end   = new Date()
  const code  = rentalPeriodMap[rentalPeriod.value] ?? '1w'

  switch (code) {
    case '1d': end.setDate(end.getDate() + 1);   break
    case '3d': end.setDate(end.getDate() + 3);   break
    case '1w': end.setDate(end.getDate() + 7);   break
    case '2w': end.setDate(end.getDate() + 14);  break
    case '1m': end.setMonth(end.getMonth() + 1); break
  }

  return [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0],
  ]
}

const handleConfirm = () => {
  if (!title.value || !type.value || price.value < 0 || !fileName.value) return

  emit('confirm', {
    gameTitle:   title.value,
    listingType: type.value === 'rent' ? 'rental' : 'sale',
    price:       price.value,
    itemType:    'boardgame',
    description: 'This is the board game',
    genres:      ['strategy', 'negotiation'],
    rentalPeriod: type.value === 'rent' ? getRentalPeriod() : null,
  }, file.value)

  closeModal()
}
</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>