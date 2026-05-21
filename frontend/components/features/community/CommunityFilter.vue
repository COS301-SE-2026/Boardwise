<template>
  <div class="sidebar">

    <h3>Community Genre</h3>

    <BaseFilterGroup title="Visibility">
      <v-checkbox
        v-for="type in types"
        :key="type"
        :label="type"
        :value="type"
        v-model="selectedTypes"
        hide-details
        density="compact"
      />
    </BaseFilterGroup>

    <BaseFilterGroup title="Category">
      <v-checkbox
        v-for="cat in categories"
        :key="cat"
        :label="cat"
        :value="cat"
        v-model="selectedCategories"
        hide-details
        density="compact"
      />
    </BaseFilterGroup>

    <BaseButton @click="reset">Reset</BaseButton>

  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const emit = defineEmits(['filter'])

const types = ['Public', 'Private']
const categories = ['Strategy', 'Family', 'Party', 'Cooperative', 'General']

const selectedTypes = ref([])
const selectedCategories = ref([])

watch([selectedTypes, selectedCategories], () => {
  emit('filter', {
    types: selectedTypes.value,
    categories: selectedCategories.value
  })
}, { deep: true })

const reset = () => {
  selectedTypes.value = []
  selectedCategories.value = []
}
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  min-width: 220px;
}

@media (max-width: 900px) {
  .sidebar {
    width: 100%;
  }
}

h3 {
  margin: 0;
  font-size: var(--fs-body);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

:deep(.v-checkbox .v-selection-control__input) {
  color: var(--color-primary);
}

:deep(.v-label) {
  font-size: var(--fs-body);
  color: var(--color-text);
}
</style>