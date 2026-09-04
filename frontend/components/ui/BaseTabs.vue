<template>
    <v-tabs
        :model-value="activeTab"
        :aria-label="ariaLabel"
        class="base-tabs"
        @update:model-value="$emit('change', $event)"
    >
        <v-tab
            v-for="tab in tabs"
            :key="tab"
            :value="tab"
        >
            {{ tab }}
        </v-tab>    
    </v-tabs>
</template>

<script setup>
defineProps({
    tabs: {
      type: Array,
      default: ''
    },
    activeTab: {
    type: String,
    default: ''
  },
  ariaLabel: {
    type: String,
    default: 'Page sections'
  }
})

defineEmits(['change'])
</script>

<style scoped>
:deep(.v-tabs) {
  border-bottom: 1px solid var(--color-border);
}

:deep(.v-tab) {
  font-family: var(--font-button);
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text-muted);
  text-transform: none;
  letter-spacing: 0;
}

:deep(.v-tab:focus-visible) {
  outline: 3px solid var(--color-primary);
  outline-offset: -3px;
}

:deep(.v-tab--selected) {
  color: var(--color-primary);
}

:deep(.v-tabs__bar .v-slide-group__content) {
  gap: var(--space-4);
}

:deep(.v-slider-thumb),
:deep(.v-tabs-slider) {
  background: var(--color-primary) !important;
  color: var(--color-primary) !important;
}
</style>