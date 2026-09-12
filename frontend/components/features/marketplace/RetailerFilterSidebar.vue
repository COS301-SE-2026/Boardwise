<template>
  <BaseFilterSidebar data-test="retailer-filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="Retailer">
       <v-checkbox
            v-for="r in retailerOptions"
            :key="r"
            :data-test="`retailer-${r.toLowerCase()}`"
            :label="r"
            :value="r"
            v-model="selectedRetailers"
            density="compact"
            color="primary"
            hide-details
        />
    </BaseFilterGroup>

    <BaseFilterGroup title="Price Range">
      <div class="d-flex ga-2">
        <v-text-field
          data-test="retailer-min-price"
          v-model="filters.minPrice"
          placeholder="Min"
          prefix="R"
          type="number"
          density="compact"
          hide-details
        />

        <v-text-field
          data-test="retailer-max-price"
          v-model="filters.maxPrice"
          placeholder="Max"
          prefix="R"
          type="number"
          density="compact"
          hide-details
        />
      </div>
    </BaseFilterGroup>

  </BaseFilterSidebar>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

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