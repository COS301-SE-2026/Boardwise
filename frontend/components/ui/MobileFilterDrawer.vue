<template>
  <div class="mobile-filters d-md-none mt-6 mb-4">
    <div class="mobile-filters__row" role="toolbar" aria-label="Filters">
      <v-chip
        :color="activeCount ? 'primary' : 'secondary'"
        :variant="activeCount ? 'flat' : 'outlined'"
        prepend-icon="mdi-filter-variant"
        size="large"
        :aria-expanded="open"
        :aria-controls="id"
        @click="open = true"
      >
        Filters <template v-if="activeCount">{{ activeCount }}</template>
      </v-chip>

      <slot name="pills" />
    </div>
      
    <v-bottom-sheet v-model="open" scrollable>
      <v-card :id="id" class="mobile-filters__sheet" rounded="t-xl">
        <div class="mobile-filters__header">
          <h2 class="card-title">Filters</h2>
          <BaseButton icon variant="text" aria-label="Close filters" @click="open = false">
            <v-icon>mdi-close</v-icon>
          </BaseButton>
        </div>

        <v-card-text class="mobile-filters__body">
          <slot />
        </v-card-text>

        <div class="mobile-filters__footer">
          <BaseButton block @click="open = false">Show results</BaseButton>
        </div>
      </v-card>
    </v-bottom-sheet>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from './BaseButton.vue'

defineProps({
  id: { type: String, required: true},
  activeCount: { type: Number, default: 0 }
})
const open = ref(false)
</script>