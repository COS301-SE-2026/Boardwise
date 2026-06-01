<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-5">

      <h2 class="text-h5 font-weight-bold">Upload a Rulebook</h2>

      <div>
        <p class="text-caption font-weight-bold mb-2">Game Name</p>
        <BaseInput v-model="gameName" placeholder="Game Name" />
      </div>

      <div>
        <p class="text-caption font-weight-bold mb-2">Edition</p>
        <BaseInput v-model="edition" placeholder="Rulebook Edition (optional, eg. version 1)" />
      </div>

      <div>
        <p class="text-caption font-weight-bold mb-2">Rulebook Upload</p>
        <div class="d-flex align-center ga-3">
          <BaseButton variant="secondary" @click="triggerUpload">
            <v-icon start>mdi-upload</v-icon>
            Upload Rulebook PDF
          </BaseButton>
          <span class="text-caption text-medium-emphasis">{{ fileName || '···' }}</span>
          <input
            ref="fileInput"
            type="file"
            accept=".pdf"
            style="display: none;"
            @change="handleFile"
          />
        </div>
      </div>

      <div class="d-flex justify-space-between mt-2">
        <BaseButton variant="secondary" @click="open = false">Cancel</BaseButton>
        <BaseButton @click="handleAdd">Add</BaseButton>
      </div>

    </div>
  </BaseModal>
</template>

<script setup>
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

const open = defineModel()
const emit = defineEmits(['add'])

const gameName = ref('')
const edition = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const handleFile = (e) => {
  const file = e.target.files[0]
  if (file) fileName.value = file.name
}

const handleAdd = () => {
  if (!gameName.value) return
  emit('add', {
    gameName: gameName.value,
    edition: edition.value,
    file: fileName.value
  })
  open.value = false
  gameName.value = ''
  edition.value = ''
  fileName.value = ''
}
</script>