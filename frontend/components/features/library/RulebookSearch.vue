<template>
  <div class="d-flex ga-3 align-center search-container">
    <div class="flex-1-1">
      <BaseSearch
        v-model="search"
        placeholder="Search for rulebooks..."
        @update:model-value="$emit('search', $event)"
      />      
    </div>

    <div class="rulebook-search__actions">
      <BaseButton
        variant="secondary"
        prepend-icon="mdi-creation-outline"
        @click="$emit('architect')"
      >
        Game Architect
      </BaseButton>

    <BaseButton variant="primary" prepend-icon="mdi-upload" @click="$emit('upload')"> 
        Upload Rulebook
    </BaseButton>
  </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')
defineEmits(['search', 'upload', 'architect'])
</script>