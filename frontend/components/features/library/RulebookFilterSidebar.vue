<template>
  <BaseFilterSidebar @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <div
        v-for="genre in genres"
        :key="genre"
        class="genre-option"
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
        <div class="d-flex ga-2">
            <v-text-field v-model="filters.minPlayers" placeholder="Min" type="number" density="compact" hide-details rounded="lg" />
            <v-text-field v-model="filters.maxPlayers" placeholder="Max" type="number " density="compact" hide-details rounded="lg" />
        </div>
    </BaseFilterGroup>
  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch , computed} from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

const props = defineProps({
  rulebooks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['filter'])

const genres = computed(() => {
  const unique = [...new Set(props.rulebooks.map(r => r.genre))]
  return ['All', ...unique]
})

const languages       = ['English', 'Afrikaans', 'Zulu', 'Spanish']
const selectedGenre  = ref('All')
const selectedLanguages = ref([])

const filters = reactive({
  minPlayers: '',
  maxPlayers: '',
})

watch([selectedGenre, selectedLanguages, filters], () => {
  emit('filter', {
    genre:   selectedGenre.value,
    conditions: selectedLanguages.value,
    ...filters
  })
}, { deep: true })

const resetFilters = () => {
  selectedGenre.value   = 'All'
  selectedLanguages.value = []
  filters.minPlayers     = ''
  filters.maxPlayers     = ''
}
</script>

