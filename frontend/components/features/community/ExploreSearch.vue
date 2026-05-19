<template>
  <div class="search-row">

    <div class="search">
      <BaseSearch v-model="search"
        placeholder="Search for a community..."
      />
    </div>
        
    <BaseButton @click="emit('create')">
          + Create a community
        </BaseButton>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'


const search = defineModel()
const emit = defineEmits(['create'])
</script>

<style scoped>
.search-row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-top: 24px;
}

.search {
  flex: 1;
}

@media (max-width: 700px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>