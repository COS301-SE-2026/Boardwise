<template>
  <v-textarea
    :placeholder="placeholder"
    :rows="rows"
    v-model="model"
    variant="outlined"
    rounded="lg"
    auto-grow
  />
</template>

<script setup>
defineProps({
  placeholder: {
    type: String,
    default: ''
  },
  rows: {
    type: Number,
    default: 5
  }
})

const model = defineModel({ default: '' })
</script>