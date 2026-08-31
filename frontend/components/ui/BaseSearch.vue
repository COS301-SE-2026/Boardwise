<template>
  <v-text-field
    v-bind="$attrs"
    :model-value="modelValue"
    :placeholder="placeholder"
    :aria-label="ariaLabel"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    density="compact"
    hide-details
    rounded="xl"
    autocomplete="off"
    class="base-search"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  }, 
  placeholder: {
    type: String,
    default: 'Search'
  },
  ariaLabel: {
    type: String,
    default: 'Search'
  }
})

defineEmits(['update:modelValue'])
</script>