<template>
  <div class="filter-pills">
    <div class="filter-search">
      <v-icon 
        icon="mdi-magnify"
        size="16"
      />

      <input 
        v-model="searchQuery"
        type="text"
        placeholder="Search genres"
        aria-label="Search genres"
      />

      <button
        v-if="searchQuery"
        type="button"
        class="filter-pill-search__clear"
        aria-label="Clear genre search"
        @click="searchQuery = ''"
      >
        <v-icon icon="mdi-close" size="14" />
      </button>
    </div>

    <div class="filter-pill-options">
      <button
        v-for="option in options"
        :key="option"
        type="button"
        class="filter-pill"
        :class="{ 'filter-pill--active': isSelected(option) }"
        @click="$emit('update:modelValue', option)"
      >
        {{ option }}
      </button>

      <span 
        v-if="filteredOptions.length === 0"
        class="filter-pill-empty"
      >
        No genres found
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

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

const emit = defineEmits(['update:modelValue'])

const searchQuery = ref('')

const filteredOptions = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  if (!query) {
    return props.options
  }

  return props.options.filter((option) =>
    option.toLowerCase().includes(query)
  )
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