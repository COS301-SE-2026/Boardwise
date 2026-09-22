<template>
  <BaseGrid data-test="games-grid" class="games-owned-grid">

    <AddGameCard @add-game="$emit('add-game')" />

    <GameCard
      v-for="game in games"
      :key="game.id"
      :id="game.id"
      :title="game.title"
      :category="game.genres?.[0] ?? ''"
      :image="game.imageUrl"
      @remove="$emit('remove-game', game.id)"
    />

  </BaseGrid>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'

defineProps({
  games: { type: Array, default: () => [] }
})

defineEmits(['add-game','remove-game'])
</script>