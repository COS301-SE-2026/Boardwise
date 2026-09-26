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
import { onMounted, computed } from 'vue'
import { useDebounceFn } from '@vueuse/core'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterNumberField from '~/components/ui/Filters/BaseFilterNumberField.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'
import { useRulebookFilters } from '~/composables/useRulebookFilters'
import { useBoardGames } from '~/composables/useBoardGames'

const {filters, resetFilters} = useRulebookFilters();
const {genres, searchGenres} = useBoardGames();

const availableGenres = computed(() => {
  const combined = ['all', ...filters.genre, ...genres.value];
  return Array.from(new Set(combined));
});

onMounted(() => {
  searchGenres()
})

const handleGenreSearch = useDebounceFn((query) => {
  searchGenres(query);
}, 300);
</script>