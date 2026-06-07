<template>
  <div class="d-flex flex-column ga-3">
    <p class="text-caption text-uppercase font-weight-bold text-primary mb-0">
      {{ rulebook.genre }}
    </p>
    <h1 class="text-h3 font-weight-bold" style="line-height: 1.1;">
      {{ rulebook.title }}
    </h1>
    <div class="d-flex flex-wrap ga-2 mt-2">
      <v-chip size="small" variant="tonal">
        {{ rulebook.players }}
      </v-chip>

      <v-chip size="small" variant="tonal">
        {{ rulebook.duration }}
      </v-chip>

      <v-chip size="small" variant="tonal">
        Age {{ rulebook.age }}
      </v-chip>

      <v-chip size="small" variant="tonal">
        {{ rulebook.difficulty }}
      </v-chip>
    </div>
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