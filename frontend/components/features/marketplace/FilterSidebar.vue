<template>
  <BaseFilterSidebar data-test="filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <BaseFilterPills v-model="selectedGenre" :options="genres" />
    </BaseFilterGroup>

    <BaseFilterGroup title="Listing Type">
      <BaseFilterCheckboxGroup v-model="selectedListingTypes" 
        :options="[{ label: 'Rent', value: 'rent'}, { label: 'Sale', value: 'sale'}]"
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Price Range">
      <BaseFilterPriceRange 
        :min="filters.minPrice"
        :max="filters.maxPrice"
        @update:min="filters.minPrice = $event"
        @update:max="filters.maxPrice = $event"
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Condition">
      <BaseFilterCheckboxGroup v-model="selectedConditions" :options="conditions" />
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'

const emit = defineEmits(['filter'])

const genres = ['All', 'Strategy', 'Family', 'Party', 'Card', 'Abstract']
const conditions = ['New', 'Like New', 'Good', 'Fair']
const selectedGenre  = ref('All')
const selectedConditions = ref([])

const filters = reactive({
  minPrice: '',
  maxPrice: '',
})

const rent = computed(() => selectedListingTypes.value.includes('rent'))
const sale = computed(() => selectedListingTypes.value.includes('sale'))

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

<style scoped>
.genre-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.genre-option {
  display: block;
  width: 100%;

  padding: 6px var(--space-3);

  border: none;
  border-radius: var(--radius-sm);

  background: transparent;
  color: var(--color-text);

  font-family: var(--font-body);
  font-size: var(--fs-body);
  font-weight: var(--fw-medium);
  text-align: left;

  cursor: pointer;

  transition:
    background-color var(--transition-fast),
    color var(--transition-fast);
}

.genre-option:hover {
  background: var(--color-surface-alt);
  color: var(--color-primary);
}

.genre-option:focus-visible {
  outline: 3px solid var(--color-primary);
  outline-offset: 2px;
}

.genre-option--active {
  background: rgba(199, 40, 110, 0.12);
  color: var(--color-primary);
  font-weight: var(--fw-bold);
}
</style>