<template>
  <v-btn
    :color="colorMap[variant] || variant"
    :variant="styleMap[variant] || 'flat'"
    :elevation="0"
    rounded="pill"
    class="text-none base-button"
    :class="`btn--${variant}`"
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

// Maps for color and style based on the variant prop
const colorMap = {
  primary:   'var(--color-primary)',
  secondary: 'var(--color-secondary)',
  accent:    'var(--color-accent)',
  ghost:     'var(--color-surface-alt)',
  error:     'var(--color-error)',
  success:   'var(--color-success)',
  text:      'var(--color-text)'
}

const styleMap = {
  primary:   'flat',
  secondary: 'outlined',
  accent:    'flat',
  ghost:     'flat',
  text:       'text'
}
</script>

<style scoped>
.base-button {
  min-width: 44px;
  min-height: 44px;

  font-family: var(--font-body) !important;
  font-weight: var(--fw-bold) !important;
  letter-spacing: normal !important;
  transition: transform var(--transition-fast), box-shadow var(--transition-base) !important;
}

.base-button:focus-visible {
  outline: 3px solid var(--color-primary);
  outline-offset: 3px;
  box-shadow: 0 0 0 2px var(--color-surface) !important;
}

@media (hover: hover) and (pointer: fine) {
  .base-button:hover {
    transform: translateY(-2px);
  }
}

.base-button:active {
  transform: translateY(1px);
}

.btn--secondary {
  border: 2px solid var(--color-secondary) !important;
}

@media (prefers-reduced-motion: reduce) {
  .base-button {
    transition: none !important;
  }

  .base-button:hover,
  .base-button:active {
    transform: none;
  }
}
</style>