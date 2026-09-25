<template>
  <div class="d-flex ga-3 align-center search-container">
    <div class="flex-1-1">
      <BaseSearch
        v-model="search"
        placeholder="Search for rulebooks..."
        @update:model-value="$emit('search', $event)"
      />      
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'

const search = ref('')
defineEmits(['search', 'upload'])
</script>