<template>
  <div class="d-flex flex-wrap ga-6 mt-8">

    <GameCard
      v-for="game in games"
      :key="game.id"
      :title="game.title"
      :category="game.category"
      :image="game.image"
    />

    <AddGameCard @add-game="$emit('add-game')" />

  </div>
</template>

<script setup>
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'

defineProps({
  games: {
    type: Array,
    default: () => []
  }
})

defineEmits(['add-game'])
</script>