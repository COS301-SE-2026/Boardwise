<template>
    <BaseFilterSidebar @resetFilters="resetFilters">
        
        <BaseFilterGroup title="Date">
            <div
                v-for="date in dates"
                :key="date"
                class="date-option"
                :class="{ 'selected': selectedDate === date }"
                @click="selectedDate= date"
            >
                {{ date }}
            </div>
        </BaseFilterGroup>
            <v-checkbox
                v-for="game in games"
                :key="game"
                :label="game"
                :value="game"
                v-model="selectedGames"
                density="compact"
                color="primary"
                hide-details
            />    
        <BaseFilterGroup title="Game">
            <v-checkbox
                v-model="filters.online"
                label="Online"
                density="compact"
                color="primary"
                hide-details
            />

            <v-checkbox
                v-model="filters.inPerson"
                label="In Person"
                density="compact"
                color="primary"
                hide-details
            />
        </BaseFilterGroup>

        <BaseFilterGroup title="Online/Offline">
        
        </BaseFilterGroup>

    </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch} from 'vue';

import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue';
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue';

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

<style scoped>
.date-option {
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.2s;
}

.date-option:hover {
  background: var(--color-surface-hover);
}

.date-option.active {
  background: var(--color-primary);
  color: white;
  font-weight: 600;
}
</style>