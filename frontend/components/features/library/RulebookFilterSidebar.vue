<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup
      title="Genre"
      :default-open="false"
    >
      <v-radio-group
        v-model="selectedGenre"
        hide-details
        density="compact"
      >
        <v-radio
          label="All"
          value="all"
          color="primary"
        />

        <v-radio
          v-for="genre in presetGenres"
          :key="genre"
          :label="genre"
          :value="genre"
          color="primary"
          class="text-capitalize"
        />
      </v-radio-group>
    </BaseFilterGroup>

    <BaseFilterGroup
      title="Language"
      :default-open="false"
    >
      <v-checkbox
        v-for="lang in languages"
        :key="lang"
        v-model="selectedLanguages"
        :label="lang"
        :value="lang"
        hide-details
        density="compact"
        color="primary"
      />
    </BaseFilterGroup>

    <BaseFilterGroup
      title="Player Count"
      :default-open="false"
    >
      <v-text-field
        v-model.number="filters.playerCount"
        type="number"
        placeholder="How many players?"
        min="1"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>

    <BaseFilterGroup
      title="Max Duration"
      :default-open="false"
    >
      <v-text-field
        v-model.number="filters.duration"
        type="number"
        placeholder="e.g. 60 minutes"
        min="1"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>

    <BaseFilterGroup
      title="Minimum Age"
      :default-open="false"
    >
      <v-text-field
        v-model.number="filters.minAge"
        type="number"
        placeholder="e.g. 10"
        min="0"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

const emit = defineEmits(['filter'])

const presetGenres = [
  'adventure',
  'card game',
  'economic',
  'family',
  'fantasy',
  'strategy'
]

const languages = [
  'English',
  'French',
  'Spanish'
]

const selectedGenre = ref('all')
const selectedLanguages = ref([])

const filters = reactive({
  playerCount: '',
  duration: '',
  minAge: ''
})

watch(
  [selectedGenre, selectedLanguages, filters],
  () => {
    emit('filter', {
      genre:
        selectedGenre.value === 'all'
          ? null
          : selectedGenre.value,

      languages: selectedLanguages.value,

      playerCount: filters.playerCount,
      duration: filters.duration,
      minAge: filters.minAge
    })
  },
  {
    deep: true
  }
)

const resetFilters = () => {
  selectedGenre.value = 'all'
  selectedLanguages.value = []

  filters.playerCount = ''
  filters.duration = ''
  filters.minAge = ''
}
</script>