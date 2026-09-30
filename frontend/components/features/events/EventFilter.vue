<template>
    <BaseFilterSidebar @reset="resetFilters">
        
        <BaseFilterGroup title="Date">
            <BaseFilterPills v-model="selectedDate" :options="dates" />
        </BaseFilterGroup>

        <BaseFilterGroup title="Game Titles">
            <BaseFilterCheckboxGroup v-model="selectedGames" :options="gameOptions" />
        </BaseFilterGroup>

        <BaseFilterGroup title="Format">
            <BaseFilterCheckboxGroup v-model="selectedFormats" :options="formatOptions" />
        </BaseFilterGroup>

    </BaseFilterSidebar>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue';
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue';
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue';
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue';

const props = defineProps({
  events: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['filter'])

const dates = ['All', 'Today', 'This Week','This Month']
const formatOptions = ['In-Person', 'Online']

const selectedDate = ref('All')
const selectedGames = ref([])
const selectedFormats = ref([])

const gameOptions = computed(() => {
  const titles = new Set()
  for (const event of props.events) {
    for (const game of event.games ?? []) {
      titles.add(game.title)
    }
  }

  return [...titles].sort()
})

watch([selectedDate, selectedGames, selectedFormats], () => {
  emit('filter', {
    date: selectedDate.value,
    games: selectedGames.value,
    online: selectedFormats.value.includes('Online'),
    inPerson: selectedFormats.value.includes('In-Person')
  })
}, { deep: true })

const resetFilters = () => {
  selectedDate.value = 'All'
  selectedGames.value = []
  selectedFormats.value = []
}
</script>