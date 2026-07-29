<template>
  <v-chip
    :color="resolvedColor"
    variant="flat"
    size="small"
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
  }
})

const colorMap = {
  // Original / Legacy Map
  default:  'var(--bw-gold-muted)',
  rent:     'var(--bw-accent-violet)',
  sale:     'var(--bw-accent-coral)',
  public:   'var(--bw-gold-muted)',
  private:  'var(--bw-navy)',
  
  // Feedback States
  success:  'var(--color-success)',
  warning:  'var(--color-warning)',
  error:    'var(--color-error)',

  // Brand Names & Aliases
  copper:   'var(--copper)',
  fire:     'var(--wildfire)',
  wildfire: 'var(--wildfire)',
  obsidian: 'var(--obsidian)'
}

// Resolve colour 
const resolvedColor = computed(() => {
  const key = props.variant.toLowerCase()
  return colorMap[key] || props.variant
})

</script>

<style scoped>
.base-badge {
  font-family: var(--font-body) !important;
  font-weight: var(--fw-bold) !important;
  letter-spacing: normal !important;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast) !important;
}
 
.badge--absolute {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  z-index: 2;
}

.badge--fire {
  transform: rotate(2deg);
}
.badge--copper {
  transform: rotate(-3deg);
}
.badge--obsidian {
  transform: rotate(-1deg);
}
</style>