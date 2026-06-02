<template>
  <PageContainer>
    <Navbar />

    <PageHeader title="listing" subtitle="Details for specific item available in yo" />

    <div class="grid">
      <SpecificItemListingCard
        v-for="listing in listing"
        :key="listing.id"
        :listing="listing"
      />
    </div>

  </PageContainer>
</template>

<script setup>
import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'
import PageHeader from '~/components/layout/PageHeader.vue'
import SpecificItemListingCard from '~/components/features/specific-item/SpecificItemListingCard.vue'
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
  margin-top: var(--space-6);
}
</style>