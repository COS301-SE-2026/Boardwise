<template>
  <BaseFilterSidebar @reset="reset">

    <BaseFilterGroup 
      title="Visibility" 
      :default-open="false"
    >
      <v-checkbox
        v-for="type in types"
        :key="type"
        :label="type"
        :value="type.toLowerCase()"
        v-model="selectedTypes"
        hide-details
        density="compact"
      />
    </BaseFilterGroup>

    <BaseFilterGroup 
    title="Category" 
    :default-open="false"
    >

      <v-checkbox
        v-for="cat in categories"
        :key="cat"
        v-model="selectedCategories"
        :label="cat"
        :value="cat.toLowerCase()"
        hide-details
        density="compact"
      />
    </BaseFilterGroup>
  </BaseFilterSidebar>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseFilterSidebar from '~/components/ui/BaseFilterSidebar.vue'

const emit = defineEmits(['filter'])

const types = ['Public', 'Private']

const categories = ['Strategy', 'Family', 'Party', 'Cooperative', 'General']

const selectedTypes = ref([])
const selectedCategories = ref([])

watch([selectedTypes, selectedCategories], () => {
  emit('filter', {
    types: selectedTypes.value,
    categories: selectedCategories.value
  })
}, { deep: true })

const reset = () => {
  selectedTypes.value = []
  selectedCategories.value = []
}
</script>
