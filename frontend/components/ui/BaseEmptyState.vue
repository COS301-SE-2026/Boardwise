<template>
  <v-empty-state 
    :title="title" 
    :text="message" 
    icon="mdi-inbox-outline"
  />
</template>

<script setup>
defineProps({
  title: String,
  message: String
})
</script>