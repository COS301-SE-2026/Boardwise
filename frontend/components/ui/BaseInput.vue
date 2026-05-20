<template>
  <input class="input"
    :placeholder="placeholder"
    :type="type"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<script setup> 
defineProps({ 
  placeholder: String,
  type: {
    type: String,
    default: 'text'
  },
  modelValue: String
})

const inputValue = defineModel()
</script>