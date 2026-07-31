<template>
  <v-dialog v-model="open" max-width="600">
    <BaseCard class="pa-6 d-flex flex-column ga-5" style="background: var(--color-surface) !important; overflow-y: auto;">

      <h2 class="ma-0">Edit Event</h2>

      <BaseInput v-model="name" label="Event Name" placeholder="Event name" variant="outlined" density="compact" hide-details />

      <BaseInput v-model="description" label="Description" placeholder="description" variant="outlined" density="compact" hide-details />

      <BaseInput v-model="location" label="Location" placeholder="e.g. Pretoria" variant="outlined" density="compact" hide-details />

      <v-date-input v-model="date" label="Date" variant="outlined" hide-details />

      <div class="d-flex ga-3">
        <BaseInput 
          v-model="start_time"
          label="Start Time"
          type="time"
          variant="outlined"
          density="compact"
          hide-details
        />

        <BaseInput
          v-model="end_time" 
          label="End Time" 
          type="time" 
          variant="outlined" 
          density="compact" 
          hide-details 
        />
      </div>

      <v-select v-model="selected_visibility" label="Visibility" :items="visibilities" variant="outlined" density="compact" hide-details />

      <v-autocomplete
        v-model="selected_games"
        label="Games"
        :items="game_options"
        :loading="gamesLoading"
        item-title="title"
        item-value="id"
        multiple
        chips
        closable-chips
        variant="outlined"
        density="compact"
        hide-details
        @update:search="onGameSearch"
      />

      <div class="d-flex align-center ga-3">
        <BaseButton 
          variant="outlined" 
          color="primary" 
          @click="triggerUpload"
        >
          Upload Image
        </BaseButton>

        <label for="edit-event-image-upload" class="text-grey text-body-2">{{ file_name || '···' }}</label>
        <input id="edit-event-image-upload" ref="file_input" type="file" accept="image/*" class="hidden-input" @change="handleFileChange" />
      </div>

      <p v-if="error" class="text-error text-body-2 ma-0">{{ error }}</p>

      <div class="d-flex justify-end ga-3">
        <BaseButton 
          variant="outlined" 
          color="primary" 
          @click="closeModal"
        >
          Cancel
        </BaseButton>
        <BaseButton 
          color="primary" 
          :loading="isLoading" 
          @click="handleSave"
        >
          Save Changes
        </BaseButton>
      </div>

    </BaseCard>
  </v-dialog>
</template>

<script setup>
import { useEvents } from '~/composables/useEvents'
import { useBoardGames } from '~/composables/useBoardGames'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseInput from '~/components/ui/BaseInput.vue'

const { updateEvent, error, isLoading } = useEvents()
const { searchGames, games: searchedGames, isLoading: gamesLoading } = useBoardGames()

const open = defineModel()
const props = defineProps({ event: Object })
const emit = defineEmits(['saved'])

const name = ref('')
const description = ref('')
const location = ref('')
const date = ref(null)
const start_time = ref('')
const end_time = ref('')
const selected_visibility = ref(null)
const selected_games = ref([])
const game_options = ref([])
const file_name = ref('')
const file_input = ref(null)
const image_file = ref(null)

watch(open, val => {
  if (!val || !props.event) return
  const eventElement = props.event
  name.value = eventElement.name ?? ''
  description.value = eventElement.description ?? ''
  location.value = eventElement.location ?? ''
  date.value = eventElement.date ?? null
  start_time.value = eventElement.startTime ?? ''
  end_time.value = eventElement.endTime ?? ''
  selected_visibility.value = eventElement.visibility ?? null

  game_options.value = eventElement.games ?? []
  selected_games.value = (eventElement.games ?? []).map(g => g.id)
})

watch(searchedGames, (results) => {
  if (!results?.length) return
  const existingIds = new Set(game_options.value.map(g => g.id))
  const merged = [...game_options.value]
  for (const g of results) {
    if (!existingIds.has(g.id)) merged.push(g)
  }
  game_options.value = merged
})

let gameSearchTimeout
const onGameSearch = (query) => {
  clearTimeout(gameSearchTimeout)
  gameSearchTimeout = setTimeout(() => searchGames(query), 300)
}

const triggerUpload = () => file_input.value?.click()

const handleFileChange = (e) => {
  const file = e.target.files[0] ?? null
  image_file.value = file
  file_name.value = file?.name ?? null
}


function fmt_date(d) {
  const dateObj = new Date(d)
  const y = dateObj.getFullYear()
  const m = String(dateObj.getMonth() + 1).padStart(2, '0')
  const day = String(dateObj.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const handleSave = async () => {
  const eventData = {
    name: name.value,
    description: description.value,
    location: location.value,
    date: fmt_date(date.value),
    startTime: start_time.value,
    endTime: end_time.value,
    visibility: selected_visibility.value,
    games: selected_games.value
  }

  try {
    const res = await updateEvent(props.event.id, eventData, image_file.value ?? undefined)
    emit('saved', res)
    open.value = false
  } catch(err) { 
      console.error('Failed to update event:', err)
   }
}

const closeModal = () => {
  open.value = false
  name.value = ''
  description.value = ''
  location.value = ''
  date.value = null
  start_time.value = ''
  end_time.value = ''
  selected_visibility.value = null
  selected_games.value = []
  game_options.value = []
  file_name.value = ''
  image_file.value = null
}

const visibilities = ['PUBLIC', 'PRIVATE']
</script>

<style scoped>
.hidden-input { display: none; }
</style>