<template>
  <v-menu>
    <template #activator="{ props: menuProps}">
      <v-btn v-bind="{ ...menuProps, ...$attrs }" variant="outlined">
        {{ label }}
        <v-icon end>mdi-chevron-down</v-icon>
      </v-btn>
    </template>
    <v-list rounded="lg" elevation="2">
      <slot />
    </v-list>
  </v-menu>
</template>

<script setup>
defineProps({
  label: String
})
</script>