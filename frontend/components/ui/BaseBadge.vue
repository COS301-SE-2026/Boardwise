<template>
  <v-chip
    :color="resolvedColor"
    :variant="tone"
    :size="size"
    rounded="lg"
    class="base-badge text-none"
    :class="[
      `badge--${variant.toLowerCase()}`,
      { 'badge--absolute' : absolute }
    ]"
    v-bind="$attrs"
  >
    <slot />
  </v-chip>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'default'
  },
  absolute: {
    type: Boolean,
    default: false
  },
  tone: {
    type: String,
    default: 'flat' 
  },
  size: {
    type: String,
    default: 'small'
  }
})

const colorMap = {
  default:  'var(--bw-gold-muted)',
  rent:     'var(--bw-accent-violet)',
  sale:     'var(--bw-accent-coral)',
  public:   'var(--bw-gold-muted)',
  private:  'var(--bw-navy)',
  secondary: 'var(--color-secondary)',
  primary:  'var(--color-primary)',
  neutral:  'var(--color-text-muted)',

  success:  'var(--color-success)',
  warning:  'var(--color-warning)',
  error:    'var(--color-error)',

  copper:   'var(--copper)',
  fire:     'var(--wildfire)',
  wildfire: 'var(--wildfire)',
  obsidian: 'var(--obsidian)'
}

const resolvedColor = computed(() => {
  const key = props.variant.toLowerCase()
  if (colorMap[key]) return colorMap[key]
  console.warn(`[BaseBadge] Unknown variant "${props.variant}" — passing through as raw color`)
  return props.variant
})
</script>