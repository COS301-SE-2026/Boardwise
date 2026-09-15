<template>
  <div class="base-state" :class="`base-state--${size}`" role="alert">
    <BaseImage
      :src="mascotSrc"
      alt="Boarley looking confused"
      :height="size === 'page' ? '140px' : '80px'"
      :width="size === 'page' ? '140px' : '80px'"
      fit="contain"
      class="base-state__mascot"
    />

    <h3 class="base-state__title">{{ title }}</h3>
    <p v-if="message" class="base-state__message">{{ message }}</p>

    <div v-if="retryable || $slots.actions" class="base-state__actions">
      <slot name="actions">
        <BaseButton v-if="retryable" variant="outlined" color="primary" @click="$emit('retry')">
          Try again
        </BaseButton>
      </slot>
    </div>
  </div>
</template>

<script setup>
import BaseImage from './BaseImage.vue';
import BaseButton from './BaseButton.vue';

defineProps({
  title: {
    type: String,
    default: 'Nothing here yet'
  },
  message: {
    type: String,
    default: ''
  },
  mascotSrc: {
    type: String,
    default: '/images/BoarleySide.svg'
  }, 
  size: {
    type: String,
    default: 'page',
    validator: (v) => ['page', 'compact'].includes(v)
  },
  retryable: {
    type: Boolean, 
    default: false
  }
})

defineEmits(['retry'])
</script>