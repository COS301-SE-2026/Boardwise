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
        v-for="option in filteredOptions"
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
import BaseSearch from '../BaseSearch.vue'

const props = defineProps({
  options: {
    type: Array,
    required: true
  },

  modelValue: {
    type: [Array, String],
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const searchQuery = ref('')

const filteredOptions = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  if (!query) {
    return props.options
  }

  return props.options.filter(option =>
    option.toLowerCase().includes(query)
  )
})

function isSelected(option) {
  if (props.multiple) {
    return props.modelValue.includes(option)
  }
  return props.modelValue === option
}

function toggleOption(option) {
  if (!props.multiple) {
    emit('update:modelValue', option)
    return
  }

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