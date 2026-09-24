<template>
  <section class="game-architect-result" aria-labelledby="architect-result-title">
    <img
      src="/images/Boarley_cute.svg"
      alt=""
      class="game-architect-result__mascot"
    >

    <div class="game-architect-result__icon" aria-hidden="true">
      <v-icon icon="mdi-check" size="34" />
    </div>

    <p class="game-architect-step__eyebrow">
      Request submitted
    </p>

    <h1 id="architect-result-title">
      We’re building your game
    </h1>

    <p>
      {{ result.message || 'Your game request has been added to the generation queue.' }}
    </p>

    <p class="game-architect-result__notice">
      You can continue using Boardwise. We’ll notify you when your game is ready.
    </p>

    <div class="game-architect-result__actions">
      <BaseButton variant="secondary" to="/library">
        Return to library
      </BaseButton>
      <BaseButton @click="emit('restart')">
        Create another game
      </BaseButton>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import type { GenerateGameResponse } from '~/services/gameArchitectService'

defineProps({
  result: {
    type: Object as PropType<GenerateGameResponse>,
    required: true
  }
})

const emit = defineEmits<{
  restart: []
}>()
</script>
