<template>
  <BaseModal 
    title="Editing Listing" 
    v-model="open"
    max-width="600" 
    :loading="saving"
  >
      <v-form ref="form" class="d-flex flex-column ga-5">
        <div class="d-flex flex-column ga-5">

          <!-- Listing Title -->
          <BaseInput 
            v-model="listing_title"
            label="Listing Title"
            placeholder="Listing title"
            :rules="[rules.required, rules.listingTitle]"
          />

          <!-- Game -->
          <v-autocomplete
            v-model="game_title"
            label="Game Title"
            :items="games"
            :loading="gamesLoading"
            item-title="title"
            item-value="title"
            no-filter
            variant="outlined"
            density="compact"
            :rules="[rules.required]"
            @update:search="onGameSearch"
          />

          <!-- Version -->
          <BaseInput 
            v-model="version"
            label="Version"
            placeholder="e.g. Original"
            :rules="[rules.required, rules.version]"
          />

          <!-- Genres -->
          <v-autocomplete
            v-model="selected_genres"
            label="Genres"
            :items="genres"
            :loading="genresLoading"
            multiple
            chips
            closable-chips
            variant="outlined"
            density="compact"
            :rules="[rules.requiredArray]"
            @update:search="onGenreSearch"
          />

          <!-- Condition -->
          <v-select
            v-model="selected_condition"
            label="Condition"
            :items="conditions"
            variant="outlined"
            density="compact"
            :rules="[rules.required]"
          />

          <!-- Item Type -->
          <v-select
            v-model="selected_item_type"
            label="Item Type"
            :items="item_types"
            variant="outlined"
            density="compact"
            :rules="[rules.required]"
          />

          <!-- Listing Type -->
          <div class="d-flex">
            <v-btn-toggle v-model="listing_type" color="primary" variant="outlined" mandatory divided>
              <v-btn value="sell">Sell</v-btn>
              <v-btn value="rent">Rent</v-btn>
            </v-btn-toggle>
          </div>

          <!-- Price -->
          <BaseInput
            v-model="price"
            label="Amount"
            prefix="R"
            placeholder="e.g. 650"
            type="number"
            min="0"
            step="1"
            variant="outlined"
            density="compact"
            :rules="[rules.required, rules.positiveNumber]"
            @keydown="blockNegativeKeys"
            @paste="blockNegativePaste"
          />

          <!-- Rental Period -->
          <div class="rental-period">
            <div v-if="listing_type === 'rent'" class="d-flex flex-column ga-5">
              <v-date-input
                v-model="start_date"
                label="Start Date"
                variant="outlined"
                @keydown="blockManualDateEntry"
                :rules="[rules.required, rules.startNotPast]"
              />

              <v-date-input
                v-model="end_date"
                label="End Date"
                variant="outlined"
                @keydown="blockManualDateEntry"
                :rules="[rules.required, rules.endAfterStart]"
              />
            </div>

            <v-checkbox v-else v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />
          </div>

          <!-- Location -->
          <v-text-field
            v-model="location"
            label="Location"
            placeholder="e.g. Pretoria"
            :rules="[rules.required]"
          />

          <!-- Description -->
          <BaseTextArea
            v-model="description"
            label="Description"
            placeholder="description"
            :rules="[rules.required, rules.description]"
          />

          <!-- Image -->
          <div class="d-flex flex-column ga-1">
            <div class="d-flex align-center ga-3">
              <BaseButton variant="outlined" color="primary" @click="triggerUpload">Upload Image</BaseButton>

              <span class="text-grey text-body-2">{{ file_name || 'No image selected' }}</span>

              <label for="edit-image-upload" class="sr-only">Upload listing image</label>
              <input 
                id="edit-image-upload" 
                ref="file_input" 
                type="file"
                accept="image/*" 
                class="hidden-input" 
                :disabled="isLoading"
                @change="handleFileChange"
              />
            </div>

            <p v-if="fileError" class="text-error text-caption ma-0">{{ fileError }}</p>
            <div v-if="saveError" class="text-error text-body-2">{{ saveError }}</div>
          </div>
        </div>
      </v-form>

      <template #actions>
        <BaseButton variant="secondary" :disabled="saving" @click="closeModal">Cancel</BaseButton>
        <BaseButton color="primary" :loading="saving" @click="handleSave">Edit Listing</BaseButton>
      </template>
  </BaseModal>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'

