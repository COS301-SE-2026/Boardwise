<template>
  <div class="d-flex flex-column ga-3">
    <p class="text-caption text-uppercase font-weight-bold text-primary mb-0">
      {{ rulebook.category }}
    </p>
    <h1 class="text-h3 font-weight-bold" style="line-height: 1.1;">
      {{ rulebook.title }}
    </h1>
  </div>
</template>

<script setup>
defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>