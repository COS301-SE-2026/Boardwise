<template>
  <div class="filter-pills">
    <button
      v-for="option in options"
      :key="option"
      type="button"
      class="filter-pill"
      :class="{ 'filter-pill--active': modelValue === option }"
      @click="$emit('update:modelValue', option)"
    >
      {{ option }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  options: { type: Array, required: true },
  modelValue: { type: String, default: null }
})
defineEmits(['update:modelValue'])
</script>