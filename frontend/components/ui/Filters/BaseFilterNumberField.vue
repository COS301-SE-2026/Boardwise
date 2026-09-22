<template>
  <v-text-field
    :model-value="modelValue"
    :placeholder="placeholder"
    type="number"
    :min="min"
    density="compact"
    hide-details
    rounded="lg"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '' },
  min: { type: [String, Number], default: 0 }
})
defineEmits(['update:modelValue'])
</script>