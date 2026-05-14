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

        <BaseButton>
          + Add Game
        </BaseButton>

      </div>

    </div>

    <GamesGrid :games="games" />

  </section>
</template>

<script setup>
import GamesGrid from './GamesGrid.vue'

import SectionTitle from '~/components/ui/SectionTitle.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
  games: Array
})
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