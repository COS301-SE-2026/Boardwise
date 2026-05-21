<template>
  <div class="header">

    <SectionTitle
      title="Marketplace"
      subtitle="Buy, Rent and List board games with the community"
    />

    <div class="search-row">
      <v-text-field
        class="search"
        placeholder="Search for games..."
        variant="outlined"
        density="compact"
        hide-details
        rounded="lg"
      />

      <!-- <v-btn
        variant="outlined"
        color="primary"
        prepend-icon="mdi-filter-variant"
      >
        Filters
      </v-btn> -->

      <v-btn
        color="primary"
        prepend-icon="mdi-plus"
        @click="$emit('create-listing')"
      >
        Create Listing
      </v-btn>
    </div>

  </div>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'

defineEmits(['create-listing'])
</script>

<style scoped>
.header {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.search-row {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.search { flex: 1; }

@media (max-width: 700px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>