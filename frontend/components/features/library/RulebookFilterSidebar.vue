<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <div
        class="genre-option text-capitalize"
        :class="{active: selectedGenre === 'all' }"
        @click="selectedGenre='all'"
      >
      All
      </div>

      <div
        v-for="genre in presetGenres"
        :key="genre"
        class="genre-option text-capitalize"
        :class="{ active: selectedGenre === genre }"
        @click="selectedGenre= genre"
      >
        {{ genre }}
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Language">
      <v-checkbox
        v-for="lang in languages"
        :key="lang"
        :label="lang"
        :value="lang"
        v-model="selectedLanguages"
        density="compact"
        color="primary"
        hide-details
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Player Count">
        <v-text-field
          v-model.number="filters.playerCount"
          placeholder="How many players?"
          type="number"
          density="compact"
          hide-details
          rounded="lg"
        />
    </BaseFilterGroup>

    <BaseFilterGroup title="Max Duration (mins)">
      <v-text-field
        v-model.number="filters.duration"
        placeholder="e.g. 60"
        type="number"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Minimum Age">
      <v-text-field
        v-model.number="filters.minAge"
        placeholder="e.g. 10"
        type="number"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>
  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch} from 'vue'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

const emit = defineEmits(['filter'])

const presetGenres = [
  'adventure',
  'card game',
  'economic',
  'family',
  'fantasy',
  'strategy',
];
const selectedGenre  = ref('all');
const selectedLanguages = ref([]);

const languages = ['English', 'French', 'Spanish']

const filters = reactive({
  playerCount: '',
  duration: '',
  minAge: ''
})

watch([selectedGenre, selectedLanguages, filters], () => {
  emit('filter', {
    genre:   selectedGenre.value === 'all' ? null : selectedGenre.value,
    languages: selectedLanguages.value,
    ...filters
  })
}, { deep: true })

const resetFilters = () => {
  selectedGenre.value   = 'all'
  selectedLanguages.value = []
  filters.playerCount = ''
  filters.duration = ''
  filters.minAge = ''
}
</script>

