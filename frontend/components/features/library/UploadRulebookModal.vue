<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-5">

      <h2 class="text-h5 font-weight-bold">Upload a Rulebook</h2>

      <div>
        <p class="text-caption font-weight-bold mb-2">Title</p>
        <BaseButton variant="secondary" class="w-100 justify-space-between" @click="browserOpen = true">
          <span>{{ title || 'Select a board game' }}</span>
          <v-icon end>mdi-chevron-right</v-icon>
        </BaseButton>
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
          <span class="text-caption text-medium-emphasis">{{ fileName || 'No file selected' }}</span>
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
        <BaseButton :disabled="!isFormValid" :loading="loading" @click="handleAdd">Add</BaseButton>
      </div>

    </div>

    <AddGamesModal
      v-model="browserOpen"
      @confirm="onGameSelected"
      @add-custom="$emit('add-custom')"
    />

  </BaseModal>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import { useBoardGames } from '~/composables/useBoardGames';

const { games, isLoading: gamesLoading, searchGames } = useBoardGames();
onMounted(() => searchGames());

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
});

const open = defineModel()
const emit = defineEmits(['add','add-custom'])

const title = ref('')
const edition = ref('')
const language = ref('')
const fileName = ref('')
const fileInput = ref(null)
const fileToUpload = ref(null)

const triggerUpload = () => fileInput.value?.click()

const isFormValid = computed(() => {
  return title.value && language.value && fileToUpload.value;
});

const onGameSelected = (selectedGames) => {
  const game = selectedGames?.[0]
  if (!game) return
  title.value = game.title
  selectedGameId.value = game.id
}

const handleFile = (e) => {
  const file = e.target.files[0]
  if (file) {
    fileName.value = file.name;
    fileToUpload.value = file;
  }
}

const resetForm = () => {
  title.value = '';
  edition.value = '';
  language.value = '';
  fileName.value = '';
  fileToUpload.value = null;
  if(fileInput.value){
    fileInput.value.value = '';
  }
}

watch(open, (isOpen) => {
  if(!isOpen){
    resetForm();
  }
});

const handleAdd = () => {
  if (!isFormValid.value) return
  emit('add', {
    title: title.value,
    edition: edition.value,
    language: language.value,
    file: fileToUpload.value
  })
}

let searchTimeout;
const onTitleSearch = (query) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => searchGames(query), 400);
}
</script>