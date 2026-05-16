<template>
  <section class="section">

    <div class="top">

      <SectionTitle
        title="My Games"
      />

      <div class="actions">

        <BaseButton variant="secondary">
          Filter
        </BaseButton>

        <BaseButton @click="showAddGame = true">
          + Add Game
        </BaseButton>

      </div>

    </div>

    <GamesGrid 
      :games="games"
      @add-game="showAddGame = true"
    />

    <AddGameModal v-model="showAddGame" />

  </section>
</template>

<script setup>
import GamesGrid from './GamesGrid.vue'
import AddGameModal from './AddGameModal.vue'

import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  games: Array
})

const showAddGame = ref(false)
</script>

<style scoped>
.section {
  margin-top: 32px;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .top {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>