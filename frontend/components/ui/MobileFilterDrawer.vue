<template>
  <div class="d-flex d-md-none mt-6 mb-4">
    <v-chip
      color="secondary"
      prepend-icon="mdi-filter-variant"
      size="large"
      :aria-expanded="open"
      :aria-controls="id"
      @click="open = true"
    >
      Filters
    </v-chip>

    <v-navigation-drawer v-model="open" temporary location="left" width="300">
      <div :id="id" class="pa-4">
        <slot />
      </div>
    </v-navigation-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  id: { type: String, required: true}
})
const open = ref(false)
</script>