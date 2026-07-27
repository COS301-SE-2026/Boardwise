<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <v-text-field
        v-model="genreSearch"
        placeholder="Search genres..."
        density="compact"
        hide-details
        rounded="lg"
        class="mb-3"
        clearable
      />
      
      <div
        class="genre-option text-capitalize"
        :class="{active: selectedGenre === 'all' }"
        @click="selectedGenre='all'"
      >
      All
      </div>

      <div
        v-for="genre in loadedGenres"
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
          v-model="filters.playerCount"
          placeholder="How many players?"
          type="number"
          density="compact"
          hide-details
          rounded="lg"
        />
    </BaseFilterGroup>

    <BaseFilterGroup title="Max Duration (mins)">
      <v-text-field
        v-model="filters.duration"
        placeholder="e.g. 60"
        type="number"
        density="compact"
        hide-details
        rounded="lg"
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Minimum Age">
      <v-text-field
        v-model="filters.minAge"
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
import { ref, reactive, watch , onMounted} from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { BoardGameService } from '~/services/boardgameService'

import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

import { useSnackBar } from '~/composables/useSnackbar';

const { show } = useSnackBar();

const props = defineProps({
  rulebooks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['filter'])

const genreSearch = ref('');
const loadedGenres = ref([]);
const selectedGenre  = ref('all');
const selectedLanguages = ref([]);

const languages = ['English', 'French', 'Spanish'] // TODO: make an enum for this just like for genres

const filters = reactive({
  playerCount: '',
  duration: '',
  minAge: ''
})

const fetchGenres = async (query = '') => {
  try{
    const res = await BoardGameService.getGenres(query);
    loadedGenres.value = res.genres.filter(g => g.toLowerCase() !== 'all');
  }catch(err){
    show('Failed to load genres', 'error');
    console.log('Failed to load genres', err);
  }
}

const debouncedGenreSearch = useDebounceFn((query) => {
  fetchGenres(query);
}, 300)

watch(genreSearch, (newQuery) => {
  debouncedGenreSearch(newQuery);
})

onMounted(() => {
  fetchGenres();
})

watch([selectedGenre, selectedLanguages, filters], () => {
  emit('filter', {
    genre:   selectedGenre.value,
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
  genreSearch.value = ''
}
</script>

