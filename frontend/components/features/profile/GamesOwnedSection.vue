<template>
  <section class="games-owned-section">
    <GamesGrid
      :games="games"
      :editable="editable"
      @add-game="$emit('add-game')"
      @remove-game="$emit('remove-game', $event)"
    />

  </section>
</template>

<script setup>
import GamesGrid from './GamesGrid.vue'

defineProps({
  games: { type: Array, default: () => [] },
  editable: { type: Boolean, default: true }
})

defineEmits(['add-game','remove-game'])
</script>