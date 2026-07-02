<template>
  <v-empty-state 
    :title="title" 
    :text="description" 
    icon="mdi-inbox-outline"
  />
</template>

<script setup>
defineProps({
  title: String,
  message: String
})
</script>