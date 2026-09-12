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

<style scoped>
.base-search :deep(.v-field) {
  background: var(--alabaster);
  border-radius: var(--radius-pill);
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.base-search :deep(.v-field__outline) {
  color: var(--color-border);
  opacity: 1;
}

.base-search :deep(.v-field__input) {
  color: var(--color-text);
  font-family: var(--font-body);
}

.base-search :deep(.v-field__input input::placeholder) {
  color: var(--color-text-muted);
  opacity: 1;
}

.base-search :deep(.v-icon) {
  color: var(--color-text-muted);
}

.base-search:focus-within {
  outline: 3px solid var(--color-primary);
  outline-offset: 2px;
  border-radius: var(--radius-pill);
}

.base-search:focus-within :deep(.v-field__outline) {
  color: var(--color-primary);
}
</style>