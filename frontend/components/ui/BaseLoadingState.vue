<template>
  <div class="base-state" :class="`base-state--${size}`" role="status" aria-live="polite">
    <BaseImage
      :src="mascotSrc"
      alt="Boarley is loading"
      :height="size === 'page' ? '120px' : '64px'"
      :width="size === 'page' ? '120px' : '64px'"
      fit="contain"
      class="base-state__mascot base-state__mascot--loading"
    />

    <p v-if="message" class="base-state__message">{{ message }}</p>

    <BaseSpinner size="sm"/>

    <span class="sr-only">Loading</span>
  </div>
</template>

<script setup>
import BaseImage from './BaseImage.vue'
import BaseSpinner from './BaseSpinner.vue';

defineProps({
  message: {
    type: String,
    default: ''
  },
  mascotSrc: {
    type: String,
    default: '/images/Boarley_cute.svg'
  },
  size: {
    type: String,
    default: 'page',
    validator: (v) => ['page', 'compact'].includes(v)
  }
})
</script>