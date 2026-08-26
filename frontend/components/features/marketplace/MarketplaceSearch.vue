<template>
  <div class="d-flex ga-3 align-center search-container">
    <div class="flex-1-1">
      <BaseSearch
        data-test="marketplace-search"
        v-model="search"
        placeholder="Search for games..."
        @update:model-value="$emit('search', $event)"
      />      
    </div>

    <BaseButton data-test="create-listing-button" variant="primary" prepend-icon="mdi-plus" @click="$emit('create-listing')"> 
        Create Listing
    </BaseButton>
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')

defineEmits(['search','create-listing'])
</script>