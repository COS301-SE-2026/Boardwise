<template>
  <BaseGrid v-if="editable || games.length" data-test="games-grid" class="games-owned-grid">
    
    <GameCard
      v-for="game in games"
      :key="game.id"
      :id="game.id"
      :title="game.title"
      :category="game.genres?.[0] ?? ''"
      :image="game.imageUrl"
      @remove="$emit('remove-game', game.id)"
    />

    <AddGameCard @add-game="$emit('add-game')" />

  </BaseGrid>

  <BaseEmptyState
  v-else
  title="No games yet"
  message="This player hasn't added any games."
  />

</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
  games: { type: Array, default: () => [] },
  editable: { type: Boolean, default: true }
})

defineEmits(['add-game','remove-game'])
</script>