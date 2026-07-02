<template>
  <div class="d-flex justify-space-between align-center mb-6">

    <div>
      <h1 class="text-h4 font-weight-bold">{{ title }}</h1>
      <p v-if="subtitle" class="text-medium-emphasis mt-1">{{ subtitle }}</p>
    </div>

    <slot />

  </div>
</template>

<script setup>
defineProps({
  title: String,
  subtitle: String
})
</script>