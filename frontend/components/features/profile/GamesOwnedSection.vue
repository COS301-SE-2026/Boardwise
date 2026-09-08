<template>
  <section>
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
  games: Array,
  editable: {
    type: Boolean,
    default: false
  }
})

defineEmits(['add-game','remove-game'])
</script>

<style scoped>
.games-owned-section {
  width: 100%;
}
</style>