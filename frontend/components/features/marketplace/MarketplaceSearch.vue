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
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'

const search = ref('')

defineEmits(['search','create-listing'])
</script>