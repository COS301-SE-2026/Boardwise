<template>
  <div class="grid">

    <GameCard
      v-for="game in games"
      :key="game.id"
      :title="game.title"
      :category="game.category"
      :image="game.image"
    />

    <AddGameCard />

  </div>
</template>

<script setup>
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'

defineProps({
  games: Array
})
</script>

<style scoped>
.grid {
  display: grid;

  grid-template-columns:
    repeat(auto-fill, minmax(220px, 1fr));

  gap: 24px;

  margin-top: 32px;
}
</style>