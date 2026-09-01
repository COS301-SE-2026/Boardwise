<template>
  <v-textarea
    :placeholder="placeholder"
    :rows="rows"
    v-model="model"
    variant="outlined"
    rounded="lg"
    :auto-grow="autoGrow"
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
  },
  autoGrow:{
    type: Boolean,
    default: true
  }
})

const model = defineModel({ default: '' })
</script>