<template>
  <nav class="game-architect-progress" aria-label="Game Architect progress">
    <ol>
      <li
        v-for="item in steps"
        :key="item.number"
        :class="{
          'game-architect-progress__item--active': currentStep === item.number,
          'game-architect-progress__item--complete': currentStep > item.number
        }"
        :aria-current="currentStep === item.number ? 'step' : undefined"
      >
        <span class="game-architect-progress__number">
          <v-icon
            v-if="currentStep > item.number"
            icon="mdi-check"
            size="18"
            aria-hidden="true"
          />
          <span v-else>{{ item.number }}</span>
        </span>
        <span>{{ item.label }}</span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
defineProps({
  currentStep: {
    type: Number,
    required: true
  }
})

const steps = [
  { number: 1, label: 'Choose action' },
  { number: 2, label: 'Select games' },
  { number: 3, label: 'Review idea' }
]
</script>
