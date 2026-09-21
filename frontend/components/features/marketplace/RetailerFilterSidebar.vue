<template>
  <BaseFilterSidebar data-test="retailer-filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="Retailer">
      <BaseFilterCheckboxGroup v-model="selectedRetailers" :options="retailerOptions" />
    </BaseFilterGroup>

    <BaseFilterGroup title="Price Range">
      <BaseFilterPriceRange 
        :min="filters.minPrice"
        :max="filters.maxPrice"
        @update:min="filters.minPrice = $event"
        @update:max="filters.maxPrice = $event"
      />
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'
import BaseFilterPriceRange from '~/components/ui/Filters/BaseFilterPriceRange.vue'

const props = defineProps({
    retailerOptions: { type: Array, default: () => []}
})

const emit = defineEmits(['filter'])
const selectedRetailers = ref([])

const filters = reactive({
  minPrice: '',
  maxPrice: '',
})

watch([selectedRetailers, filters], () => {
  emit('filter', {
    retailers: selectedRetailers.value.length > 0 ? selectedRetailers.value : null,
    minPrice: filters.minPrice === '' ? null : Number(filters.minPrice),
    maxPrice: filters.maxPrice === '' ? null : Number(filters.maxPrice)
  })
}, { deep: true })

const resetFilters = () => {
  selectedRetailers.value = []
  filters.minPrice = ''
  filters.maxPrice = ''
}
</script>