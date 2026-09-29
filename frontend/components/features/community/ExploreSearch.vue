<template>
  <div class="d-flex flex-wrap align-center ga-4 mt-6">

    <div class="flex-grow-1">
      <BaseSearch
        v-model="model"
        placeholder="Search for a community..."
        hide-details
      />
    </div>
  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'

const model = defineModel({
  type: String,
  default: ''
})

defineEmits([
  'create-community'
])
</script>
