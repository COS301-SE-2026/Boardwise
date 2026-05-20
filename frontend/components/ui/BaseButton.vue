<template>
  <v-btn
    class="btn"
    :class="`btn--${variant}`"
    :elevation="0"
    v-bind="$attrs"
  >
    <slot />
  </v-btn>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary'
  }
})
</script>