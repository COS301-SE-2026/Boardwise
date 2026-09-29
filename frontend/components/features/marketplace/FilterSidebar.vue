<template>
  <BaseFilterSidebar data-test="filter-sidebar" @reset="resetFilters">

    <BaseFilterGroup title="Genres">
      <BaseFilterPills v-model="selectedGenre" :options="visibleGenres" :multiple="false" @search="onGenreSearch" />
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
import { computed, ref, reactive, watch, onScopeDispose } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'
import BaseFilterCheckboxGroup from '~/components/ui/Filters/BaseFilterCheckboxGroup.vue'
import BaseFilterPills from '~/components/ui/Filters/BaseFilterPills.vue'
import BaseFilterPriceRange from '~/components/ui/Filters/BaseFilterPriceRange.vue'
import { useBoardGames } from '~/composables/useBoardGames'
const emit = defineEmits(['filter'])

const { searchGenres } = useBoardGames()
const hardGenres = ['All', 'Economic', 'Family', 'Party', 'Card Game', 'Abstract']
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
  clearTimeout(timer)
  latest = ''
  searchedGenres.value = null
}

const genreQuery = ref('')
const searchedGenres = ref(null)
let timer = null 
let latest = ''

const visibleGenres = computed(()=> searchedGenres.value ?? hardGenres)

const onGenreSearch = (q) =>{
  clearTimeout(timer)
  const query = q.trim()
  latest = query

  if(!query){
    searchedGenres.value = null
    return;
  }

  timer = setTimeout(async ()=>{
    const res = await searchGenres(query)
    if(query !== latest) return
    searchedGenres.value = res.length? res : hardGenres.filter(g => g.toLowerCase().includes(query.toLowerCase()))
  },300)
}

onScopeDispose(()=> clearTimeout(timer))
</script>