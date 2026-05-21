<template>
  <v-text-field
    :model-value="modelValue"
    :placeholder="placeholder"
    variant:"outlined"
    
  />
</template>

<script setup>
import BaseInput from './BaseInput.vue'

defineProps({
  placeholder: String
})

const search = defineModel()
</script>