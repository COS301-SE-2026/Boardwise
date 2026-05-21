<template>
  <v-chip
    :color="colorMap[variant]"
    size="small"
    rounded="lg"
    v-bind="$attrs"
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
  default: 'primary',
  rent: 'purple',
  sale: 'warning',
  success: 'success',
  warning: 'warning',
  error: 'error'
}
</script>