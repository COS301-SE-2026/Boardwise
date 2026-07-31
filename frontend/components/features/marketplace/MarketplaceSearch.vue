<template>
  <div class="search-row">

    <div class="search">
      <BaseSearch placeholder="Search for listings..." />
    </div>

    <!-- <BaseButton variant="secondary">
      Filters
    </BaseButton> -->

    <BaseButton variant="primary" @click="$emit('create-listing')">
      + Create Listing
    </BaseButton>

  </div>
</template>

<script setup>
import BaseSearch from '~/components/ui/BaseSearch.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

defineEmits(['create-listing'])
</script>

<style scoped>
.search-row {
  display:     flex;
  gap:         var(--space-4);
  align-items: center;
  margin-top:  var(--space-6);
}

.search {
  flex: 1;
}

@media (max-width: 700px) {
  .search-row {
    flex-direction: column;
    align-items:    stretch;
  }
}
</style>