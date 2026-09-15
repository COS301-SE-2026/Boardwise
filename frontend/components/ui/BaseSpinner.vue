<template>
  <v-progress-circular
    indeterminate
    :color="color"
    :size="sizeMap[size].size"
    :width="sizeMap[size].width"
  />
</template>

<script setup>
const props = defineProps({
  size: {
    type: String,
    default: 'sm',
    validator: (v) => ['xs', 'sm', 'md', 'lg'].includes(v)
  },
  color: {
    type: String,
    default: 'primary'
  }
})

const sizeMap = {
  xs: { size: 14, width: 2 },
  sm: { size: 20, width: 2 },
  md: { size: 28, width: 3 },
  lg: { size: 48, width: 4 }
}
</script>