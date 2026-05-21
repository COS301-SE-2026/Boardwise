<template>
  <BaseInput
    :placeholder="placeholder"
    v-model="search"
  />
</template>

<script setup>
import BaseInput from './BaseInput.vue'

defineProps({
  placeholder: String
})

const search = defineModel()
</script>