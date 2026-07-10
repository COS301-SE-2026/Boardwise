<template>
  <BaseGrid cols="220px" gap="var(--space-6)">

    <GameCard
      v-for="game in games"
      :key="game.id"
      :title="game.title"
      :category="game.genres?.[0] ?? ''"
      :image="game.imageUrl"
    />

    <AddGameCard @add-game="$emit('add-game')" />

  </BaseGrid>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'

defineProps({
  games: { type: Array, default: () => [] }
})

defineEmits(['add-game'])
</script>