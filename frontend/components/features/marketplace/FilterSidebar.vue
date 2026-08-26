<template>
  <BaseFilterSidebar data-test="filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="genres">
      <div
        v-for="genre in genres"
        :key="genre"
        class="genre-option"
        :class="{ active: selectedGenre === genre }"
        data-test="`genre-${genre.toLowerCase()}`"
        @click="selectedGenre= genre"
      >
        {{ genre }}
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Listing Type">
      <v-checkbox data-test="rent-filter" v-model="filters.rent" label="Rent" density="compact" color="primary" hide-details />
      <v-checkbox data-test="sale-filter" v-model="filters.sale" label="For Sale" density="compact" color="primary" hide-details />
    </BaseFilterGroup>

    <BaseFilterGroup title="Price Range">
      <div class="d-flex ga-2">
        <v-text-field data-test="min-price" v-model="filters.minPrice" placeholder="Min" prefix="R" type="number" density="compact" hide-details />
        <v-text-field data-test="max-price" v-model="filters.maxPrice" placeholder="Max" prefix="R" type="number" density="compact" hide-details />
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Condition">
      <v-checkbox
        v-for="c in conditions"
        :key="c"
        :data-test="`condition-${c.toLowerCase().replace(' ', '-')}`"
        :label="c"
        :value="c"
        v-model="selectedConditions"
        density="compact"
        color="primary"
        hide-details
      />
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

const emit = defineEmits(['filter'])

const genres = ['All', 'Strategy', 'Family', 'Party', 'Card', 'Abstract']
const conditions = ['New', 'Like New', 'Good', 'Fair']
const selectedGenre  = ref('All')
const selectedConditions = ref([])

const filters = reactive({
  rent: false,
  sale: false,
  minPrice: '',
  maxPrice: '',
})

watch([selectedGenre, selectedConditions, filters], () => {
  emit('filter', {
    genres: selectedGenre.value === 'All' ? null : [selectedGenre.value.toLowerCase()],
    conditions: selectedConditions.value,
    rent: filters.rent,
    sale: filters.sale,
    minPrice: filters.minPrice === '' ? null : Number(filters.minPrice),
    maxPrice: filters.maxPrice === '' ? null : Number(filters.maxPrice),
  })
}, { deep: true })

const resetFilters = () => {
  selectedGenre.value = 'All'
  selectedConditions.value = []
  filters.rent= false
  filters.sale = false
  filters.minPrice = ''
  filters.maxPrice = ''
}


</script>

