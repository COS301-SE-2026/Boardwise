<template>
    <v-expansion-panels 
      v-model="openPanels"
      variant="accordion"
      flat
      class="base-filter-group"
    >
      <v-expansion-panel>
        <v-expansion-panel-title class="base-filter-group__title">
          {{ title }}
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <slot />
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
    title: {
      type: String,
      default: 'Filter options'
    }, 
    defaultOpen: {
        type: Boolean,
        default: true
    }
})

const openPanels = ref(props.defaultOpen ? [0] : [])
</script>

<style scoped>
.base-filter-group :deep(.v-expansion-panel) {
  background: transparent;
}

.base-filter-group__title {
  padding-inline: 0 !important;

  color: var(--color-text);
  font-family: var(--font-body);
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
}

.base-filter-group :deep(.v-expansion-panel-text__wrapper) {
  padding-inline: 0;
}
</style>