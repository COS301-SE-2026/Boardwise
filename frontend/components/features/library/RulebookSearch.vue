<template>
  <div class="search-row">
    <div class="search">
      <BaseSearch
        v-model="search"
        placeholder="Search rulebooks..."
        @update:model-value="$emit('search', $event)"
        hide-details
      />      
    </div>
    <BaseButton variant="primary" @click="$emit('upload')"
      class="row-btn"  
    >
      <v-icon start>mdi-upload</v-icon>
      Upload Rulebook
    </BaseButton>
  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const search = ref('')
defineEmits(['search', 'upload'])
</script>

<style scoped>
.search-row {
  display: flex;
  gap: var(--space-4);
  align-items: stretch;
  margin-top: var(--space-6);
}

.search {
  flex: 1;
}

.row-btn {
  white-space: nowrap;
  align-self: stretch;
  height: auto;
}

@media (max-width: 700px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .row-btn{
    height: 50px;
  }
}
</style>