<template>
  <section aria-labelledby="architect-action-title">
    <div class="game-architect-step__heading">
      <p class="game-architect-step__eyebrow">Step 1 of 3</p>
      <h1 id="architect-action-title">Decide your action</h1>
      <p>Choose how Boarley should help create your next game concept.</p>
    </div>

    <div class="game-architect-action-grid">
      <button
        type="button"
        class="game-architect-option"
        :class="{ 'game-architect-option--selected': modelValue === 'scale' }"
        :aria-pressed="modelValue === 'scale'"
        @click="emit('update:modelValue', 'scale')"
      >
        <span class="game-architect-option__icon" aria-hidden="true">
          <v-icon icon="mdi-arrow-expand-all" size="36" />
        </span>
        <span class="game-architect-option__title">Scale a game you own</span>
        <span class="game-architect-option__description">
          Start with one game from your collection and describe how you want
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

<script setup lang="ts">
import type { PropType } from 'vue'

import type { GameArchitectMode } from '~/services/gameArchitectService'

defineProps({
  modelValue: {
    type: String as PropType<GameArchitectMode | null>,
    default: null
  }
})

const emit = defineEmits<{
  'update:modelValue': [value: GameArchitectMode]
}>()
</script>
