<template>
  <textarea
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

<style scoped>
.base-textarea {
  width: 100%;

  padding: 14px 16px;

  border: 1px solid #ddd;
  border-radius: 12px;

  font-size: 15px;
  font-family: inherit;

  resize: vertical;

  outline: none;

  transition: 0.2s ease;

  background: white;

  min-height: 120px;
}

.base-textarea:focus {
  border-color: #6C3BFF;

  box-shadow: 0 0 0 3px rgba(108, 59, 255, 0.15);
}

.base-textarea::placeholder {
  color: #999;
}
</style>