import BaseInput from '~/components/ui/BaseInput.vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

import { useMarketplace } from '~/composables/useMarketplace'
import { useBoardGames } from '~/composables/useBoardGames'

const { editListing } = useMarketplace()

const { searchGenres, genres, isLoading: genresLoading } = useBoardGames()
const { searchGames, games, isLoading: gamesLoading } = useBoardGames()

onMounted(() => {
  searchGenres()
  searchGames()
})

let genreSearchTimeout
const onGenreSearch = (query) => {
  clearTimeout(genreSearchTimeout)
  genreSearchTimeout = setTimeout(() => searchGenres(query), 300)
}

let gameSearchTimeout
const onGameSearch = (query) => {
  clearTimeout(gameSearchTimeout)
  gameSearchTimeout = setTimeout(() => searchGames(query), 300)
}

const open = defineModel({ type: Boolean, default: false })
const props = defineProps({ listing: { type: Object, required: true }})
const emit  = defineEmits(['saved'])

const form = ref(null)
const formValid = ref(false)

const saving = ref(false)
const saveError = ref('')
const fileError = ref('')

const listing_title = ref('')
const game_title = ref('')
const description = ref('')
const listing_type = ref('sell')
const negotiable = ref(false)
const price = ref(0)
const location = ref('')
const file_name = ref('')
const file_input = ref(null)
const image_file = ref(null)

const version = ref('')

const selected_condition = ref(null)
const selected_item_type = ref(null)
const selected_genres = ref(null)

const start_date = ref(null)
const end_date = ref(null)

const MAX_FILE_SIZE = 50 * 1024 * 1024
const ALLOWED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif'])

const startOfDay = (d) => {
  const date = new Date(d)
  date.setHours(0, 0, 0, 0)
  return date
}

// blocks typed keystrokes in the date fields
const blockManualDateEntry = (e) => {
  const allowed = ['Tab', 'Shift', 'Escape', 'Enter']
  if (!allowed.includes(e.key)) {
    e.preventDefault()
  }
}

const blockNegativeKeys = (e) => {
  if (['-', '+', 'e', 'E'].includes(e.key)) {
    e.preventDefault()
  }
}

const blockNegativePaste = (e) => {
  const pasted = (e.clipboardData || window.clipboardData).getData('text')
  if (!/^\d+(\.\d+)?$/.test(pasted)) {
    e.preventDefault()
  }
}

const rules = {
  required: (v) => (v !== null && v !== undefined && String(v).trim() !== '') || 'This field is required',
  requiredArray: (v) => (Array.isArray(v) && v.length > 0) || 'Select at least one genre',
  positiveNumber: (v) => (Number(v) > 0) || 'Amount must be greater than 0',
  listingTitle: (v) => {
    const s = String(v ?? '')
    if (s.length < 3) return 'Title must be at least 3 characters'
    if (s.length > 100) return 'Title cannot exceed 100 characters'
    return true
  },
  version: (v) => (String(v ?? '').length <= 50) || 'Version cannot exceed 50 characters',
  description: (v) => {
    const s = String(v ?? '').trim()
    if (s.length < 10) return 'Description must be at least 10 characters'
    return true
  },
  startNotPast: (v) => {
    if (listing_type.value !== 'rent' || !v) return true
    return startOfDay(v) >= startOfDay(new Date()) || 'Start date cannot be in the past'
  },
  endAfterStart: (v) => {
    if (!start_date.value || !v) return true 
    return new Date(v) > new Date(start_date.value) || 'End date must be after start date'
  },
}

