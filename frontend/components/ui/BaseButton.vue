<template>
  <v-btn
    :color="colorMap[variant]"
    :variant="styleMap[variant]"
    :elevation="0"
    rounded="lg"
    class="text-none"
    v-bind="$attrs"
  >
    <slot />
  </v-btn>
</template>

<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: 'primary'
  }
})

// Maps for color and style based on the variant prop
const colorMap = {
  primary: 'primary',
  secondary: 'secondary',
  accent: 'error'
}

const styleMap = {
  primary: 'flat',
  secondary: 'outlined',
  accent: 'flat'
}
</script>