<template>
  <v-app-bar flat border="b" color="surface" height="64" style="position: sticky; top: 0; z-index: 20;">

    <NuxtLink :to="`/library/${rulebook?.id}`" class="text-primary font-weight-bold text-decoration-none ml-4">
      <v-icon start>mdi-arrow-left</v-icon>
      Back
    </NuxtLink>

    <v-app-bar-title class="text-body-1 font-weight-medium">
      {{ rulebook?.title }}
    </v-app-bar-title>

    <template #append>
      <span class="text-caption text-medium-emphasis mr-4">
        {{ currentPage + 1 }} / {{ totalPages }}
      </span>
    </template>

  </v-app-bar>
</template>

<script setup>
defineProps({
  rulebook: Object,
  currentPage: Number,
  totalPages: Number
})
</script>