watch(open, val => { // listen for an open & populate ref
  if (!val || !props.listing) return
  saveError.value = ''
  fileError.value = ''
  const listing_element = props.listing
  listing_title.value = listing_element.listingTitle ?? ''
  game_title.value = listing_element.gameTitle ?? ''
  description.value = listing_element.description ?? ''
  listing_type.value = listing_element.listingType === 'rental' ? 'rent' : 'sell'
  negotiable.value = listing_element.isNegotiable ?? false
  price.value = listing_element.price ?? 0
  location.value = listing_element.location ?? ''
  version.value = listing_element.version ?? ''
  selected_genres.value = listing_element.genres ?? []
  selected_condition.value = listing_element.condition ?? null
  selected_item_type.value = listing_element.itemType ?? null
  start_date.value = listing_element.rentalPeriod?.startDate ?? null
  end_date.value = listing_element.rentalPeriod?.endDate ?? null
  file_name.value = ''
  image_file.value = null

  if (selected_genres.value.length) {
    const missing = selected_genres.value.filter(g => !genres.value.includes(g))
    if (missing.length) genres.value = [...missing, ...genres.value]
  }

  if (game_title.value && !games.value.some(g => g.title === game_title.value)) {
    games.value = [{ title: game_title.value }, ...games.value]
  }

  nextTick(() => form.value?.resetValidation())
})

const triggerUpload = () => file_input.value?.click()

const handleFileChange = (e) => {
  fileError.value = ''
  const selected = e.target.files[0]
  if (!selected) return

  if (!ALLOWED_IMAGE_TYPES.includes(selected.type)) {
    fileError.value = 'Please upload a JPEG, PNG, WEBP or GIF image.'
  } else if (selected.size > MAX_FILE_SIZE) {
    fileError.value = 'Image must be smaller than 5MB.'
  }

  if (fileError.value) {
    e.target.value = ''
    file_name.value = ''
    image_file.value = null
    return
  }

  image_file.value = selected
  file_name.value = selected.name
}

function get_rental_period() {
  const fmt = (d) => {
    const date = new Date(d)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${day}/${m}/${y}`
  }
  return [fmt(start_date.value), fmt(end_date.value)]
}

const handleSave = async () => {
  saveError.value = ''
  const { valid } = await form.value.validate()
  if (!valid) return

  if (fileError.value) return

  saving.value = true
  try {
    const listingData = {
      listingTitle: listing_title.value,
      gameTitle: game_title.value,
      listingType: (listing_type.value === 'rent') ? 'rental' : 'sale',
      itemType: selected_item_type.value,
      description: description.value,
      price: Number(price.value),
      condition: selected_condition.value,
      isNegotiable: negotiable.value,
      rentalPeriod: (listing_type.value === 'rent') ? get_rental_period() : null,
      genres: selected_genres.value,
      version: version.value,
      location: location.value
    }

    await editListing(props.listing.listingId, listingData, image_file.value ?? undefined)
    emit('saved', 'updated')
    open.value = false
  } catch (e) {
    saveError.value = e?.data?.message || e?.message || 'Failed to save listing. Please try again.'
  } finally {
    saving.value = false
  }
}

const closeModal = () => {
  open.value = false
  listing_title.value = ''
  game_title.value = ''
  version.value = ''
  description.value = ''
  selected_condition.value = ''
  selected_genres.value = []
  selected_item_type.value = ''
  listing_type.value = 'sell'
  price.value = ''
  rental_period.value = ''
  negotiable.value = false
  location.value = ''
  file_name.value = ''
  image_file.value = null
  saveError.value = ''
  fileError.value = ''
}
const conditions = ['New', 'Like New', 'Good', 'Fair']

const item_types = ["Merch", "Full Boardgame", "Partial Boardgame", "Pieces"]
</script>

<style scoped>
.hidden-input { display: none; }
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>