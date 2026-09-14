<template>
  <div class="d-flex justify-space-between align-center flex-wrap ga-3 mb-3">

    <div>
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
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