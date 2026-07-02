<template>
  <div class="d-flex ga-3 align-center">
    <div class="flex-1-1">
      <BaseSearch
        v-model="search"
        placeholder="Search for rulebooks..."
        @update:model-value="$emit('search', $event)"
      />      
    </div>

    <BaseButton variant="primary" prepend-icon="mdi-upload" @click="$emit('upload')"> 
        Upload Rulebook
    </BaseButton>
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')
defineEmits(['search', 'upload'])
</script>