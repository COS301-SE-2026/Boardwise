<template>
  <section
    class="game-architect-job-progress"
    aria-labelledby="architect-progress-title"
  ><img
      src="/images/Boarley_cute.svg"
      alt=""
      class="game-architect-result__mascot"
    >

    <div class="game-architect-result__icon" aria-hidden="true">
      <v-icon icon="mdi-check" size="34" />
    </div>

    <p class="game-architect-step__eyebrow">
      Generation complete
    </p>

    <h1 id="architect-result-title">
     {{ gameTitle }}
    </h1>

    <p>
      {{ description}}
    </p>

    <p class="game-architect-result__notice">
      You can continue using Boardwise. We’ll notify you when your game is ready.
    </p>

    <div class="game-architect-result__details">
      <span>{{ playerCount }}</span>
      <span>{{ duration }}</span>
      <span>{{ category }}</span>
    </div>

    <div class="game-architect-result__actions">
      <BaseButton variant="secondary" @click="$emit('view')">
        View game
      </BaseButton>
      <BaseButton @click="emit('restart')">
        Start another
      </BaseButton>
    </div>
  </section>
</template>

<script setup >
import BaseButton from '~/components/ui/BaseButton.vue'

defineProps({
 gameTitle: {
    type: String,
    default: ''
  },
  playerCount: {
    type: String,
    default: ''
  },

  duration: {
    type: String,
    default: ''
  },

  category: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'restart',
  'view'
])
</script>
