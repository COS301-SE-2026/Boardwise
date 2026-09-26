<template>
  <div class="filter-pills">
    <BaseSearch 
      v-model="searchQuery"
      placeholder=""
      aria-label="Search"
      class="filter-pills__search"
    />

    <div class="filter-pill-options">
      <button
        v-for="option in options"
        :key="option"
        type="button"
        class="filter-pill"
        :class="{ 'filter-pill--active': isSelected(option) }"
        :aria-pressed="isSelected(option)"
        @click="toggleOption(option)"
      >
        <v-icon 
          v-if="isSelected(option)"
          icon="mdi-check"
          size="16"
        />

        {{ option }}
      </button>

      <span 
        v-if="options.length === 0"
        class="filter-pill-empty"
      >
        No genres found
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseSearch from '../BaseSearch.vue'

const props = defineProps({
  options: {
    type: Array,
    required: true
  },

  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'search'])

const searchQuery = ref('')

watch(searchQuery, (newQuery) => {
  emit('search', newQuery.toLowerCase())
})

function isSelected(option) {
  return props.modelValue.includes(option)
}

function toggleOption(option) {
  const selected = [...props.modelValue]
  const index = selected.indexOf(option)

  if (index === -1) {
    selected.push(option)
  } else {
    selected.splice(index, 1)
  }

  emit('update:modelValue', selected)
}
</script>