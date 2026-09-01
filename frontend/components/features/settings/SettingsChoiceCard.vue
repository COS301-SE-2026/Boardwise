<template>
  <button
    type="button"
    class="settings-choice-card"
    :class="{
      'settings-choice-card--selected': selected
    }"
    :aria-pressed="selected"
    @click="$emit('select')"
  >
    <span
      class="settings-choice-card__icon"
      aria-hidden="true"
    >
      <v-icon
        :icon="icon"
        size="24"
      />
    </span>

    <span class="settings-choice-card__content">
      <span class="settings-choice-card__title">
        {{ title }}
      </span>

      <span
        v-if="description"
        class="settings-choice-card__description"
      >
        {{ description }}
      </span>
    </span>

    <v-icon
      v-if="selected"
      icon="mdi-check-circle"
      color="primary"
      size="22"
      aria-hidden="true"
    />
  </button>
</template>

<script setup lang="ts">
defineProps({
  title: {
    type: String,
    required: true
  },

  description: {
    type: String,
    default: ''
  },

  icon: {
    type: String,
    required: true
  },

  selected: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select'])
</script>