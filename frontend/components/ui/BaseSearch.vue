<template>
  <v-text-field
    v-bind="$attrs"
    :model-value="search"
    :placeholder="placeholder"
    variant="outlined"
    density="compact"
    hide-details
    rounded="lg"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup>
defineProps({
  modelValue: String, 
  placeholder: {
    type: String,
    default: 'Search'
  }
})

defineEmits(['update:modelValue'])
</script>