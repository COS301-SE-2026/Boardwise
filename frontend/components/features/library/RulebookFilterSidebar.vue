<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup title="Genre" :default-open="true">
      <BaseFilterPills v-model="selectedGenre" :options="presetGenres" />
    </BaseFilterGroup>

    <BaseFilterGroup title="Player Count" :default-open="true">
      <BaseFilterNumberField v-model.number="filters.playerCount" placeholder="e.g. 4 players" :min="1" />
    </BaseFilterGroup>

    <BaseFilterGroup title="Max Duration" :default-open="true">
      <BaseFilterNumberField v-model.number="filters.duration" placeholder="e.g. 60 minutes" :min="1" />
    </BaseFilterGroup>

    <BaseFilterGroup title="Minimum Age" :default-open="true">
      <BaseFilterNumberField v-model.number="filters.minAge" placeholder="e.g. 10" :min="0" />
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'
import BaseFilterNumberField from '~/components/ui/Filters/BaseFilterNumberField.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'

const emit = defineEmits(['filter'])

const presetGenres = [
  'all',
  'adventure',
  'card game',
  'economic',
  'family',
  'fantasy',
  'strategy'
]

const selectedGenre = ref('all')

const filters = reactive({
  playerCount: '',
  duration: '',
  minAge: ''
})

watch(
  [selectedGenre, filters],
  () => {
    emit('filter', {
      genre:
        selectedGenre.value === 'all'
          ? null
          : selectedGenre.value,

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
  filters.playerCount = ''
  filters.duration = ''
  filters.minAge = ''
}
</script>