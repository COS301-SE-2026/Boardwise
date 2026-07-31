<template>
  <div class="d-flex ga-3 align-center search-container">
    <div class="flex-1-1">
      <BaseSearch
        v-model="search"
        placeholder="Search for events..."
        @update:model-value="$emit('search',$event)"
      />
    </div>

    <BaseButton
      variant="primary"
      prepend-icon="mdi-calendar-plus"
      @click="$emit('create-event')"
    >
      Create Event
    </BaseButton>
  </div>
</template>

<script setup>
import { ref } from 'vue'

import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')

defineEmits(['create-event','search'])
</script>