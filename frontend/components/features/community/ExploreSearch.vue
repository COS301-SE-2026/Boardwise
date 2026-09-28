<template>
  <div class="d-flex flex-wrap align-center ga-4 mt-6">

    <div class="flex-grow-1">
      <BaseSearch
        v-model="model"
        placeholder="Search for a community..."
        hide-details
      />
    </div>

    <BaseButton 
      variant="primary" 
      @click="$emit('create-community')"
    >
      + Create a community
    </BaseButton>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const model = defineModel({
  type: String,
  default: ''
})

defineEmits([
  'create-community'
])
</script>
