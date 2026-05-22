<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6 d-flex flex-column ga-6">

      <h2>Add a Game</h2>

      <div class="d-flex flex-column ga-4">
        <v-text-field v-model="title" label="Game Name" placeholder="Please enter" variant="outlined" density="compact" hide-details />

        <v-select
          v-model="category"
          label="Game Genre / Category"
          :items="['Strategy','Family','Abstract','Party','Cooperative','Thematic','War','Other']"
          variant="outlined"
          density="compact"
          hide-details
        />

        <div class="d-flex align-center ga-3">
          <v-btn variant="outlined" color="primary" @click="triggerUpload">Upload Game Cover</v-btn>
          <span class="text-grey text-body-2">{{ fileName || '···' }}</span>
          <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
        </div>
      </div>

      <div class="d-flex justify-space-between ga-3">
        <v-btn variant="outlined" color="primary" @click="closeModal">Cancel</v-btn>
        <v-btn color="primary" @click="handleConfirm">Add</v-btn>
      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'

const open = defineModel()
const emit = defineEmits(['confirm'])

const title    = ref('')
const category = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const closeModal = () => {
  open.value     = false
  title.value    = ''
  category.value = ''
  fileName.value = ''
}

const handleConfirm = () => {
  if (!title.value) return
  emit('confirm', {
    title:    title.value,
    category: category.value,
    image:    fileName.value,
  })
  closeModal()
}
</script>

<style scoped>
h2 {
  margin:    0;
  font-size: 24px;
}

.hidden-input {
  display: none;
}
</style>