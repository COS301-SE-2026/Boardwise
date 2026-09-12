<template>
  <BaseGrid cols="220px" gap="var(--space-6)">

    <AddGameCard v-if="editable" @add-game="$emit('add-game')" />

    <GameCard
      v-for="game in games"
      :key="game.id"
      :title="game.title"
      :category="game.genres?.[0] ?? ''"
      :image="game.imageUrl"
      :removeable="editable"
      @remove="$emit('remove-game', game.id)"
    />

  </BaseGrid>
</template>

<script setup>
import BaseGrid from '~/components/ui/BaseGrid.vue'
import GameCard from './GameCard.vue'
import AddGameCard from './AddGameCard.vue'

defineProps({
  games: { type: Array, default: () => [] },
  editable: { type: Boolean, default: false }
})

defineEmits(['add-game','remove-game'])
</script>