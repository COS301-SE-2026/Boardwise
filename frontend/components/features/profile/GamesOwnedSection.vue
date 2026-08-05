<template>
  <section>
    <GamesGrid
      :games="games"
      @add-game="$emit('add-game')"
      @remove-game="$emit('remove-game', $event)"
    />

  </section>
</template>

<script setup>
import GamesGrid from './GamesGrid.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

defineProps({
  games: Array
})

defineEmits(['add-game','remove-game'])
</script>