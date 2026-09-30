<template>
  <section
    class="game-architect-job-progress"
    aria-labelledby="architect-progress-title"
  >
    <div class="game-architect-job-progress__spinner">
      <v-progress-circular
        indeterminate
        color="primary"
        size="88"
        width="8"
      />

      <v-icon
        class="game-architect-job-progress__spinner-icon"
        icon="mdi-creation-outline"
        color="primary"
        size="34"
      />
    </div>

    <p class="game-architect-step__eyebrow">
      Request submitted
    </p>

    <h1 id="architect-progress-title">
      Your game is still in progress
    </h1>

    <p>
      Your Game Architect request is currently being processed.
    </p>

    <p class="game-architect-result__notice">
      You can continue using Boardwise. We’ll notify you when your game is
      ready.
    </p>

    <div class="game-architect-result__actions">
      <BaseButton
        variant="primary"
        prepend-icon="mdi-bookshelf"
        to="/library"
      >
        Continue browsing
      </BaseButton>

    </div>
  </section>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue'

defineEmits(['complete'])
</script>