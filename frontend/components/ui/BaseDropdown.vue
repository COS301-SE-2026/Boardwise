<template>
  <v-menu>
    <template #activator="{ props: menuProps}">
      <v-btn v-bind="{ ...menuProps, ...$attrs }" variant="outlined"
            :aria-label="ariaLabel || label"
      >
        {{ label }}
        <v-icon end aria-hidden="true">mdi-chevron-down</v-icon>
      </v-btn>
    </template>
    <v-list rounded="lg" elevation="2">
      <slot />
    </v-list>
  </v-menu>
</template>

<script setup>
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