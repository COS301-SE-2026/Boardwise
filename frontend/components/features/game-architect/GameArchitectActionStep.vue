<template>
  <section aria-labelledby="architect-action-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 1 of 3</p>
      <h1 id="architect-action-title">Decide your action</h1>
      <p>Choose how Boarley should help create your next game concept.</p>
    </div>

    <v-alert
      v-if="generationInProgress"
      type="info"
      variant="tonal"
      class="mb-6"
      role="status"
    >
      <strong>Your Game Architect is already working.</strong>
      You cannot start another game concept until the current one is complete.
      Please continue browsing Boardwise—we’ll notify you when it is ready.
    </v-alert>


    <div class="game-architect-action-grid">
      <button
        type="button"
        class="game-architect-option"
        :class="{ 'game-architect-option--selected': modelValue === 'scale' }"
        :aria-pressed="modelValue === 'scale'"
        :disabled="generationInProgress"
        :aria-disabled="generationInProgress"
        @click="emit('update:modelValue', 'scale')"
      >
        <span class="game-architect-option__icon" aria-hidden="true">
          <v-icon icon="mdi-arrow-expand-all" size="36" />
        </span>
        <span class="game-architect-option__title">Scale a game you own</span>
        <span class="game-architect-option__description">
          Start with one game from your collection and choose  how you want
          to expand or adapt it.
        </span>
        <v-icon
          v-if="modelValue === 'scale'"
          class="game-architect-option__check"
          icon="mdi-check-circle"
          color="primary"
          aria-hidden="true"
        />
      </button>

      <button
        type="button"
        class="game-architect-option"
        :class="{ 'game-architect-option--selected': modelValue === 'create' }"
        :aria-pressed="modelValue === 'create'"
        :disabled="generationInProgress"
        :aria-disabled="generationInProgress"
        @click="emit('update:modelValue', 'create')"
      >
        <span class="game-architect-option__icon" aria-hidden="true">
          <v-icon icon="mdi-creation-outline" size="36" />
        </span>
        <span class="game-architect-option__title">Create a new experience</span>
        <span class="game-architect-option__description">
          Draw inspiration from up to three games or let Boarley surprise you.
        </span>
        <v-icon
          v-if="modelValue === 'create'"
          class="game-architect-option__check"
          icon="mdi-check-circle"
          color="primary"
          aria-hidden="true"
        />
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: null
  },
  generationInProgress: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue'
])
</script>
