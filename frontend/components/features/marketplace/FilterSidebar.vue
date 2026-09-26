<template>
  <BaseFilterSidebar data-test="filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <BaseFilterPills v-model="selectedGenre" :options="genres" :multiple="false" />
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
import { computed, ref, reactive, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'
import BaseFilterPriceRange from '~/components/ui/Filters/BaseFilterPriceRange.vue'

const emit = defineEmits(['filter'])

const genres = ['All', 'Economic', 'Family', 'Party', 'Card Game', 'Abstract']
const conditions = ['New', 'Like New', 'Good', 'Fair']

const selectedGenre  = ref('All')
const selectedConditions = ref([])
const selectedListingTypes = ref([])

const filters = reactive({
  minPrice: '',
  maxPrice: '',
})

const rent = computed(() => selectedListingTypes.value.includes('rent'))
const sale = computed(() => selectedListingTypes.value.includes('sale'))

watch([selectedGenre, selectedListingTypes, selectedConditions, filters], () => {
  console.log("filter genre val being sent ",selectedGenre.value)
  emit('filter', {
    genres: selectedGenre.value === 'All' ? null : selectedGenre.value,
    conditions: selectedConditions.value,
    rent: rent.value,
    sale: sale.value,
    minPrice: filters.minPrice === '' ? null : Number(filters.minPrice),
    maxPrice: filters.maxPrice === '' ? null : Number(filters.maxPrice),
  })
}, { deep: true })

const resetFilters = () => {
  selectedGenre.value = 'All'
  selectedConditions.value = []
  selectedListingTypes.value = []
  filters.minPrice = ''
  filters.maxPrice = ''
}
</script>