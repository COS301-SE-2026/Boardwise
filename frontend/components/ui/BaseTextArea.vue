<template>
  <v-textarea
    class="base-textarea"
    :placeholder="placeholder"
    :rows="rows"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
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
    default: ''
  },

  rows: {
    type: Number,
    default: 5
  }
})

defineEmits(['update:modelValue'])
</script>
