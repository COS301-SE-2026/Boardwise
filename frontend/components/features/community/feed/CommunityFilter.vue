<template>
  <div class="community-filter">
    <h3 class="filter-title">Filters</h3>

    <BaseFilterGroup title="Community Type" :default-open="true">
      <div class="filter-options">
        <label v-for="type in types" :key="type" class="filter-option">
          <input 
            type="checkbox" 
            :value="type" 
            v-model="selectedTypes"
            @change="emitFilter"
          />
          <span>{{ type }}</span>
          <span class="filter-count">{{ getTypeCount(type) }}</span>
        </label>
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Category" :default-open="false">
      <div class="filter-options">
        <label v-for="cat in categories" :key="cat" class="filter-option">
          <input 
            type="checkbox" 
            :value="cat" 
            v-model="selectedCategories"
            @change="emitFilter"
          />
          <span>{{ cat }}</span>
          <span class="filter-count">{{ getCategoryCount(cat) }}</span>
        </label>
      </div>
    </BaseFilterGroup>

    <BaseFilterGroup title="Member Count" :default-open="false">
      <div class="filter-options">
        <label v-for="range in memberRanges" :key="range.label" class="filter-option">
          <input 
            type="radio" 
            :value="range.value" 
            v-model="selectedMemberRange"
            @change="emitFilter"
          />
          <span>{{ range.label }}</span>
        </label>
      </div>
    </BaseFilterGroup>

    <v-btn variant="outlined" block @click="resetFilters" class="reset-btn" rounded="lg">
      Reset Filters
    </v-btn>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BaseFilterGroup from '~/components/ui/BaseFilterGroup.vue'
import { communities as mockCommunities } from '~/services/mockData/communities.js'

const props = defineProps({
  activeTab: {
    type: String,
    default: 'All'
  }
})

const emit = defineEmits(['filter-change'])

const types = ['Public', 'Private']
const categories = ['Strategy', 'Family', 'Party', 'Cooperative', 'General']
const memberRanges = [
  { label: 'All', value: 'all' },
  { label: '1-10 members', value: 'small' },
  { label: '11-50 members', value: 'medium' },
  { label: '50+ members', value: 'large' }
]

const selectedTypes = ref([])
const selectedCategories = ref([])
const selectedMemberRange = ref('all')

const communities = computed(() => {
  return mockCommunities || []
})

const getTypeCount = (type) => {
  const data = communities.value
  if (!data || !Array.isArray(data)) return 0
  return data.filter(c => c.type === type).length
}

const getCategoryCount = (category) => {
  const data = communities.value
  if (!data || !Array.isArray(data)) return 0
  return data.filter(c => c.members > 5).length
}

const emitFilter = () => {
  emit('filter-change', {
    types: selectedTypes.value,
    categories: selectedCategories.value,
    memberRange: selectedMemberRange.value
  })
}

const resetFilters = () => {
  selectedTypes.value = []
  selectedCategories.value = []
  selectedMemberRange.value = 'all'
  emitFilter()
}
</script>

<style scoped>.community-filter {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.filter-title {
  margin: 0;
  font-size: var(--fs-h4);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
  font-family: var(--font-display);
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-option {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--fs-body);
  color: var(--color-text);
  cursor: pointer;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  transition: background var(--transition-base);
}

.filter-option:hover {
  background: var(--color-surface-alt);
}

.filter-option input[type="checkbox"],
.filter-option input[type="radio"] {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.filter-count {
  margin-left: auto;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  background: var(--color-bg);
  padding: 0 var(--space-2);
  border-radius: var(--radius-pill);
}

.reset-btn {
  margin-top: var(--space-2);
  font-family: var(--font-button);
  font-weight: var(--fw-medium);
}

@media (max-width: 768px) {
  .community-filter {
    padding: var(--space-4);
  }
}
</style>