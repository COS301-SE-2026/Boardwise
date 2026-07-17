<template>
  <v-chip
    :color="colorMap[variant.toLowerCase()]"
    variant="flat"
    size="small"
    rounded="lg"
    v-bind="$attrs"
    class="base-badge"
  >
    <slot />
  </v-chip>
</template>

<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: 'default'
  }
})

const colorMap = {
  default:  'var(--bw-gold-muted)',
  rent:     'var(--bw-accent-violet)',
  sale:     'var(--bw-accent-coral)',
  public:   'var(--bw-gold)',
  private:  'var(--bw-navy)',
  success:  'var(--color-success)',
  warning:  'var(--color-warning)',
  error:    'var(--color-error)'
}


</script>

<style scoped>
.base-badge{
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
}
</style>