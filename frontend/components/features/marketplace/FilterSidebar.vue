<template>
  <div class="sidebar">

    <h3>Filters</h3>

    <BaseFilterGroup title="Categories">
      <div
        v-for="category in categories"
        :key="category"
        class="category-option"
        :class="{ active: selectedCategory === category }"
        @click="selectedCategory = category"
      >
        {{ category }}
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Listing Type">
      <v-checkbox v-model="filters.rent" label="Rent"     density="compact" color="primary" hide-details />
      <v-checkbox v-model="filters.sale" label="For Sale" density="compact" color="primary" hide-details />
    </BaseFilterGroup>

    <BaseFilterGroup title="Price Range">
      <div class="d-flex ga-2">
        <v-text-field v-model="filters.minPrice" placeholder="Min" prefix="R" type="number" density="compact" hide-details />
        <v-text-field v-model="filters.maxPrice" placeholder="Max" prefix="R" type="number" density="compact" hide-details />
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Condition">
      <v-checkbox
        v-for="c in conditions"
        :key="c"
        :label="c"
        :value="c"
        v-model="selectedConditions"
        density="compact"
        color="primary"
        hide-details
      />
    </BaseFilterGroup>

    <BaseButton @click="resetFilters">↺ Reset</BaseButton>

  </div>
</template>

<script setup>
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const emit = defineEmits(['filter'])

const categories = ['All', 'Strategy', 'Family', 'Party', 'Card', 'Abstract']
const conditions = ['New', 'Like New', 'Good', 'Fair']
const selectedCategory = ref('All')
const selectedConditions = ref([])

const filters = reactive({
  rent: false,
  sale: false,
  minPrice: '',
  maxPrice: '',
})

const categoryGenreMap = {
  'Strategy': 'strategy',
  'Family': 'family',
  'Party':'party',
  'Card':'card',
  'Abstract': 'abstract strategy'
}


watch([selectedCategory, selectedConditions, () => ({ ...filters })], () => {
  const genres = selectedCategory.value === 'All' ? null : [categoryGenreMap[selectedCategory.value] ?? selectedCategory.value.toLowerCase()] 
  
  console.log("genres to find: " , selectedConditions.value)
  emit('filter', {
    genres,
    category: selectedCategory.value,
    conditions: selectedConditions.value,
    rent: filters.rent,
    sale: filters.sale,
    minPrice: filters.minPrice === '' ? null : Number(filters.minPrice),
    maxPrice: filters.maxPrice === '' ? null : Number(filters.maxPrice),
  })
}, { deep: true })

const resetFilters = () => {
  selectedCategory.value = 'All'
  selectedConditions.value = []
  filters.rent= false
  filters.sale = false
  filters.minPrice = ''
  filters.maxPrice = ''
}


</script>

<style scoped>
.sidebar {
    display: flex;
    flex-direction: column;
    gap: var(--space-4);
    padding: var(--space-5);
    background: var(--color-surface);
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border);
    box-shadow: var(--shadow-sm);
    min-width: 220px;
    max-width: 300px;
}

@media (max-width: 900px) {
  .sidebar { width: 100%; }
}

h3 {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.category-option {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 4px 0;
}

.category-option:hover,
.category-option.active {
  color: var(--color-primary);
  font-weight: var(--fw-bold);
}

:deep(.v-checkbox .v-selection-control__input) {
  color: var(--color-primary);
}

:deep(.v-label) {
  font-size: var(--fs-body);
  color: var(--color-text);
}
</style>