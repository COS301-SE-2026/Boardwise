<template>
  <v-empty-state 
    :title="title" 
    :text="message" 
    icon="mdi-inbox-outline"
  />
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: 'Nothing here yet'
  },
  message: {
    type: String,
    default: ' '
  },
  icon: {
    type: String,
    default: 'mdi-inbox-outline'
  }
})
</script>