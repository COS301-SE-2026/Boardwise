<template>
  <BaseModal v-model="open">
    <div class="d-flex flex-column ga-5">
      <h2 class="text-h5 font-weight-bold">Upload a Rulebook</h2>

      <div>
        <p class="text-caption font-weight-bold mb-2">Title</p>

        <template v-if="!selectedGame">
          <BaseSearch
            v-model="search"
            placeholder="Board game title"
          />

          <BaseButton variant="secondary" class="mt-2" @click="customModalOpen = true">
            + Add new game
          </BaseButton>

          <div v-if="search.trim()" class="titleResults mt-2">
            <div v-if="gamesLoading" class="text-caption text-medium-emphasis pa-2">
              Searching...
            </div>

            <template v-else>
              <div
                v-for="game in games"
                :key="game.id"
                class="titleResult"
                @click="selectGame(game)"
              >
                <div class="titleResult_thumb">
                  <v-img
                    width="48"
                    height="48"
                    cover
                    :src="game.imageUrl ?? '/default.png'"
                  ></v-img>
                </div>
                <div class="titleResult_info">
                  <p class="text-body-2 mb-0">{{ game.title }}</p>
                  <p class="text-caption mb-0" style="color: var(--color-text-muted)">
                    {{ game.genre?.[0] ?? '' }}
                  </p>
                </div>
              </div>
            </template>
          </div>
        </template>

        <div v-else class="selectedGameCard">
          <v-img
            width="40"
            height="40"
            cover
            :src="selectedGame.imageUrl ?? '/default.png'"
            class="selectedGameCard_thumb"
          ></v-img>
          <span class="text-body-2 flex-1-1">{{ selectedGame.title }}</span>
          <BaseButton variant="secondary" size="small" @click="clearSelection">
            Clear selection
          </BaseButton>
        </div>
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

    <AddCustomGameModal
      v-model="customModalOpen"
      create-only
      @confirm="onCustomGameAdded"
    />
  </BaseModal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'
import AddCustomGameModal from '~/components/features/library/AddCustomGameModal.vue'
import { useBoardGames } from '~/composables/useBoardGames'
import { useDebounceFn } from '@vueuse/core'

const { games, isLoading: gamesLoading, searchGames } = useBoardGames()

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

const open = defineModel()
const emit = defineEmits(['add'])

const search = ref('')
const selectedGame = ref(null)
const customModalOpen = ref(false)

const debouncedSearch = useDebounceFn((query) => searchGames(query), 400)

watch(search, (val) => {
  if (selectedGame.value) selectedGame.value = null
  if (val && val.trim()) {
    debouncedSearch(val)
  }
})

const selectGame = (game) => {
  selectedGame.value = game
}

const clearSelection = () => {
  selectedGame.value = null
  search.value = ''
}

const onCustomGameAdded = async (res, submittedGame) => {
  const maxAttempts = 4
  const delayMs = 500

  try {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      await searchGames(submittedGame.title)

      if (games.value.length) {
        selectedGame.value = games.value[0]
        return
      }

      if (attempt < maxAttempts) {
        await new Promise(resolve => setTimeout(resolve, delayMs))
      }
    }

    console.error('Search returned no results for newly created game after retries:', submittedGame.title)
    selectedGame.value = null
  } catch (err) {
    console.error('Failed to look up newly created game', err)
    selectedGame.value = null
  } finally {
    games.value = []
  }
}

const edition = ref('')
const language = ref('')
const fileName = ref('')
const fileInput = ref(null)
const fileToUpload = ref(null)

const triggerUpload = () => fileInput.value?.click()

const isFormValid = computed(() => {
  return selectedGame.value && language.value && fileToUpload.value
})

const handleFile = (e) => {
  const file = e.target.files[0]
  if (file) {
    fileName.value = file.name
    fileToUpload.value = file
  }
}

const resetForm = () => {
  search.value = ''
  selectedGame.value = null
  edition.value = ''
  language.value = ''
  fileName.value = ''
  fileToUpload.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

watch(open, (isOpen) => {
  if (!isOpen) {
    resetForm()
  }
})

const handleAdd = () => {
  if (!isFormValid.value) return
  emit('add', {
    title: selectedGame.value.title,
    gameId: selectedGame.value.id,
    edition: edition.value,
    language: language.value,
    file: fileToUpload.value
  })
}
</script>

<style scoped>
.titleResults {
  border: 1px solid var(--color-border, #e0e0e0);
  border-radius: var(--radius-md, 8px);
  max-height: 220px;
  overflow-y: auto;
}

.titleResult {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  cursor: pointer;
}

.titleResult:hover {
  background: var(--color-surface-hover, #f5f5f5);
}

.titleResult_thumb {
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
}

.titleResult_info {
  flex: 1;
  min-width: 0;
}

.titleResult_info p {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selectedGameCard {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selectedGameCard_thumb {
  flex: 0 0 40px;
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
}
</style>