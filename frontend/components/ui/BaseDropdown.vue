<template>
  <v-menu>
    <template #activator="{ props: menuProps}">
      <BaseButton v-bind="{ ...menuProps, ...$attrs }" variant="secondary"
            :aria-label="ariaLabel || label"
      >
        {{ label }}
        <v-icon end aria-hidden="true">mdi-chevron-down</v-icon>
      </BaseButton>
    </template>
    <v-list rounded="lg" elevation="2">
      <slot />
    </v-list>
  </v-menu>
</template>

<script setup>
import BaseButton from './BaseButton.vue';

defineProps({
  label: {
    type: String,
    default: 'Options'
  },
  ariaLabel: {
    type: String,
    default: ''
  }
})
</script>