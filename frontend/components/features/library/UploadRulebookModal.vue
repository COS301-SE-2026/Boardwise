<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-5">

      <h2 class="text-h5 font-weight-bold">Upload a Rulebook</h2>

      <div>
        <p class="text-caption font-weight-bold mb-2">Title</p>
        <BaseInput v-model="title" placeholder="Title" />
      </div>

      <div>
        <p class="text-caption font-weight-bold mb-2">Edition</p>
        <BaseInput v-model="edition" placeholder="Rulebook Edition (optional, eg. version 1)" />
      </div>

      <div>
        <p class="text-caption font-weight-bold mb-2">Language</p>
        <BaseInput v-model="language" placeholder="Language" />
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

const title = ref('')
const edition = ref('')
const language = ref('')
const fileName = ref('')
const fileInput = ref(null)

const triggerUpload = () => fileInput.value?.click()

const fileToUpload = ref(null)

const handleFile = (e) => {
  const file = e.target.files[0]
  if (file) {
    fileName.value = file.name;
    fileToUpload.value = file;
  }
}

const handleAdd = () => {
  if (!title.value || !language.value || !fileToUpload.value) return
  emit('add', {
    title: title.value,
    edition: edition.value,
    language: language.value,
    file: fileToUpload.value
  })
  open.value = false
  title.value = ''
  edition.value = ''
  language.value = ''
  fileName.value = ''
  fileToUpload.value = null
}
</script>