<template>
  <div class="search-row">

    <div class="search">
      <BaseSearch
        placeholder="Search for a community..."
        hide-details
        v-model="model"
      />
    </div>

    <BaseButton variant="secondary" class="row-btn">
      Filters
    </BaseButton>

    <BaseButton variant="primary" class="row-btn">
      + Create a community
    </BaseButton>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

let model = defineModel()
</script>

<style scoped>
.search-row {
  display: flex;
  gap: var(--space-4);
  align-items: stretch;
  margin-top: var(--space-4);
  margin-bottom: var(--space-2);
}

.search {
  flex: 1;
}

.row-btn {
  white-space: nowrap;
  align-self: stretch;
  height: auto;
}

@media (max-width: 768px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .row-btn{
    height: 55px;
  }
}
</style>