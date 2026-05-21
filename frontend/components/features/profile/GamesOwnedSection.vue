<template>
  <section class="mt-8">

    <div class="d-flex justify-space-between align-center mb-6 flex-wrap ga-4">
      <SectionTitle title="My Games" />
      <div class="d-flex ga-3">
        <v-btn variant="outlined" color="primary">Filter</v-btn>
        <v-btn color="primary" @click="showAddGame = true">+ Add Game</v-btn>
      </div>
    </div>

    <GamesGrid
      :games="games"
      @add-game="showAddGame = true"
    />

    <AddGameModal
      v-model="showAddGame"
      @confirm="addGame"
    />

  </section>
</template>

<script setup>
import GamesGrid from './GamesGrid.vue'
import AddGameModal from './AddGameModal.vue'
import SectionTitle from '~/components/ui/SectionTitle.vue'

const props = defineProps({
  games: Array
})

const emit = defineEmits(['add-game'])

const showAddGame = ref(false)

const addGame = (game) => {
  emit('add-game', {
    id: Date.now(),
    ...game
  })
}
</script>