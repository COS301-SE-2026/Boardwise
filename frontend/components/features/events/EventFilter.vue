<template>
    <BaseFilterSidebar @reset="resetFilters">
        
        <BaseFilterGroup title="Date">
            <BaseFilterPills v-model="selectedDate" :options="dates" />
        </BaseFilterGroup>

        <BaseFilterGroup title="Games Titles">
            <BaseFilterCheckboxGroup v-model="selectedGames" :options="gameOptions" />
        </BaseFilterGroup>

        <BaseFilterGroup title="Format">
            <BaseFilterCheckboxGroup v-model="selectedFormats" :options="['In-Person', 'Online']" />
        </BaseFilterGroup>

    </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch} from 'vue';

import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue';
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue';
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue';
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue';

const emit = defineEmits(['filter'])

const dates = [
  'All',
  'Today',
  'This Week',
  'This Month'
]

const games = [
  'Catan',
  'Chess',
  'Uno',
  'Monopoly',
  'D&D',
  'General'
]

const selectedDate = ref('All')
const selectedGames = ref([])

const filters = reactive({
  online: false,
  inPerson: false
})

watch([selectedDate, selectedGames, filters], () => {
  emit('filter', {
    date: selectedDate.value,
    games: selectedGames.value,
    online: filters.online,
    inPerson: filters.inPerson
  })
}, { deep: true })

const resetFilters = () => {
  selectedDate.value = 'All'
  selectedGames.value = []

  filters.online = false
  filters.inPerson = false
}
</script>