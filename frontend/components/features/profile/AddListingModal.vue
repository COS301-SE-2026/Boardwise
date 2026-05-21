<template>
  <v-dialog v-model="open" max-width="500">
    <v-card class="pa-6 d-flex flex-column ga-5">

      <h2>Create Listing</h2>

      <v-text-field v-model="title" label="Listing Title" placeholder="Game title" variant="outlined" density="compact" hide-details />


      <v-btn-toggle v-model="type" color="primary" variant="outlined" mandatory>
        <v-btn value="sell">Sell</v-btn>
        <v-btn value="rent">Rent</v-btn>
      </v-btn-toggle>

      <v-text-field v-if="type" v-model="price" label="Amount" prefix="R" placeholder="e.g. 650" type="number" variant="outlined" density="compact" hide-details />

      <v-select v-if="type === 'rent'" v-model="rentalPeriod" 
      label="Rental Period" 
      :items="['1 day','3 days','1 week','2 weeks','1 month']" 
      variant="outlined"
      density="compact" 
      hide-details />


      <v-checkbox v-if="type === 'sell'" v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />


      <v-text-field v-model="location" 
      label="Location" 
      placeholder="e.g. Pretoria" 
      variant="outlined" 
      density="compact" 
      hide-details />


      <!-- <div class="input-group">
        <label>Image URL</label>
        <BaseInput v-model="image" placeholder="https://..." />
      </div> -->

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
const open = defineModel()
const emit = defineEmits(['confirm'])

const title = ref('')
const type = ref('')
const rentalPeriod = ref('')
const negotiable = ref(false)
const price = ref('')
const location = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if(file) fileName.value = file.name
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
}

const handleConfirm = () => {
  if (!title.value || !type.value) return
  emit('confirm', {
    title: title.value,
    type: type.value,
    price: price.value,
    rentalPeriod: rentalPeriod.value,
    negotiable: negotiable.value,
    location: location.value,
    image: fileName.value
  })
  closeModal()
}

</script>

<style scoped>
.hidden-input {
  display: none;
}
</style>