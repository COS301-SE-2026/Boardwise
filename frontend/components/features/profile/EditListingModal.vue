<template>
  <v-dialog v-model="open" max-width="500">
    <BaseCard class="pa-6" style="background: var(--color-surface) !important; overflow-y: auto;">
      <v-form ref="formRef" class="d-flex flex-column ga-5">

      <h2 class="ma-0">Edit Listing</h2>

      <v-text-field
          v-model="listing_title"
          label="Listing Title"
          placeholder="Listing title"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[listingTitleRule]"
        />

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
          hide-details="auto"
          :rules="[requiredRule]"
          @update:search="onGameSearch"
      />

      <v-text-field
          v-model="version"
          label="Version"
          placeholder="e.g. Original"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[versionRule]"
      />

      <v-select
        v-model="selected_condition"
        label="Condition"
        :items="conditions"
        variant="outlined"
        density="compact"
        hide-details="auto"
        :rules="[requiredRule]"
      />

      <v-select
        v-model="selected_item_type"
        label="Item Type"
        :items="item_types"
        variant="outlined"
        density="compact"
        hide-details="auto"
        :rules="[requiredRule]"
      />

      <div class="d-flex">
        <v-btn-toggle v-model="listing_type" color="primary" variant="outlined" mandatory divided>
          <BaseButton value="sell">Sell</BaseButton>
          <BaseButton value="rent">Rent</BaseButton>
        </v-btn-toggle>
      </div>

      <v-text-field
        v-model="price"
        label="Amount"
        prefix="R"
        placeholder="e.g. 650"
        type="number"
        variant="outlined"
        density="compact"
        hide-details="auto"
        :rules="[priceRule]"
       />

      <div class="RentalPeriod">
        <div v-if="listing_type === 'rent'" class="d-flex flex-column ga-3">
            <v-date-input
              v-model="start_date"
              label="Start Date"
              variant="outlined"
              hide-details="auto"
              :rules="[startDateRule]"
              @keydown="blockManualDateEntry"
          />

          <v-date-input
            v-model="end_date"
            label="End Date"
            variant="outlined"
            hide-details="auto"
            :rules="[endDateRule]"
            @keydown="blockManualDateEntry"
          />

        </div>
          <div v-else>
              <v-checkbox v-model="negotiable" label="Open to negotiation" color="primary" density="compact" hide-details />
          </div>
        </div>

        <v-text-field
          v-model="location"
          label="Location"
          placeholder="e.g. Pretoria"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <BaseTextArea
          v-model="description"
          label="Description"
          placeholder="description"
          variant="outlined"
          density="compact"
          hide-details="auto"
          :rules="[requiredRule]"
        />

        <div class="d-flex flex-column ga-1">
          <div class="d-flex align-center ga-3">
            <BaseButton variant="outlined" color="primary" @click="triggerUpload">Upload Image</BaseButton>
            <label for="edit-image-upload" class="text-grey text-body-2">{{ file_name || '···' }}</label>
            <input
              id="edit-image-upload"
              ref="file_input"
              type="file"
              accept="image/*"
              class="hidden-input"
              @change="handleFileChange"
            />
          </div>
          <p v-if="fileError" class="text-error text-caption">{{ fileError }}</p>
        </div>

        <div class="d-flex justify-end ga-3">
          <BaseButton variant="secondary" color="primary" :disabled="isSaving" @click="closeModal">Cancel</BaseButton>
          <BaseButton variant="primary" :loading="isSaving" :disabled="isSaving" @click="handleSave">Edit Listing</BaseButton>
        </div>

      </v-form>
    </BaseCard>
  </v-dialog>
</template>

<script setup>
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseTextArea from '~/components/ui/BaseTextArea.vue'
import { useMarketplace } from '~/composables/useMarketplace'
import { useBoardGames } from '~/composables/useBoardGames'

const { editListing } = useMarketplace()
const { games, searchGames, gamesLoading } = useBoardGames()

const open = defineModel()
const props = defineProps({ listing: Object })
const emit = defineEmits(['saved'])

const conditions = ['New', 'Like New', 'Good', 'Fair']
const item_types = ['Merch', 'Full Boardgame', 'Partial Boardgame', 'Pieces']

onMounted(() => {
  searchGames()
})


let gameSearchTimeout
const onGameSearch = (query) => {
  clearTimeout(gameSearchTimeout)
  gameSearchTimeout = setTimeout(() => searchGames(query), 300)
}

const formRef = ref(null)
const isSaving = ref(false)
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

const start_date = ref(null)
const end_date = ref(null)

const matchItem = (items, v) =>
  items.find(i => i.toLowerCase() === String(v ?? '').toLowerCase()) ?? null

