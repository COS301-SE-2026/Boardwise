<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup title="Genre" :default-open="true">
      <BaseFilterPills v-model="filters.genre" :options="availableGenres" @search="handleGenreSearch" />
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
import { computed } from 'vue'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterNumberField from '~/components/ui/Filters/BaseFilterNumberField.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'
import { useRulebookFilters } from '~/composables/useRulebookFilters'
import { useBoardGames } from '~/composables/useBoardGames'
import { useDebouncedAutocomplete } from '~/composables/useDebounce'

const {filters, resetFilters} = useRulebookFilters();
const {searchGenres} = useBoardGames();

const {options: apiGenres, refetch: handleGenreSearch, markSelecting} = useDebouncedAutocomplete(searchGenres, {fetchOnMount: true});

const availableGenres = computed(() => {
  const combined = ['all', ...filters.genre, ...apiGenres.value];
  return Array.from(new Set(combined));
});
</script>