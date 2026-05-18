<template>
  <div class="search-row">

    <div class="search">
      <BaseSearch
        placeholder="Search for a community..."
      />
    </div>

    <BaseButton variant="secondary">
      Filters
    </BaseButton>

    <BaseButton variant="primary">
      + Create a community
    </BaseButton>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
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