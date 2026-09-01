<template>
  <v-textarea
    v-model="model"
    :label="label"
    :placeholder="placeholder"
    :rows="rows"
    :aria-label="label || ariaLabel"
    variant="outlined"
    rounded="lg"
    :auto-grow="autoGrow"
    auto-grow
    hide-details="auto"
    v-bind="$attrs"
  />
</template>

<script setup>
defineOptions({
  inheritAttrs: false
})
defineProps({
  placeholder: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  ariaLabel: {
    type: Sring,
    default: 'Text input'
  },
  rows: {
    type: Number,
    default: 5
  },
  autoGrow:{
    type: Boolean,
    default: true
  }
})

const model = defineModel({ default: '' })
</script>