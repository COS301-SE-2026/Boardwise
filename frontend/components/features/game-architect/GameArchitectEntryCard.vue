<template>
  <BaseCard class="game-architect-entry">
    <div class="game-architect-entry__content">
      <div class="game-architect-entry__icon" aria-hidden="true">
        <v-icon icon="mdi-creation-outline" size="32" />
      </div>

      <div class="game-architect-entry__copy">
        <p class="game-architect-entry__eyebrow">Powered by Boarley</p>
        <h2>Design your next board game</h2>
        <p>
          Scale a game you own or combine ideas from your collection into a
          new experience.
        </p>
      </div>

      <BaseButton
        append-icon="mdi-arrow-right"
        @click="openArchitect"
      >
        Open Game Architect
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'

const props = defineProps({
  source: {
    type: String,
    default: 'profile'
  }
})

const router = useRouter()

const openArchitect = () => {
  router.push({
    path: '/game-architect',
    query: { from: props.source }
  })
}
</script>
