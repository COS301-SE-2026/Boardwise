<template>
  <input class="base-input"
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
</script>

<style scoped>
.base-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
</style>