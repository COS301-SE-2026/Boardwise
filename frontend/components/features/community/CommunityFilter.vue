<template>
    <div class="sidebar">
 
    <h3>Community Genre</h3>
 
    <BaseFilterGroup title="Type">
      <label v-for="type in types" :key="type">
        <input type="checkbox" :value="type" v-model="selectedTypes" />
        {{ type }}
      </label>
    </BaseFilterGroup>
 
    <BaseFilterGroup title="Category">
      <label v-for="cat in categories" :key="cat">
        <input type="checkbox" :value="cat" v-model="selectedCategories" />
        {{ cat }}
      </label>
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
    gap: 16px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    min-width: 220px;
}

@media (max-width: 900px) {
  .sidebar {
    width: 100%;
  }
}

label {
    display: flex;
    gap: 8px;
}

h3 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
}

input[type="checkbox"] {
  accent-color: #6C3BFF;
  width: 15px;
  height: 15px;
  cursor: pointer;
}
</style>