const toDate = (v) => (v ? new Date(v) : null)

watch(open, (val) => {
  if (!val || !props.listing) return
  const l = props.listing
  listing_title.value = l.listingTitle ?? ''
  game_title.value = l.gameTitle ?? ''
  description.value = l.description ?? ''
  listing_type.value = l.listingType === 'rental' ? 'rent' : 'sell'
  negotiable.value = l.isNegotiable ?? false
  price.value = l.price ?? 0
  location.value = l.location ?? ''
  version.value = l.version ?? ''
  selected_condition.value = matchItem(conditions, l.condition)
  selected_item_type.value = matchItem(item_types, l.itemType)
  start_date.value = toDate(l.rentalPeriod?.startDate)
  end_date.value = toDate(l.rentalPeriod?.endDate)
  fileError.value = ''

  nextTick(() => formRef.value?.resetValidation())
})

const requiredRule = (v) =>
  (v !== null && v !== undefined && String(v).trim() !== '') || 'This field is required'

const listingTitleRule = (v) => {
  const required = requiredRule(v)
  if (required !== true) return required
  if (String(v).length < 3) return 'Title must be at least 3 characters'
  if (String(v).length > 100) return 'Title cannot exceed 100 characters'
  return true
}

const versionRule = (v) => {
  const required = requiredRule(v)
  if (required !== true) return required
  if (String(v).length > 50) return 'Version cannot exceed 50 characters'
  return true
}

const priceRule = (v) => {
  if (v === null || v === undefined || v === '') return 'Enter an amount'
  const n = Number(v)
  if (!Number.isFinite(n)) return 'Enter a valid amount'
  if (n <= 0) return 'Amount must be greater than 0'
  return true
}

const startOfDay = (d) => {
  const date = new Date(d)
  date.setHours(0, 0, 0, 0)
  return date
}

const blockManualDateEntry = (e) => {
  const allowed = ['Tab', 'Shift', 'Escape', 'Enter']
  if (!allowed.includes(e.key)) e.preventDefault()
}

const startDateRule = (v) => {
  if (listing_type.value !== 'rent') return true
  if (!v) return 'Start date is required'
  const original = props.listing?.rentalPeriod?.startDate
  if (original && startOfDay(v).getTime() === startOfDay(original).getTime()) return true
  return startOfDay(v) >= startOfDay(new Date()) || 'Start date cannot be in the past'
}

const endDateRule = (v) => {
  if (listing_type.value !== 'rent') return true
  if (!v) return 'End date is required'
  if (start_date.value && startOfDay(v) < startOfDay(start_date.value)) {
    return 'End date must be after start date'
  }
  return true
}

const MAX_FILE_SIZE = 50 * 1024 * 1024
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

const triggerUpload = () => file_input.value?.click()

const handleFileChange = (e) => {
  fileError.value = ''
  const file = e.target.files[0]
  if (!file) return

  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    fileError.value = 'Please upload a JPEG, PNG, WEBP or GIF image.'
  } else if (file.size > MAX_FILE_SIZE) {
    fileError.value = `Image must be smaller than ${MAX_FILE_SIZE / 1024 / 1024}MB.`
  }

  if (fileError.value) {
    e.target.value = ''
    file_name.value = ''
    image_file.value = null
    return
  }

  image_file.value = file
  file_name.value = file.name
}

function get_rental_period() {
  const fmt = (d) => {
    const date = new Date(d)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  return [fmt(start_date.value), fmt(end_date.value)]
}

const handleSave = async () => {
  const { valid } = await formRef.value.validate()
  if (!valid || fileError.value) return

  isSaving.value = true
  try {
    const listingData = {
      listingTitle: listing_title.value,
      gameTitle: game_title.value,
      listingType: listing_type.value === 'rent' ? 'rental' : 'sale',
      itemType: selected_item_type.value.toLowerCase(),
      description: description.value,
      price: Number(price.value),
      condition: selected_condition.value.toLowerCase(),
      isNegotiable: negotiable.value,
      rentalPeriod: listing_type.value === 'rent' ? get_rental_period() : null,
      version: version.value,
      location: location.value,
    }

    const ok = await editListing(props.listing.listingId, listingData, image_file.value ?? undefined)
    if (!ok) return

    emit('saved', 'updated')
    closeModal()
  } finally {
    isSaving.value = false
  }
}

const closeModal = () => {
  open.value = false
  image_file.value = null
  file_name.value = ''
  fileError.value = ''
  if (file_input.value) file_input.value.value = ''
}
</script>

<style scoped>
.hidden-input { display: none; }
</style>